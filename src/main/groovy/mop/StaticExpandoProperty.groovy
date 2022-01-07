package mop

import org.apache.groovy.util.concurrent.ManagedIdentityConcurrentMap
import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.ReflectionCache
import org.codehaus.groovy.util.ReferenceBundle

import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap


/**
 * near duplicate of ThreadBoundMetabeanProperty
 * except it makes the setters and getters by default as public, static
 */
public class StaticExpandoProperty extends MetaBeanProperty {
    private static final ConcurrentHashMap<String, ManagedIdentityConcurrentMap> PROPNAME_TO_MAP = new ConcurrentHashMap<String, ManagedIdentityConcurrentMap>();

    private final ManagedIdentityConcurrentMap instance2Prop;

    private final Class declaringClass;
    private final ThreadBoundGetter getter;
    private final ThreadBoundSetter setter;
    private Object initialValue;
    private Closure initialValueCreator;

    private static final ReferenceBundle SOFT_BUNDLE = ReferenceBundle.getSoftBundle();

    /**
     * Retrieves the initial value of the ThreadBound property
     *
     * @return The initial value
     */
    public synchronized Object getInitialValue() {
        return getInitialValue(null);
    }

    public synchronized Object getInitialValue(Object object) {
        if (initialValueCreator != null) {
            return initialValueCreator.call(object);
        }
        return initialValue;

    }

    /**
     * Closure responsible for creating the initial value of thread-managed bean properties
     *
     * @param callable The closure responsible for creating the initial value
     */
    public void setInitialValueCreator(Closure callable) {
        this.initialValueCreator = callable;
    }

    /**
     * Constructs a new ThreadManagedBeanProperty for the given arguments
     *
     * @param declaringClass The class that declares the property
     * @param name           The name of the property
     * @param type           The type of the property
     * @param iv             The properties initial value
     */
    public StaticExpandoProperty(Class declaringClass, String name, Class type, Object iv) {
        super(name, type, null, null);
        this.type = type;
        this.declaringClass = declaringClass;

        this.getter = new ThreadBoundGetter(name);
        this.setter = new ThreadBoundSetter(name);
        initialValue = iv;

        instance2Prop = getInstance2PropName(name);
    }

    /**
     * Constructs a new ThreadManagedBeanProperty for the given arguments
     *
     * @param declaringClass      The class that declares the property
     * @param name                The name of the property
     * @param type                The type of the property
     * @param initialValueCreator The closure responsible for creating the initial value
     */
    public StaticExpandoProperty(Class declaringClass, String name, Class type, Closure initialValueCreator) {
        super(name, type, null, null);
        this.type = type;
        this.declaringClass = declaringClass;

        this.getter = new ThreadBoundGetter(name);
        this.setter = new ThreadBoundSetter(name);
        this.initialValueCreator = initialValueCreator;

        instance2Prop = getInstance2PropName(name);
    }

    private static ManagedIdentityConcurrentMap getInstance2PropName(String name) {
        ManagedIdentityConcurrentMap res = PROPNAME_TO_MAP.get(name);
        if (res == null) {
            res = new ManagedIdentityConcurrentMap(ManagedIdentityConcurrentMap.ReferenceType.SOFT);
            ManagedIdentityConcurrentMap ores = PROPNAME_TO_MAP.putIfAbsent(name, res);
            if (ores != null)
                return ores;
        }
        return res;
    }

    /* (non-Javadoc)
      * @see groovy.lang.MetaBeanProperty#getGetter()
      */
    @Override
    public MetaMethod getGetter() {
        return this.getter;
    }

    /* (non-Javadoc)
      * @see groovy.lang.MetaBeanProperty#getSetter()
      */
    @Override
    public MetaMethod getSetter() {
        return this.setter;
    }


    /**
     * Accesses the ThreadBound state of the property as a getter
     */
    class ThreadBoundGetter extends MetaMethod {
        private final String name;
        int modifier

        public ThreadBoundGetter(String name, int modifier = (Modifier.STATIC|Modifier.PUBLIC)) {
            setParametersTypes(CachedClass.EMPTY_ARRAY);
            this.name = getGetterName(name, type);
            this.modifier = modifier
        }


        @Override
        public int getModifiers() {
            return modifier
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class getReturnType() {
            return type;
        }

        @Override
        public CachedClass getDeclaringClass() {
            Class declaringClazz = StaticExpandoProperty.this.declaringClass

            return ReflectionCache.getCachedClass(declaringClazz);
        }

        /* (non-Javadoc)
           * @see groovy.lang.MetaMethod#invoke(java.lang.Object, java.lang.Object[])
           */
        @Override
        public Object invoke(Object object, Object[] arguments) {
            return instance2Prop.getOrPut(object, getInitialValue());
        }
    }

    /**
     * Sets the ThreadBound state of the property like a setter
     */
    private class ThreadBoundSetter extends MetaMethod {
        private final String name;
        private int modifier

        public ThreadBoundSetter(String name, int modifier = (Modifier.STATIC|Modifier.PUBLIC)) {
            setParametersTypes (new CachedClass [] {ReflectionCache.getCachedClass(type)} );
            this.name = getSetterName(name);
            this.modifier = modifier
        }


        @Override
        public int getModifiers() {
            return modifier
        }

        /* (non-Javadoc)
         * @see groovy.lang.MetaMethod#getName()
         */

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class getReturnType() {
            return type;
        }

        @Override
        public CachedClass getDeclaringClass() {
            Class declaringClazz = StaticExpandoProperty.this.declaringClass

            return ReflectionCache.getCachedClass(declaringClazz)
        }

        /* (non-Javadoc)
           * @see groovy.lang.MetaMethod#invoke(java.lang.Object, java.lang.Object[])
           */
        @Override
        public Object invoke(Object object, Object[] arguments) {
            instance2Prop.put(object, arguments[0]);
            return null;
        }
    }
}

