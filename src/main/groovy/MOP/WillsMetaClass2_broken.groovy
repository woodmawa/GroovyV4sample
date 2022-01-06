package MOP


import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.MixinInMetaClass;
import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.callsite.*;
import org.codehaus.groovy.runtime.metaclass.*;
import org.codehaus.groovy.util.FastArray;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ExpandoMetaClass is a MetaClass that behaves like an Expando, allowing the addition or replacement
 * of methods, properties and constructors on the fly.
 * <p>
 * Some examples of usage:
 * <pre>
 * // defines or replaces instance method:
 * metaClass.myMethod = { args {@code ->} }
 *
 * // defines a new instance method
 * metaClass.myMethod {@code <<} { args {@code ->} }
 *
 * // creates multiple overloaded methods of the same name
 * metaClass.myMethod {@code <<} { String s {@code ->} } {@code <<} { Integer i {@code ->} }
 *
 * // defines or replaces a static method with the 'static' qualifier
 * metaClass.'static'.myMethod = { args {@code ->}  }
 *
 * // defines a new static method with the 'static' qualifier
 * metaClass.'static'.myMethod {@code <<} { args {@code ->}  }
 *
 * // defines a new constructor
 * metaClass.constructor {@code <<} { String arg {@code ->} }
 *
 * // defines or replaces a constructor
 * metaClass.constructor = { String arg {@code ->} }
 *
 * // defines a new property with an initial value of "blah"
 * metaClass.myProperty = "blah"
 * </pre>
 * <p>
 * ExpandoMetaClass also supports a DSL/builder like notation to combine multiple definitions together. So instead of this:
 * <pre>
 * Number.metaClass.multiply = { Amount amount {@code ->} amount.times(delegate) }
 * Number.metaClass.div =      { Amount amount {@code ->} amount.inverse().times(delegate) }
 * </pre>
 * You can also now do this:
 * <pre>
 * Number.metaClass {
 *     multiply { Amount amount {@code ->} amount.times(delegate) }
 *     div      { Amount amount {@code ->} amount.inverse().times(delegate) }
 * }
 * </pre>
 * <p>
 * ExpandoMetaClass also supports runtime mixins. While {@code @Mixin} allows you to mix in new behavior
 * to classes you own and are designing, you can not easily mixin anything to types you didn't own, e.g.
 * from third party libraries or from JDK library classes.
 * Runtime mixins let you add a mixin on any type at runtime.
 * <pre>
 * interface Vehicle {
 *     String getName()
 * }
 *
 * // Category annotation style
 * {@code @Category}(Vehicle) class FlyingAbility {
 *     def fly() { "I'm the ${name} and I fly!" }
 * }
 *
 * // traditional category style
 * class DivingAbility {
 *     static dive(Vehicle self) { "I'm the ${self.name} and I dive!" }
 * }
 *
 * // provided by a third-party, so can't augment using Mixin annotation
 * class JamesBondVehicle implements Vehicle {
 *     String getName() { "James Bond's vehicle" }
 * }
 *
 * // Can be added via metaClass, e.g.:
 * // JamesBondVehicle.metaClass.mixin DivingAbility, FlyingAbility
 * // Or using shorthand through DGM method on Class
 * JamesBondVehicle.mixin DivingAbility, FlyingAbility
 *
 * assert new JamesBondVehicle().fly() ==
 *        "I'm the James Bond's vehicle and I fly!"
 * assert new JamesBondVehicle().dive() ==
 *        "I'm the James Bond's vehicle and I dive!"
 * </pre>
 * As another example, consider the following class definitions:
 * <pre>
 * class Student {
 *     List<String> schedule = []
 *     def addLecture(String lecture) { schedule {@code <<} lecture }
 * }
 *
 * class Worker {
 *     List<String> schedule = []
 *     def addMeeting(String meeting) { schedule {@code <<} meeting }
 * }
 * </pre>
 * We can mimic a form of multiple inheritance as follows:
 * <pre>
 * class CollegeStudent {
 *     static { mixin Student, Worker }
 * }
 * new CollegeStudent().with {
 *     addMeeting('Performance review with Boss')
 *     addLecture('Learn about Groovy Mixins')
 *     println schedule
 *     println mixedIn[Student].schedule
 *     println mixedIn[Worker].schedule
 * }
 * </pre>
 * Which outputs these lines when run:
 * <pre>
 * [Performance review with Boss]
 * [Learn about Groovy Mixins]
 * [Performance review with Boss]
 * </pre>
 * Perhaps some explanation is required here. The methods and properties of Student and Worker are
 * added to CollegeStudent. Worker is added last, so for overlapping methods, its methods will
 * be used, e.g. when calling <code>schedule</code>, it will be the schedule property (getSchedule method)
 * from Worker that is used. The schedule property from Student will be shadowed but the <code>mixedIn</code>
 * notation allows us to get to that too if we need as the last two lines show.
 * <p>
 * We can also be a little more dynamic and not require the CollegeStudent class to
 * be defined at all, e.g.:
 * <pre>
 * def cs = new Object()
 * cs.metaClass {
 *     mixin Student, Worker
 *     getSchedule {
 *         mixedIn[Student].schedule + mixedIn[Worker].schedule
 *     }
 * }
 * cs.with {
 *     addMeeting('Performance review with Boss')
 *     addLecture('Learn about Groovy Mixins')
 *     println schedule
 * }
 * </pre>
 * Which outputs this line when run:
 * <pre>
 * [Learn about Groovy Mixins, Performance review with Boss]
 * </pre>
 * As another example, we can also define a no dup queue by mixing in some
 * Queue and Set functionality as follows:
 * <pre>
 * def ndq = new Object()
 * ndq.metaClass {
 *     mixin ArrayDeque
 *     mixin HashSet
 *     leftShift = { Object o  {@code ->}
 *         if (!mixedIn[Set].contains(o)) {
 *             mixedIn[Queue].push(o)
 *             mixedIn[Set].add(o)
 *         }
 *     }
 * }
 * ndq {@code <<} 1
 * ndq {@code <<} 2
 * ndq {@code <<} 1
 * assert ndq.size() == 2
 * </pre>
 * As a final example, we sometimes need to pass such mixed in classes or objects
 * into Java methods which require a given static type but the ExpandoMetaClass mixin approach uses a very dynamic
 * approach based on duck typing rather than static interface definitions, so doesn't by default
 * produce objects matching the required static type. Luckily, there is a mixins capability
 * within ExpandoMetaClass which supports the use of Groovy's common 'as StaticType' notation to produce an object
 * having the correct static type so that it can be passed to the Java method call in question.
 * A slightly contrived example illustrating this feature:
 * <pre>
 * class CustomComparator implements Comparator {
 *     int compare(Object a, b) { return a.size() - b.size() }
 * }
 *
 * class CustomCloseable implements Closeable {
 *     void close() { println 'Lights out - I am closing' }
 * }
 *
 * import static mypackage.IOUtils.closeQuietly
 * import static java.util.Collections.sort
 * def o = new Object()
 * o.metaClass.mixin CustomComparator, CustomCloseable
 * def items = ['a', 'bbb', 'cc']
 * sort(items, o as Comparator)
 * println items                // {@code =>} [a, cc, bbb]
 * closeQuietly(o as Closeable) // {@code =>} Lights out - I am closing
 * </pre>
 * <p>
 * <b>Further details</b>
 * <p>
 * When using the default implementations of MetaClass, methods are only allowed to be added before initialize() is called.
 * In other words you create a new MetaClass, add some methods and then call initialize(). If you attempt to add new methods
 * after initialize() has been called, an error will be thrown. This is to ensure that the MetaClass can operate appropriately
 * in multi-threaded environments as it forces you to do all method additions at the beginning, before using the MetaClass.
 * <p>
 * ExpandoMetaClass differs here from the default in that it allows you to add methods after initialize has been called.
 * This is done by setting the initialize flag internally to false and then add the methods. Since this is not thread
 * safe it has to be done in a synchronized block. The methods to check for modification and initialization are
 * therefore synchronized as well. Any method call done through this meta class will first check if the it is
 * synchronized. Should this happen during a modification, then the method cannot be selected or called unless the
 * modification is completed.
 * <p>
 *
 * @since 1.5
 */
public class WillsMetaClass2_broken extends MetaClassImpl implements GroovyObject {

    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final String META_CLASS = "metaClass";
    private static final String CLASS = "class";
    private static final String META_METHODS = "metaMethods";
    private static final String METHODS = "methods";
    private static final String PROPERTIES = "properties";
    public static final String STATIC_QUALIFIER = "static";
    public static final String CONSTRUCTOR = "constructor";
    private static final String CLASS_PROPERTY = "class";
    private static final String META_CLASS_PROPERTY = "metaClass";
    private static final String GROOVY_CONSTRUCTOR = "<init>";
    private MetaClass myMetaClass;
    private volatile boolean initialized;
    private volatile boolean modified;
    private boolean initCalled;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();
    private final boolean allowChangesAfterInit;
    public boolean inRegistry;
    private final Set<MetaMethod> inheritedMetaMethods = new HashSet<MetaMethod>();
    private final Map<String, MetaProperty> beanPropertyCache = new ConcurrentHashMap<String, MetaProperty>(16, 0.75f, 1);
    private final Map<String, MetaProperty> staticBeanPropertyCache = new ConcurrentHashMap<String, MetaProperty>(16, 0.75f, 1);
    private final Map<MethodKey, MetaMethod> expandoMethods = new ConcurrentHashMap<MethodKey, MetaMethod>(16, 0.75f, 1);
    private final ConcurrentHashMap expandoSubclassMethods = new ConcurrentHashMap(16, 0.75f, 1);
    private final Map<String, MetaProperty> expandoProperties = new ConcurrentHashMap<String, MetaProperty>(16, 0.75f, 1);
    private ClosureStaticMetaMethod invokeStaticMethodMethod;
    private final Set<MixinInMetaClass> mixinClasses = new LinkedHashSet<MixinInMetaClass>();

    /**
     * For simulating closures in Java
     */
    private interface Callable {
        public abstract void call();
    }


    public Collection getExpandoSubclassMethods() {
        return expandoSubclassMethods.values();
    }

    public WillsMetaClass2_broken(Class theClass, boolean register, boolean allowChangesAfterInit, MetaMethod[] add) {
        this(GroovySystem.getMetaClassRegistry(), theClass, register, allowChangesAfterInit, add);
    }

    public WillsMetaClass2_broken(MetaClassRegistry registry, Class theClass, boolean register, boolean allowChangesAfterInit, MetaMethod[] add) {
        super(registry, theClass, add);
        Class<? extends Class> clazz = theClass //.getClass();
        //had to patch this here orig code didnt work
        this.myMetaClass = InvokerHelper.getMetaClass(clazz);
        this.inRegistry = register;
        this.allowChangesAfterInit = allowChangesAfterInit;
    }

    /**
     * Constructs a new ExpandoMetaClass instance for the given class
     *
     * @param theClass The class that the MetaClass applies to
     */
    public WillsMetaClass2_broken(Class theClass) {
        this(theClass, false, false, null);
    }

    public WillsMetaClass2_broken(Class theClass, MetaMethod[] add) {
        this(theClass, false, false, add);
    }

    /**
     * Constructs a new ExpandoMetaClass instance for the given class optionally placing the MetaClass
     * in the MetaClassRegistry automatically
     *
     * @param theClass The class that the MetaClass applies to
     * @param register True if the MetaClass should be registered inside the MetaClassRegistry. This defaults to true and ExpandoMetaClass will effect all instances if changed
     */
    public WillsMetaClass2_broken(Class theClass, boolean register) {
        this(theClass, register, false, null);
    }

    /*
    //todo fix later
    public Object WillsMetaClass2pandoMetaClass(Class theClass, boolean register, MetaMethod[] add) {
        return invokeMethod("this", new Object[]{theClass, register, false, add});
    }*/

    /**
     * Constructs a new ExpandoMetaClass instance for the given class optionally placing the MetaClass
     * in the MetaClassRegistry automatically
     *
     * @param theClass              The class that the MetaClass applies to
     * @param register              True if the MetaClass should be registered inside the MetaClassRegistry. This defaults to true and ExpandoMetaClass will effect all instances if changed
     * @param allowChangesAfterInit Should the meta class be modifiable after initialization. Default is false.
     */
    public WillsMetaClass2_broken(Class theClass, boolean register, boolean allowChangesAfterInit) {
        this(theClass, register, allowChangesAfterInit, null);
    }

    @Override
    public MetaMethod findMixinMethod(String methodName, Class[] arguments) {
        for (MixinInMetaClass mixin : mixinClasses) {
            final CachedClass mixinClass = mixin.getMixinClass();
            MetaClass metaClass = mixinClass.classInfo.getMetaClassForClass();
            if (metaClass == null) {
                metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(mixinClass.getTheClass());
            }


            MetaMethod metaMethod = metaClass.pickMethod(methodName, arguments);
            if (metaMethod == null && metaClass instanceof MetaClassImpl) {
                MetaClassImpl mc = (MetaClassImpl) metaClass;
                for (CachedClass cl = mc.getTheCachedClass().getCachedSuperClass(); ; cl != null ){
                    metaMethod = mc.getMethodWithoutCaching(cl.getTheClass(), methodName, arguments, false);
                    if (metaMethod != null) break;
                }

            }

            if (metaMethod != null) {
                MetaMethod method = new MixinInstanceMetaMethod(metaMethod, mixin);

                if (method.getParameterTypes().length == 1 && !method.getParameterTypes()[0].isPrimitive) {
                    MetaMethod noParam = pickMethod(methodName, EMPTY_CLASS_ARRAY);
                    // if the current call itself is with empty arg class array, no need to recurse with 'new Class[0]'
                    if (noParam == null && arguments.length != 0) {
                        try {
                            findMixinMethod(methodName, EMPTY_CLASS_ARRAY);
                        } catch (MethodSelectionException msex) {
                            /*
                             * Here we just additionally tried to find another no-arg mixin method of the same name and register that as well, if found.
                             * Safe to ignore a MethodSelectionException in this additional exercise. (GROOVY-4999)
                             */
                        }

                    }

                }


                registerInstanceMethod(method);
                return method;
            }

        }

        return null;
    }

    @Override
    protected void onInvokeMethodFoundInHierarchy(MetaMethod method) {
        this.invokeMethodMethod = method;
    }

    @Override
    protected void onSuperMethodFoundInHierarchy(MetaMethod method) {
        addSuperMethodIfNotOverridden(method);
    }

    @Override
    protected void onSuperPropertyFoundInHierarchy(MetaBeanProperty property) {
        addMetaBeanProperty(property);
    }

    @Override
    protected void onSetPropertyFoundInHierarchy(MetaMethod method) {
        this.setPropertyMethod = method;
    }

    @Override
    protected void onGetPropertyFoundInHierarchy(MetaMethod method) {
        this.getPropertyMethod = method;
    }

    @Override
    public boolean isModified() {
        return this.modified;
    }

    public void registerSubclassInstanceMethod(String name, Class klazz, Closure closure) {
        final List<MetaMethod> list = ClosureMetaMethod.createMethodList(name, klazz, closure);
        for (MetaMethod metaMethod : list) {
            registerSubclassInstanceMethod(metaMethod);
        }

    }

    public void registerSubclassInstanceMethod(MetaMethod metaMethod) {
        modified = true;

        final String name = metaMethod.getName();
        Object methodOrList = expandoSubclassMethods.get(name);
        if (methodOrList == null) {
            expandoSubclassMethods.put(name, metaMethod);
        } else {
            if (methodOrList instanceof MetaMethod) {
                FastArray arr = new FastArray(2);
                arr.add(methodOrList);
                arr.add(metaMethod);
                expandoSubclassMethods.put(name, arr);
            } else {
                ((FastArray) methodOrList).add(metaMethod);
            }

        }

    }

    public void addMixinClass(MixinInMetaClass mixin) {
        mixinClasses.add(mixin);
    }

    public Object castToMixedType(Object obj, Class type) {
        for (MixinInMetaClass mixin : mixinClasses) {
            if (type.isAssignableFrom(mixin.getMixinClass().getTheClass())) return mixin.getMixinInstance(obj);
        }

        return null;
    }

    /**
     * Call to enable global use of ExpandoMetaClass within the registry.
     * This has the advantage that inheritance will function correctly and
     * metaclass modifications will also apply to existing objects,
     * but has a higher memory usage on the JVM than normal Groovy
     */
    public static void enableGlobally() {
        DefaultMetaClassInfo.setWithoutCustomMetaclassCreationHandle(false);
        ExpandoMetaClassCreationHandle.enable();
    }

    /**
     * Call to disable the global use of ExpandoMetaClass
     */
    public static void disableGlobally() {
        DefaultMetaClassInfo.setWithoutCustomMetaclassCreationHandle(true);
        ExpandoMetaClassCreationHandle.disable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        try {
            writeLock.lock();
            if (!isInitialized()) {
                super.initialize();
                setInitialized(true);
                this.initCalled = true;
            }

        } finally {
            // downgrade to readlock before releasing just in case
            readLock.lock();
            writeLock.unlock();
            readLock.unlock();
        }

    }

    /**
     * Checks if the meta class is initialized.
     *
     * @see MetaClassImpl#isInitialized()
     */
    @Override
    protected boolean isInitialized() {
        try {
            readLock.lock();
            return this.initialized;
        } finally {
            readLock.unlock();
        }

    }

    @Override
    protected void setInitialized(boolean b) {
        this.initialized = b;
    }

    private void addSuperMethodIfNotOverridden(final MetaMethod metaMethodFromSuper) {
        performOperationOnMetaClass(new ExpandoMetaClass.Callable() {
            @Override
            public void call() {

                MetaMethod existing = null;
                try {
                    existing = pickMethod(metaMethodFromSuper.getName(), metaMethodFromSuper.getNativeParameterTypes());
                } catch (GroovyRuntimeException e) {
                    // ignore, this happens with overlapping method definitions
                }


                if (existing == null) {
                    addMethodWithKey(metaMethodFromSuper);
                } else {
                    boolean isGroovyMethod = getMetaMethods().contains(existing);

                    if (isGroovyMethod) {
                        addMethodWithKey(metaMethodFromSuper);
                    } else if (inheritedMetaMethods.contains(existing)) {
                        inheritedMetaMethods.remove(existing);
                        addMethodWithKey(metaMethodFromSuper);
                    }

                }

            }

            private void addMethodWithKey(final MetaMethod aMetaMethodFromSuper) {
                inheritedMetaMethods.add(aMetaMethodFromSuper);
                if (aMetaMethodFromSuper instanceof ClosureMetaMethod) {
                    ClosureMetaMethod closureMethod = (ClosureMetaMethod) aMetaMethodFromSuper;
                    String name = aMetaMethodFromSuper.getName();
                    final Class declaringClass = aMetaMethodFromSuper.getDeclaringClass().getTheClass();
                    ClosureMetaMethod localMethod = ClosureMetaMethod.copy(closureMethod);
                    addMetaMethod(localMethod);

                    MethodKey key = new DefaultCachedMethodKey(declaringClass, name, localMethod.getParameterTypes(), false);

                    checkIfGroovyObjectMethod(localMethod);
                    DefaultGroovyMethods.invokeMethod(getExpandoMethods(), "put", new Object[]{key, localMethod});

                }

            }

        });
    }

    private void registerStaticBeanProperty(final String property, final Object newValue) {
        performOperationOnMetaClass(new Closure<Object>(this, this) {
            public void doCall(Object it) {
                Class type = newValue == null ? Object.class : newValue.getClass();

                MetaBeanProperty mbp = newValue instanceof MetaBeanProperty ? (MetaBeanProperty) newValue : new StaticExpandoProperty(getTheClass(), property, type, newValue);//ThreadManagedMetaBeanProperty(theClass, property, type, newValue);

                final MetaMethod getter = mbp.getGetter();
                final MethodKey getterKey = new DefaultCachedMethodKey(getTheClass(), getter.getName(), CachedClass.EMPTY_ARRAY, false);
                final MetaMethod setter = mbp.getSetter();
                final MethodKey setterKey = new DefaultCachedMethodKey(getTheClass(), setter.getName(), setter.getParameterTypes(), false);
                addMetaMethod(getter);
                addMetaMethod(setter);

                //avoid calling getExpandoMethods() by getProperty() interceptor,  as this returns a list
                WillsMetaClass2_broken.this.expandoMethods.put(setterKey, setter);
                WillsMetaClass2_broken.this.expandoMethods.put(getterKey, getter);
                WillsMetaClass2_broken.this.expandoProperties.put(mbp.getName(), mbp);

                addMetaBeanProperty(mbp);
                performRegistryCallbacks();
            }

            public void doCall() {
                doCall(null);
            }

        });
    }

    @Override
    public Object invokeConstructor(Object[] arguments) {

        // TODO This is the only area where this MetaClass needs to do some interception because Groovy's current
        // MetaClass uses hard coded references to the java.lang.reflect.Constructor class so you can't simply
        // inject Constructor like you can do properties, methods and fields. When Groovy's MetaClassImpl is
        // refactored we can fix this
        Class[] argClasses = MetaClassHelper.convertToTypeArray(arguments);
        MetaMethod method = pickMethod(GROOVY_CONSTRUCTOR, argClasses);
        if (method != null && method.getParameterTypes().length == arguments.length) {
            return method.invoke(getTheClass(), arguments);
        }

        return super.invokeConstructor(arguments);
    }

    @Override
    public MetaClass getMetaClass() {
        return myMetaClass;
    }

    @Override
    public Object getProperty(String property) {
        if (isValidExpandoProperty(property)) {
            if (property.equals(STATIC_QUALIFIER)) {
                return new ExpandoMetaClass.ExpandoMetaProperty(property, true);
            } else if (property.equals(CONSTRUCTOR)) {
                return new ExpandoMetaClass.ExpandoMetaConstructor();
            } else {
                if (myMetaClass.hasProperty(this, property) == null)
                    return new ExpandoMetaClass.ExpandoMetaProperty(property);
                else return myMetaClass.getProperty(this, property);
            }

        } else {
            return myMetaClass.getProperty(this, property);
        }

    }

    public static boolean isValidExpandoProperty(String property) {
        return !(property.equals(META_CLASS) || property.equals(CLASS) || property.equals(META_METHODS) || property.equals(METHODS) || property.equals(PROPERTIES));
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        final Object[] argsArr = args instanceof Object[] ? (Object[]) args : Object[]::new;

        def myMc = WillsMetaClass2_broken  //get metaClass of this WillsMetaClass2
        List listMM = myMc.getMethods().collect{"${it.name},${it.parameterTypes}"}
        def myMM = myMc.getMethods().find{it.name == name}
        if (myMM != null) {
            // we have to use doMethodInvoke here instead of simply invoke,
            // because getMetaMethod may provide a method that can not be called
            // without further argument transformation, which is done only in
            // doMethodInvoke
            myMM.doMethodInvoke(this, *argsArr)
        } else {

            def classArgs = args.collect { it.getClass() } as Class[]
            //def altMM = myMc.pickMethod (name, (Class[])classArgs)
            //def altMM = myMc.getMetaMethods().find{it.name == name}
            MetaMethod metaMethod = myMc.getMetaMethod(name, argsArr);
            if (metaMethod != null) {
                // we have to use doMethodInvoke here instead of simply invoke,
                // because getMetaMethod may provide a method that can not be called
                // without further argument transformation, which is done only in
                // doMethodInvoke
                return metaMethod.doMethodInvoke(this, argsArr);
            }
        }


        if (argsArr.length == 2 && argsArr[0] instanceof Class && argsArr[1] instanceof Closure) {
            if (argsArr[0].equals(getTheClass())) registerInstanceMethod(name, (Closure) argsArr[1]);
            else {
                registerSubclassInstanceMethod(name, (Class) argsArr[0], (Closure) argsArr[1]);
            }

            return null;
        }


        if (argsArr.length == 1 && argsArr[0] instanceof Closure) {
            registerInstanceMethod(name, (Closure) argsArr[0]);
            return null;
        }


        throw new MissingMethodException(name, getClass(), argsArr);
    }

    @Override
    public void setMetaClass(MetaClass metaClass) {
        this.myMetaClass = metaClass;
    }

    @Override
    public void setProperty(String property, Object newValue) {
        if (newValue instanceof Closure) {
            if (property.equals(CONSTRUCTOR)) {
                property = GROOVY_CONSTRUCTOR;
            }

            Closure callable = (Closure) newValue;
            final List<MetaMethod> list = ClosureMetaMethod.createMethodList(property, getTheClass(), callable);
            for (MetaMethod method : list) {
                // here we don't care if the method exists or not we assume the
                // developer is responsible and wants to override methods where necessary
                registerInstanceMethod(method);
            }

        } else {
            //manage dynamic dispatch to get expected result
            registerBeanProperty(property, newValue);
        }

    }

    public WillsMetaClass2_broken define(@DelegatesTo(value = ExpandoMetaClass.DefiningClosure.class, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        final DefiningClosure definer = (DefiningClosure) new ExpandoMetaClass.DefiningClosure();
        Object delegate = closure.getDelegate();
        closure.setDelegate(definer);
        closure.setResolveStrategy(Closure.DELEGATE_ONLY);
        closure.call((Object) null);
        closure.setDelegate(delegate);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        definer.setDefinition(false);
        return this;
    }

    protected synchronized void performOperationOnMetaClass(Callable c) {
        try {
            writeLock.lock();
            if (allowChangesAfterInit) {
                setInitialized(false);
            }

            c.call();
        } finally {
            if (initCalled) {
                setInitialized(true);
            }

            // downgrade to readlock before releasing just in case
            readLock.lock();
            writeLock.unlock();
            readLock.unlock();
        }

    }

    @Override
    protected void checkInitalised() {
        try {
            readLock.lock();
            super.checkInitalised();
        } finally {
            readLock.unlock();
        }

    }

    /**
     * Registers a new bean property
     *
     * @param property The property name
     * @param newValue The properties initial value (removed Object to make dyna,mic dispatch work in groovy)
     */
    public void registerBeanProperty(final String property, newValue) {

        println "called registerbeanproperty with $property and value $newValue"

        performOperationOnMetaClass(() -> {
            Class type = newValue == null ? Object.class : newValue.getClass();

            MetaBeanProperty mbp = newValue instanceof MetaBeanProperty ? (MetaBeanProperty) newValue : new ThreadManagedMetaBeanProperty(theClass, property, type, newValue);

            final MetaMethod getter = mbp.getGetter();
            final MethodKey getterKey = new DefaultCachedMethodKey(theClass, getter.getName(), CachedClass.EMPTY_ARRAY, false);
            final MetaMethod setter = mbp.getSetter();
            final MethodKey setterKey = new DefaultCachedMethodKey(theClass, setter.getName(), setter.getParameterTypes(), false);
            addMetaMethod(getter);
            addMetaMethod(setter);

            expandoMethods.put(setterKey, setter);
            expandoMethods.put(getterKey, getter);
            expandoProperties.put(mbp.getName(), mbp);

            addMetaBeanProperty(mbp);
            performRegistryCallbacks();
        });
    }

    /**
     * Registers a new instance method for the given method name and closure on this MetaClass
     *
     * @param metaMethod
     */
    public void registerInstanceMethod(final MetaMethod metaMethod) {
        final boolean inited = this.initCalled;
        performOperationOnMetaClass(() -> {
            String methodName = metaMethod.getName();
            checkIfGroovyObjectMethod(metaMethod);
            MethodKey key = new DefaultCachedMethodKey(theClass, methodName, metaMethod.getParameterTypes(), false);

            if (isInitialized()) {
                throw new RuntimeException("Already initialized, cannot add new method: " + metaMethod);
            }
            // we always adds meta methods to class itself
            addMetaMethodToIndex(metaMethod, metaMethodIndex.getHeader(theClass));

            dropMethodCache(methodName);
            expandoMethods.put(key, metaMethod);

            if (inited && isGetter(methodName, metaMethod.getParameterTypes())) {
                String propertyName = getPropertyForGetter(methodName);
                registerBeanPropertyForMethod(metaMethod, propertyName, true, false);

            } else if (inited && isSetter(methodName, metaMethod.getParameterTypes())) {
                String propertyName = getPropertyForSetter(methodName);
                registerBeanPropertyForMethod(metaMethod, propertyName, false, false);
            }
            performRegistryCallbacks();
        });
    }

    public void registerInstanceMethod(String name, Closure closure) {
        if (name.equals(CONSTRUCTOR)) {
            name = GROOVY_CONSTRUCTOR;
        }

        final List<MetaMethod> list = ClosureMetaMethod.createMethodList(name, getTheClass(), closure);
        for (MetaMethod method : list) {
            registerInstanceMethod(method);
        }

    }

    /**
     * Overrides the behavior of parent getMethods() method to make MetaClass aware of added Expando methods
     *
     * @return A list of MetaMethods
     * @see MetaObjectProtocol#getMethods()
     */
    @Override
    public List<MetaMethod> getMethods() {
        List<MetaMethod> methodList = new ArrayList<MetaMethod>();
        methodList.addAll(this.expandoMethods.values());
        methodList.addAll(super.getMethods());
        return methodList;
    }

    @Override
    public List<MetaProperty> getProperties() {
        List<MetaProperty> propertyList = new ArrayList<MetaProperty>(super.getProperties());
        return propertyList;
    }

    private void performRegistryCallbacks() {
        MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
        incVersion();
        if (!modified) {
            modified = true;
            // Implementation note: By default Groovy uses soft references to store MetaClass
            // this insures the registry doesn't grow and get out of hand. By doing this we're
            // saying this this EMC will be a hard reference in the registry. As we're only
            // going have a small number of classes that have modified EMC this is ok
            if (inRegistry) {
                MetaClass currMetaClass = registry.getMetaClass(getTheClass());
                if (!(currMetaClass instanceof ExpandoMetaClass) && currMetaClass instanceof AdaptingMetaClass) {
                    ((AdaptingMetaClass) currMetaClass).setAdaptee(this);
                } else {
                    registry.setMetaClass(getTheClass(), this);
                }

            }


        }

    }

    private void registerBeanPropertyForMethod(MetaMethod metaMethod, String propertyName, boolean getter, boolean isStatic) {
        Map<String, MetaProperty> propertyCache = isStatic ? staticBeanPropertyCache : beanPropertyCache;
        MetaBeanProperty beanProperty = (MetaBeanProperty) propertyCache.get(propertyName);
        if (beanProperty == null) {
            MetaProperty metaProperty = super.getMetaProperty(propertyName);
            if (metaProperty instanceof MetaBeanProperty) {
                boolean staticProp = Modifier.isStatic(metaProperty.getModifiers());
                if (isStatic.equals(staticProp)) {
                    beanProperty = (MetaBeanProperty) metaProperty;
                }

            }

        }

        if (beanProperty == null) {
            if (getter) beanProperty = new MetaBeanProperty(propertyName, Object.class, metaMethod, null);
            else beanProperty = new MetaBeanProperty(propertyName, Object.class, null, metaMethod);

            propertyCache.put(propertyName, beanProperty);
        } else {
            if (getter) {
                MetaMethod setterMethod = beanProperty.getSetter();
                Class type = setterMethod != null ? setterMethod.getParameterTypes()[0].getTheClass() : Object.class;
                beanProperty = new MetaBeanProperty(propertyName, type, metaMethod, setterMethod);
                propertyCache.put(propertyName, beanProperty);
            } else {
                MetaMethod getterMethod = beanProperty.getGetter();
                beanProperty = new MetaBeanProperty(propertyName, metaMethod.getParameterTypes()[0].getTheClass(), getterMethod, metaMethod);
                propertyCache.put(propertyName, beanProperty);
            }

        }

        expandoProperties.put(beanProperty.getName(), beanProperty);
        addMetaBeanProperty(beanProperty);
    }

    protected void registerStaticMethod(final String name, final Closure callable) {
        registerStaticMethod(name, callable, null);
    }

    /**
     * Registers a new static method for the given method name and closure on this MetaClass
     *
     * @param name     The method name
     * @param callable The callable Closure
     */
    protected void registerStaticMethod(final String name, final Closure callable, final Class[] paramTypes) {
        performOperationOnMetaClass();
    }

    @Override
    protected Object getSubclassMetaMethods(String methodName) {
        if (!isModified()) return null;

        return expandoSubclassMethods.get(methodName);
    }

    /**
     * @return The Java class enhanced by this MetaClass
     */
    public Class getJavaClass() {
        return getTheClass();
    }

    /**
     * Called from ExpandoMetaClassCreationHandle in the registry if it exists to
     * set up inheritance handling
     *
     * @param modifiedSuperExpandos A list of modified super ExpandoMetaClass
     */
    public void refreshInheritedMethods(Set modifiedSuperExpandos) {
        for (Object modifiedSuperExpando : modifiedSuperExpandos) {
            WillsMetaClass2_broken superExpando = (WillsMetaClass2_broken) modifiedSuperExpando;
            if (!superExpando.equals(this)) {
                localRefreshInheritedMethods(superExpando);
            }

        }

    }

    private void localRefreshInheritedMethods(WillsMetaClass2_broken superExpando) {
        List<MetaMethod> metaMethods = superExpando.getExpandoMethods();
        for (MetaMethod metaMethod : metaMethods) {
            if (metaMethod.isStatic()) {
                if (!superExpando.getTheClass().equals(getTheClass()))
                    continue;// don't inherit static methods except our own
                registerStaticMethod(metaMethod.getName(), (Closure) ((ClosureStaticMetaMethod) metaMethod).getClosure().clone());
            } else addSuperMethodIfNotOverridden(metaMethod);
        }

        Collection<MetaProperty> metaProperties = superExpando.getExpandoProperties();
        for (Object metaProperty : metaProperties) {
            MetaBeanProperty property = (MetaBeanProperty) metaProperty;
            expandoProperties.put(property.getName(), property);
            addMetaBeanProperty(property);
        }

    }

    /**
     * Returns a list of expando MetaMethod instances added to this ExpandoMetaClass
     *
     * @return the expandoMethods
     */
    public List<MetaMethod> getExpandoMethods() {
        return Collections.unmodifiableList(DefaultGroovyMethods.toList(expandoMethods.values()));
    }

    /**
     * Returns a list of MetaBeanProperty instances added to this ExpandoMetaClass
     *
     * @return the expandoProperties
     */
    public Collection<MetaProperty> getExpandoProperties() {
        return Collections.unmodifiableCollection(expandoProperties.values());
    }

    /**
     * Overrides default implementation just in case invokeMethod has been overridden by ExpandoMetaClass
     *
     * @see MetaClassImpl#invokeMethod(Class, Object, String, Object[], boolean, boolean)
     */
    @Override
    public Object invokeMethod(Class sender, Object object, String methodName, Object[] originalArguments, boolean isCallToSuper, boolean fromInsideClass) {
        if (invokeMethodMethod != null) {
            MetaClassHelper.unwrap(originalArguments);
            return invokeMethodMethod.invoke(object, Object[]::new);
        }

        return super.invokeMethod(sender, object, methodName, originalArguments, isCallToSuper, fromInsideClass);
    }

    /**
     * Overrides default implementation just in case a static invoke method has been set on ExpandoMetaClass
     *
     * @see MetaClassImpl#invokeStaticMethod(Object, String, Object[])
     */
    @Override
    public Object invokeStaticMethod(Object object, String methodName, Object[] arguments) {
        if (invokeStaticMethodMethod != null) {
            MetaClassHelper.unwrap(arguments);
            return invokeStaticMethodMethod.invoke(object, Object[]::new);
        }

        return super.invokeStaticMethod(object, methodName, arguments);
    }

    /**
     * Overrides default implementation just in case getProperty method has been overridden by ExpandoMetaClass
     *
     * @see MetaClassImpl#getProperty(Class, Object, String, boolean, boolean)
     */
    @Override
    public Object getProperty(Class sender, Object object, String name, boolean useSuper, boolean fromInsideClass) {
        if (hasOverrideGetProperty(name) && getJavaClass().isInstance(object)) {
            return getPropertyMethod.invoke(object, Object[]::new);
        }


        if ("mixedIn".equals(name)) {
            return new ExpandoMetaClass.MixedInAccessor(object, mixinClasses);
        }


        return super.getProperty(sender, object, name, useSuper, fromInsideClass);
    }

    /**
     * Overrides default implementation just in case getProperty method has been overridden by ExpandoMetaClass
     *
     * @see MetaClassImpl#getProperty(Object, String)
     */
    @Override
    public Object getProperty(Object object, String name) {
        if (hasOverrideGetProperty(name) && getJavaClass().isInstance(object)) {
            return getPropertyMethod.invoke(object, Object[]::new);
        }

        return super.getProperty(object, name);
    }

    private boolean hasOverrideGetProperty(String name) {
        return getPropertyMethod != null && !name.equals(META_CLASS_PROPERTY) && !name.equals(CLASS_PROPERTY);
    }

    /**
     * Overrides default implementation just in case setProperty method has been overridden by ExpandoMetaClass
     *
     * @see MetaClassImpl#setProperty(Class, Object, String, Object, boolean, boolean)
     */
    @Override
    public void setProperty(Class sender, Object object, String name, Object newValue, boolean useSuper, boolean fromInsideClass) {
        if (setPropertyMethod != null && !name.equals(META_CLASS_PROPERTY) && getJavaClass().isInstance(object)) {
            setPropertyMethod.invoke(object, Object[]::new);
            return;

        }

        super.setProperty(sender, object, name, newValue, useSuper, fromInsideClass);
    }

    /**
     * Looks up an existing MetaProperty by name
     *
     * @param name The name of the MetaProperty
     * @return The MetaProperty or null if it doesn't exist
     */
    @Override
    public MetaProperty getMetaProperty(String name) {
        MetaProperty mp = this.expandoProperties.get(name);
        if (mp != null) return mp;
        return super.getMetaProperty(name);
    }

    /**
     * Returns true if the MetaClass has the given property
     *
     * @param name The name of the MetaProperty
     * @return True it exists as a MetaProperty
     */
    public boolean hasMetaProperty(String name) {
        return getMetaProperty(name) != null;
    }

    /**
     * Checks whether a MetaMethod for the given name and arguments exists
     *
     * @param name The name of the MetaMethod
     * @param args The arguments to the meta method
     * @return True if the method exists otherwise null
     */
    public boolean hasMetaMethod(String name, Class[] args) {
        return super.pickMethod(name, args) != null;
    }

    /**
     * Determine if this method name suffix is a legitimate bean property name.
     * Either the first or second letter must be upperCase for that to be true.
     */
    private static boolean isPropertyName(String name) {
        return ((name.length() > 0) && Character.isUpperCase(name.charAt(0))) || ((name.length() > 1) && Character.isUpperCase(name.charAt(1)));
    }

    /**
     * Returns true if the name of the method specified and the number of arguments make it a javabean property
     *
     * @param name True if its a Javabean property
     * @param args The arguments
     * @return True if it is a javabean property method
     */
    private boolean isGetter(String name, CachedClass[] args) {
        if (name == null || name.length() == 0 || args == null) return false;
        if (args.length != 0) return false;

        if (name.startsWith("get")) {
            name = name.substring(3);
            return isPropertyName(name);
        }

        if (name.startsWith("is")) {
            name = name.substring(2);
            return isPropertyName(name);
        }

        return false;
    }

    /**
     * Returns a property name equivalent for the given getter name or null if it is not a getter
     *
     * @param getterName The getter name
     * @return The property name equivalent
     */
    private String getPropertyForGetter(String getterName) {
        if (getterName == null || getterName.length() == 0) return null;

        if (getterName.startsWith("get")) {
            String prop = getterName.substring(3);
            return MetaClassHelper.convertPropertyName(prop);
        }

        if (getterName.startsWith("is")) {
            String prop = getterName.substring(2);
            return MetaClassHelper.convertPropertyName(prop);
        }

        return null;
    }

    /**
     * Returns a property name equivalent for the given setter name or null if it is not a getter
     *
     * @param setterName The setter name
     * @return The property name equivalent
     */
    public String getPropertyForSetter(String setterName) {
        if (setterName == null || setterName.length() == 0) return null;

        if (setterName.startsWith("set")) {
            String prop = setterName.substring(3);
            return MetaClassHelper.convertPropertyName(prop);
        }

        return null;
    }

    public boolean isSetter(String name, CachedClass[] args) {
        if (name == null || name.length() == 0 || args == null) return false;

        if (name.startsWith("set")) {
            if (args.length != 1) return false;
            name = name.substring(3);
            return isPropertyName(name);
        }


        return false;
    }

    @Override
    public CallSite createPojoCallSite(CallSite site, Object receiver, Object[] args) {
        if (invokeMethodMethod != null) return new PojoMetaClassSite(site, this);

        return super.createPojoCallSite(site, receiver, args);
    }

    @Override
    public CallSite createStaticSite(CallSite site, Object[] args) {
        if (invokeStaticMethodMethod != null) return new StaticMetaClassSite(site, this);

        return super.createStaticSite(site, args);
    }

    @Override
    public boolean hasCustomStaticInvokeMethod() {
        return invokeStaticMethodMethod != null;
    }

    @Override
    public CallSite createPogoCallSite(CallSite site, Object[] args) {
        if (invokeMethodMethod != null) return new PogoMetaClassSite(site, this);
        return super.createPogoCallSite(site, args);
    }

    public CallSite createPogoCallCurrentSite(CallSite site, Class sender, String name, Object[] args) {
        if (invokeMethodMethod != null) return new PogoMetaClassSite(site, this);
        return super.createPogoCallCurrentSite(site, sender, args);
    }

    @Override
    public MetaMethod retrieveConstructor(Object[] args) {
        Class[] params = MetaClassHelper.convertToTypeArray(args);
        MetaMethod method = pickMethod(GROOVY_CONSTRUCTOR, params);
        if (method != null) return method;
        return super.retrieveConstructor(args);
    }

    @Override
    public CallSite createConstructorSite(CallSite site, Object[] args) {
        Class[] params = MetaClassHelper.convertToTypeArray(args);
        MetaMethod method = pickMethod(GROOVY_CONSTRUCTOR, params);
        if (method != null && method.getParameterTypes().length == args.length) {
            if (method.getDeclaringClass().getTheClass().equals(getTheClass())) {
                return new ConstructorMetaMethodSite(site, this, method, params);
            }

        }


        return super.createConstructorSite(site, args);
    }

     /**
     * Instances of this class are returned when using the {@code <<} left shift operator.
     * <p>
     * Example:
     * <p>
     * metaClass.myMethod {@code <<} { String args {@code ->} }
     * <p>
     * This allows callbacks to the ExpandoMetaClass for registering appending methods
     */
    protected class WillsExpandoMetaProperty extends GroovyObjectSupport implements GroovyObject {
        protected WillsExpandoMetaProperty(WillsMetaClass2_broken enclosing, String name) {
            this(name, false);
        }

        protected WillsExpandoMetaProperty(WillsMetaClass2_broken enclosing, String name, boolean isStatic) {
            this.propertyName = name;
            this.isStatic = isStatic;
        }

        public String getPropertyName() {
            return this.propertyName;
        }

        public boolean isStatic() {
            return this.isStatic;
        }

        public Object leftShift(Object arg) {
            registerIfClosure(arg, false);
            return this;
        }

        private void registerIfClosure(Object arg, boolean replace) {
            if (arg instanceof Closure) {
                if (propertyName.equals(CONSTRUCTOR)) {
                    propertyName = GROOVY_CONSTRUCTOR;
                }

                Closure callable = (Closure) arg;
                final List<MetaMethod> list = ClosureMetaMethod.createMethodList(propertyName, getTheClass(), callable);
                if (list.isEmpty() && this.isStatic) {
                    Class[] paramTypes = callable.getParameterTypes();
                    registerStatic(callable, replace, paramTypes);
                    return;

                }

                for (MetaMethod method : list) {
                    Class[] paramTypes = method.getNativeParameterTypes();
                    if (this.isStatic) {
                        registerStatic(callable, replace, paramTypes);
                    } else {
                        registerInstance(method, replace, paramTypes);
                    }

                }

            }

        }

        private void registerStatic(Closure callable, boolean replace, Class[] paramTypes) {
            Method foundMethod = checkIfMethodExists(getTheClass(), propertyName, paramTypes, true);
            if (foundMethod != null && !replace)
                throw new GroovyRuntimeException("Cannot add new static method [" + propertyName + "] for arguments [" + DefaultGroovyMethods.inspect(paramTypes) + "]. It already exists!");
            registerStaticMethod(propertyName, callable, paramTypes);
        }

        private void registerInstance(MetaMethod method, boolean replace, Class[] paramTypes) {
            Method foundMethod = checkIfMethodExists(getTheClass(), propertyName, paramTypes, false);
            if (foundMethod != null && !replace)
                throw new GroovyRuntimeException("Cannot add new method [" + propertyName + "] for arguments [" + DefaultGroovyMethods.inspect(paramTypes) + "]. It already exists!");
            registerInstanceMethod(method);
        }

        private Method checkIfMethodExists(Class methodClass, String methodName, Class[] paramTypes, boolean staticMethod) {
            Method foundMethod = null;
            Method[] methods = methodClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && Modifier.isStatic(method.getModifiers()).equals(staticMethod)) {
                    if (MetaClassHelper.parametersAreCompatible(paramTypes, method.getParameterTypes())) {
                        foundMethod = method;
                        break;
                    }

                }

            }

            return foundMethod;
        }

        @Override
        public Object getProperty(String property) {
            //todo remove debug
            DefaultGroovyMethods.println(this, "get property (" + property + ", " + DefaultGroovyMethods.invokeMethod(String.class, "valueOf", new Object[]{getProperty("newValue")}) + ") called on WillsExpandoProperty ");

            this.propertyName = property;

            //added this to try and access the property
            MetaProperty value = WillsMetaClass2_broken.this.expandoProperties.get(property);

            if (DefaultGroovyMethods.asBoolean(value)) return value;
            else return this;
        }

        @Override
        public void setProperty(String property, Object newValue) {
            //todo remove debug
            DefaultGroovyMethods.println(this, "set property (" + property + ", " + String.valueOf(newValue) + ") called on WillsExpandoProperty ");

            this.propertyName = property;

            //new code - register if closure, else regsiter static MetaBeanProperty
            if (newValue instanceof Closure) {
                registerIfClosure(newValue, true);

            } else {
                registerStaticBeanProperty(property, newValue);
            }


        }

        protected String propertyName;
        protected boolean isStatic;
    }

    /**
     * Handles the ability to use the left shift operator to append new constructors
     */
    protected class ExpandoMetaConstructor extends GroovyObjectSupport {
        public Object leftShift(Closure c) {
            if (c != null) {
                final List<MetaMethod> list = ClosureMetaMethod.createMethodList(GROOVY_CONSTRUCTOR, getTheClass(), c);
                for (MetaMethod method : list) {
                    Class[] paramTypes = method.getNativeParameterTypes();
                    Constructor ctor = retrieveConstructor(paramTypes);
                    if (ctor != null)
                        throw new GroovyRuntimeException("Cannot add new constructor for arguments [" + DefaultGroovyMethods.inspect(paramTypes) + "]. It already exists!");

                    registerInstanceMethod(method);
                }

            }


            return this;
        }

    }

    private class SubClassDefiningClosure extends GroovyObjectSupport implements GroovyObject {
        public SubClassDefiningClosure(WillsMetaClass2_broken enclosing, Class klazz) {
            this.klazz = klazz;
        }

        @Override
        public Object invokeMethod(String name, Object obj) {
            if (obj instanceof Object[]) {
                Object[] args = (Object[]) obj;
                if (args.length == 1 && args[0] instanceof Closure) {
                    registerSubclassInstanceMethod(name, klazz, (Closure) args[0]);
                    return null;
                }

            }


            throw new MissingMethodException(name, getClass(), Object[]::new);
        }

        private final Class klazz;
    }

    private class DefiningClosure extends GroovyObjectSupport implements GroovyObject {
        public void mixin(Class category) {
            mixin(Collections.singletonList(category));
        }

        public void mixin(List categories) {
            DefaultGroovyMethods.mixin(WillsMetaClass2_broken.this, categories);
        }

        public void mixin(Class[] categories) {
            DefaultGroovyMethods.mixin(WillsMetaClass2_broken.this, categories);
        }

        public void define(Class subClass, Closure closure) {
            final GroovyObjectSupport definer = new ExpandoMetaClass.SubClassDefiningClosure(subClass);
            closure.setDelegate(definer);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            closure.call((Object) null);
        }

        @Override
        public Object invokeMethod(String name, Object obj) {
            try {
                return getMetaClass().invokeMethod(this, name, obj);
            } catch (MissingMethodException mme) {
                if (obj instanceof Object[]) {
                    if (STATIC_QUALIFIER.equals(name)) {
                        final GroovyObjectSupport staticDef = new ExpandoMetaClass.StaticDefiningClosure();
                        Closure c = (Closure) ((Object[]) obj)[0];
                        c.setDelegate(staticDef);
                        c.setResolveStrategy(Closure.DELEGATE_ONLY);
                        c.call((Object) null);
                        return null;
                    }

                    Object[] args = (Object[]) obj;
                    if (args.length == 1 && args[0] instanceof Closure) {
                        registerInstanceMethod(name, (Closure) args[0]);
                    } else if (args.length == 2 && args[0] instanceof Class && args[1] instanceof Closure)
                        registerSubclassInstanceMethod(name, (Class) args[0], (Closure) args[1]);
                    else WillsMetaClass2_broken.this.setProperty(name, ((Object[]) obj)[0]);

                    return null;
                }


                throw mme;
            }

        }

        @Override
        public void setProperty(String property, Object newValue) {
            WillsMetaClass2_broken.this.setProperty(property, newValue);
        }

        @Override
        public Object getProperty(String property) {
            if (STATIC_QUALIFIER.equals(property)) return new ExpandoMetaClass.StaticDefiningClosure();

            if (definition) return new ExpandoMetaClass.ExpandoMetaProperty(property);
            else throw new MissingPropertyException(property, getClass());
        }

        public boolean getDefinition() {
            return definition;
        }

        public boolean isDefinition() {
            return definition;
        }

        public void setDefinition(boolean definition) {
            this.definition = definition;
        }

        private boolean definition = true;
    }

    private class StaticDefiningClosure extends GroovyObjectSupport implements GroovyObject {
        protected StaticDefiningClosure(WillsMetaClass2_broken enclosing) {
            super(STATIC_QUALIFIER, true);
        }

        @Override
        public Object invokeMethod(String name, Object obj) {
            if (obj instanceof Object[]) {
                final Object[] args = (Object[]) obj;
                if (args.length == 1 && args[0] instanceof Closure) {
                    registerStaticMethod(name, (Closure) args[0]);
                    return null;
                }

            }


            throw new MissingMethodException(name, getClass(), obj instanceof Object[] ? (Object[]) obj : Object[]::new);
        }

    }

    private static class MixedInAccessor {
        public MixedInAccessor(Object object, Set<MixinInMetaClass> mixinClasses) {
            this.object = object;
            this.mixinClasses = mixinClasses;
        }

        public Object getAt(Class key) {
            if (key.isAssignableFrom(object.getClass())) {
                return new GroovyObjectSupport() {
                };
            }


            for (final MixinInMetaClass mixin : mixinClasses) {
                if (key.isAssignableFrom(mixin.getMixinClass().getTheClass())) {
                    return new GroovyObjectSupport() {
                    };
                }

            }


            throw new RuntimeException("Class " + key + " isn't mixed in " + object.getClass());
        }

        public void putAt(Class key, Object value) {
            for (MixinInMetaClass mixin : mixinClasses)
                if (mixin.getMixinClass().getTheClass().equals(key)) {
                    mixin.setMixinInstance(object, value);
                    return;

                }


            throw new RuntimeException("Class " + key + " isn't mixed in " + object.getClass());
        }

        private final Object object;
        private final Set<MixinInMetaClass> mixinClasses;
    }
}
