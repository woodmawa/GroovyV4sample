package mop


import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.runtime.DefaultCachedMethodKey
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.runtime.MethodKey

import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap

class WillsMetaClass extends ExpandoMetaClass {

    protected final Map<MethodKey, MetaMethod> expandoMethods = new ConcurrentHashMap<MethodKey, MetaMethod>(16, 0.75f, 1);
    protected final Map<String, MetaProperty> expandoProperties = new ConcurrentHashMap<String, MetaProperty>(16, 0.75f, 1);

    WillsMetaClass(Class theClass, boolean register, boolean allowChangesAfterInit, MetaMethod[] add) {
        super(theClass, register, allowChangesAfterInit, add)
    }

    WillsMetaClass(MetaClassRegistry registry, Class theClass, boolean register, boolean allowChangesAfterInit, MetaMethod[] add) {
        super(registry, theClass, register, allowChangesAfterInit, add)
    }

    WillsMetaClass(Class theClass) {
        super(theClass)

        //to get property in metaclass to be detected - have to register it first
        registerBeanProperty("willsMetaClassProperty", "Wills meta class property")

        /* Method method = myClass.getDeclaredMethod('hello')
        MetaMethod metaMethod = myMetaClass.getMetaMethod('hello')
        CachedMethod cachedMethod = new CachedMethod(method)
        cachedMethod */
        //registerInstanceMethod(metaMethod)
    }

    WillsMetaClass(Class theClass, MetaMethod[] add) {
        super(theClass, add)
    }

    WillsMetaClass(Class theClass, boolean register) {
        super(theClass, register)
    }

    WillsMetaClass(Class theClass, boolean register, MetaMethod[] add) {
        super(theClass, register, add)
    }

    WillsMetaClass(Class theClass, boolean register, boolean allowChangesAfterInit) {
        super(theClass, register, allowChangesAfterInit)

        println "WillsMetaClass constructor called  - allow changes after initialise"

        //Class myClass = this.getClass()
        //MetaClass myMetaClass = this.metaClass

    }

    def willsMetaClassProperty = "Wills meta class property"
    static def willsStaticMetaClassProperty = "Wills meta class property"

    def willsMethod () {
        println "hello from wills metaclass "
    }

    def hello() {
        "hello from wills metaclass"
    }

    @Override
    public Object invokeMethod (Object instance, String name, Object args) {
        println "objects invokeMethod is relayed to metaClass, called with $instance, method $name and $args"

        //try super class search first
        if (super.hasMetaMethod(name, args)) {
            MetaMethod mm = getMetaMethod(name, args)
            mm.invoke(this, args)
        } else {
            def result

            MethodClosure mc = this::"$name"
            if (mc == null)
                throw new MissingMethodException(name, instance.getClass(), args)

            if (mc.getMaximumNumberOfParameters() == 0) {
                mc.delegate = instance      //set closure delegate to instance class
                result =  mc ()

            } else {
                result = mc(args) ?: null
            }
            result
        }
    }


    public MetaMethod getMetaMethod (String name, Object args) {
        println "getMethod in metaClass called with $name and $args"

        def result = super.getMetaMethod(name, args)
        if (result == null )
            return this::"$name"
        else
            result
    }

    @Override
    def getProperty (String name) {
        println "getProperty in metaClass called with $name"

        def result
        def instance = this



        if (name == 'static') {
            result = new WillsExpandoMetaProperty( this, 'static', true)
        } else if (name == 'metaClass') {
            result = this
        } else {
            result = super.getProperty(name)
        }

        if (result == null  )
            return this."$name"
        else
            result

        /*if (isValidExpandoProperty(name)) {
            if (name.equals(STATIC_QUALIFIER)) {
                return new WillsExpandoMetaProperty(name, true);
            } else if (name.equals(CONSTRUCTOR)) {
                return new ExpandoMetaConstructor();
            } else {
                if (myMetaClass.hasProperty(this, property) == null)
                    return new WillsExpandoMetaProperty(name);
                else
                    return myMetaClass.getProperty(this, name);
            }
        } else {
            return myMetaClass.getProperty(this, property);
        }*/
    }

    @Override       //groovyObject delegates instance calls to this version  in the metaClass first
    def getProperty (Object instance, String name) {
        println "getProperty in instance class delegated to  metaClass' getProperty ($instance, $name)  "

        def result
        if (name == 'metaClass') {
            return this
        } else {
            result = super.getProperty(instance, name)
            return result
        }

    }

    @Override
    void setProperty (String name, value) {
        def result = super.setProperty(name, value)
        println "setProperty in metaClass $this called with $name, and $value, and registeredBeanProperty(name, value) called in super class "
    }


    protected class WillsExpandoMetaProperty  {

        @Delegate
        ExpandoMetaClass.ExpandoMetaProperty expandoMetaProperty

        protected String propertyName;
        protected boolean isStatic;

        protected WillsExpandoMetaProperty(metaClass, String name) {
            expandoMetaProperty = new ExpandoMetaClass.ExpandoMetaProperty(metaClass, name, false)
            this.propertyName = name;
            this.isStatic = false;
        }

        protected WillsExpandoMetaProperty(metaClass, String name, boolean isStatic) {
            Constructor[] cons =  ExpandoMetaClass.ExpandoMetaProperty.constructors
            expandoMetaProperty = new ExpandoMetaClass.ExpandoMetaProperty(metaClass, name, isStatic)

            this.propertyName = name;
            this.isStatic = isStatic;

        }


        @Override
        public Object getProperty(String property) {
            this.propertyName = property;
            return this;
        }
        /* (non-Javadoc)
           * @see groovy.lang.GroovyObjectSupport#setProperty(java.lang.String, java.lang.Object)
           */

        @Override
        public void setProperty(String property, Object newValue) {
            this.propertyName = property;
            this.isStatic = true

            registerStaticBeanProperty (property, newValue)
            //super.registerIfClosure(newValue, true);
        }


    }

    private void registerStaticBeanProperty (final String property, final Object newValue) {
        performOperationOnMetaClass{
            Class type = newValue == null ? Object.class : newValue.getClass();

            MetaBeanProperty mbp = newValue instanceof MetaBeanProperty ? (MetaBeanProperty) newValue : new StaticExpandoProperty(theClass, property, type, newValue) //ThreadManagedMetaBeanProperty(theClass, property, type, newValue);

            final MetaMethod getter = mbp.getGetter();
            final MethodKey getterKey = new DefaultCachedMethodKey(theClass, getter.getName(), CachedClass.EMPTY_ARRAY, false);
            final MetaMethod setter = mbp.getSetter();
            final MethodKey setterKey = new DefaultCachedMethodKey(theClass, setter.getName(), setter.getParameterTypes(), false);
            super.addMetaMethod(getter);
            super.addMetaMethod(setter);

            getAllExpandoMethods().put(setterKey, setter);
            getAllExpandoMethods().put(getterKey, getter);
            getAllExpandoProperties().put(mbp.getName(), mbp);

            super.addMetaBeanProperty(mbp);
            performRegistryCallbacks();
        }
    }

    protected Map getAllExpandoMethods () {
        Map m = expandoMethods
    }
    protected Map getAllExpandoProperties () {
        Map m = expandoProperties
    }
}
