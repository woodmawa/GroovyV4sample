package MOP

import com.sun.tools.jdi.JDWP
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.reflection.ClassInfo
import org.codehaus.groovy.reflection.ReflectionUtils
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod
import org.codehaus.groovy.runtime.metaclass.ClosureStaticMetaMethod
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

@EqualsAndHashCode (includeFields = true)
@CompileStatic
class WillsExpando {

    static protected Map staticExpandoProperties = new ConcurrentHashMap()
    static protected Map staticExpandoMethods = new ConcurrentHashMap()

    protected Map expandoProperties = new ConcurrentHashMap()
    protected Map expandoMethods = new ConcurrentHashMap()

    WillsExpando () {}

    WillsExpando (Map initialProperties) {
        addProperties(initialProperties)
    }


    StaticContainer getStatic () {
        new StaticContainer()
     }

    static def addStaticProperty (String name , def value) {
        if (value instanceof Closure || value instanceof Callable || value instanceof Function )
            addStaticMethod (name, value )
        else
            staticExpandoProperties.putIfAbsent(name, value)
    }

    static def setStaticProperty (String name , def value) {
        if (value instanceof Closure || value instanceof Callable || value instanceof Function )
            addStaticMethod (name, value )
        else
            staticExpandoProperties.put(name, value)
    }

    static void removeStaticProperty (String name) {
        staticExpandoProperties.remove(name)
    }

    //because this method is defined - it will show when querying metaClass.properties -
    //so need to skip in getStaticProperties () as we want to exclude this from processing
    static def getStaticProperty (String name){
        staticExpandoProperties[name]
    }

    //if used on a class itself we don't know the class or instance so we have to pass as a param
    static Map getStaticProperties (def ofThing) {

        List mps
        if (ofThing instanceof Class<?>) {
            mps = ofThing.metaClass.getProperties().findAll { Modifier.isStatic(it.modifiers) ? it : null } //.collect()
        } else {
            //if instance variable, get the metaClass and get propertes from that
            mps = ofThing.metaClass.getProperties().findAll { Modifier.isStatic(it.modifiers) ? it : null }
        }
        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        Map m1 = [:]
        for (mp in mps) {
            def name = mp.name
            if (name == "properties" || name == "methods" ||name == "staticProperties" || name == "staticMethods")   //skip recursion here
                continue
            def value = mp.getProperty(this)
            m1.put (name, value)
        }

         m1.putAll(staticExpandoProperties)
        m1
    }

    //non static form where we know the instance context class
    Map getStaticProperties () {
        List mps = this.metaClass.getProperties().findAll{Modifier.isStatic (it.modifiers)}.collect()

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        Map m1 = [:]
        for (mp in mps) {
            def name = mp.name
            if (name == "properties" || name == "methods" ||name == "staticProperties" || name == "staticMethods")   //skip recursion here
                continue
            def value = mp.getProperty(this)
            m1.put (name, value)
        }

        //cant seem to add a static property using MOP - so just get the staticExpandoProperties here
        //List l2 = staticExpandoProperties.collect() //.asImmutable()
        //(l1 + l2).asImmutable()
        m1.putAll (staticExpandoProperties)
        m1
    }

    static def addStaticMethod (String name , def value) {
        if (value instanceof Closure || value instanceof Callable || value instanceof Function)
            staticExpandoMethods.put(name, value)
    }

    static void removeStaticMethod (String name) {
        staticExpandoMethods.remove(name)
    }

    static Closure getStaticMethod (String name){
        staticExpandoMethods[name]
    }

    boolean hasStaticExpandoProperty (String name) {
        def propertyExists
        MetaProperty
        if (propertyExists = metaClass.hasProperty(this, name )){
            MetaProperty mp = metaClass.getMetaProperty(name)
            if (Modifier.isStatic(mp.modifiers))
                return true
            else
                return false
        } else {
            staticExpandoProperties[name] ? true : false
        }
     }

    //if used on static class we dont know the context so we have to pass as a param to get the MetaMethods
    static Map  getStaticMethods (ofThing) {
        //todo : not working yet
        List<MetaMethod> mms = ofThing.metaClass.getMetaMethods().findAll{Modifier.isStatic (it.modifiers)}.collect()

        Map m1 = [:]
        for (mm in mms){
            def name = mm.name
            def value = new MethodClosure (WillsExpando,name)
            m1.put (name, value)
        }
        m1.putAll (staticExpandoMethods)
        m1
    }

    //non static form as we know the call instance context here
    Map  getStaticMethods () {
        List<MetaMethod> mms = this.metaClass.getMetaMethods().findAll{Modifier.isStatic (it.modifiers)}.collect()

        Map m1 =[:]
        for (mm in mms){
            def name = mm.name
            def value = new MethodClosure (this.getClass(), name)
            m1.put (name,  value)
        }
        m1.putAll(staticExpandoMethods)
        m1
    }

    //non static form as we know the call instance context here
    List<MetaMethod>  getStaticMetaMethods () {

        List<MetaMethod> mms = this.metaClass.getMetaMethods().findAll{Modifier.isStatic (it.modifiers)}.collect()

        List<MetaMethod> closureMetaMethodList =[]
        for (entry in staticExpandoMethods) {
            closureMetaMethodList.add (new ClosureStaticMetaMethod((String) entry.key, this.getClass(), (Closure) entry.value ) )
        }

        mms.addAll(closureMetaMethodList)
        mms
    }


    def addProperty (String name , def value) {
        if (value instanceof Closure || value instanceof Callable || value instanceof Function )
            addMethod (name, value )
        else
            expandoProperties.put(name, value)
    }

    def addProperties (Map props) {
        props.each {prop, value -> addProperty ((String)prop, value)}
    }

    def removeProperty (String name) {
        expandoProperties.remove(name)
    }

    /*
     * next two methods are delegating proxies for ConcurrentMap when generating MetaProperties for WillsExpando
     * defer to standard props first then look at the private expandoProperties ConcurrentMap, then the staticExpandoProperties
     * so we need to intercept these so that getter and setter refer to WillsExpando class
     */
    def getAt (String key) {
        def prop
        if (hasProperty(key)) {
            prop = getProperty(key.asType(String))
        } else if (hasStaticExpandoProperty(key)) { //better check dynamic static properties list as well
            prop = getStaticProperty(key)
        }
        prop
    }

    void putAt (String key, Object value) {
        if (hasProperty(key)) {
            setProperty(key.asType(String), value)
        } else if (hasStaticExpandoProperty(key)) {
            setStaticProperty(key.asType(String), value)
        }
    }

    /*
     * intercept all calls for properties and look at metaClass first, then private expandoProperties next
     */
    def getProperty (String name){
        if (name == "static") {
            return getStatic()
        } else if (name == "staticProperties" ){
            return getStaticProperties()
        } else if (name == "properties") {
            return getProperties()
        }
        //check metaclass first
        def prop
        if (metaClass.hasProperty(this, name)) {
            prop =  metaClass.getMetaProperty(name).getProperty(this)
        } else {

            //ok go ahead and look in dynamic store next
            prop = expandoProperties[name]

            if (!prop) {
                throw new MissingPropertyException(name, WillsExpando)
            }
        }
        prop
    }

    def getMetaProperty (String name){
        //check metaclass first
        def prop
        Method getterMethod, setterMethod
        CachedMethod cacheGet, cacheSet
        MetaMethod metaGetter, metaSetter
        if (metaClass.hasProperty(this, name)) {
            String camelCaseName = name[0].toUpperCase() +  name.substring(1)
            getterMethod = getClass().getMethod("get${camelCaseName}")
            setterMethod = getClass().getMethod("set${camelCaseName}",Object)

            cacheGet = new CachedMethod (getterMethod)
            cacheSet = new CachedMethod (setterMethod)

            metaGetter = new ReflectionMetaMethod(cacheGet)
            metaSetter = new ReflectionMetaMethod(cacheSet)
        } else {
            //ok go ahead and look in dynamic store next
            prop = expandoProperties.containsKey(name)
            if (!prop) {
                throw new MissingPropertyException(name, WillsExpando)
            }

            /**
             * to make this work - get two closures, from real methods that take take the dynamic propertyName as key,  and will
             * access the expando's propertyMap.
             * Then curry the dynamicProperty  name on that closure so it takes the same number of params as an ordinary property would
             * create CachedMethods for the ::call() action
             */
            Closure getterClos = this::getAt.curry(name)
            Closure setterClos = this::putAt.curry( name)

            cacheGet = new CachedMethod (getterClos.getClass().getMethod( 'call'))
            cacheSet = new CachedMethod (setterClos.getClass().getMethod ('call', Object))

            metaGetter = new ClosureMetaMethod ('getAt', this.getClass(), getterClos, cacheGet)
            metaSetter = new ClosureMetaMethod ('getAt', this.getClass(), setterClos, cacheSet)
        }

        MetaBeanProperty mbp = new MetaBeanProperty (name, this.getClass(), metaGetter, metaSetter)
        mbp
    }

    /**
     * checks if it has the property, or it exists in the expandoProperties
     *
     * excludes:  doesnt look at static properties defined in staticExpandoProperties
     */
    boolean hasProperty (String name) {
        if (metaClass.hasProperty(this, name )){
            return true
        } else {
            expandoProperties[name] ? true : false
        }
    }

    Map getProperties () {
        List<MetaProperty> mps = metaClass.properties

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        Map m1 = [:]
        for (mp in mps) {
            def name = mp.name
            if (name == "properties" )   //skip recursion here
                continue
            if (Modifier.isStatic(mp.modifiers))  //remove static entries from the list
                continue
            def value = mp.getProperty(this)
            m1.put (name, value)
        }

        m1.putAll (expandoProperties)
        m1
    }

    List<MetaProperty> getMetaProperties () {
        List<MetaProperty> mps = metaClass.properties

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        List<MetaProperty> expandoMetaProperties  = []
        CachedMethod cacheGet, cacheSet
        MetaMethod metaGetter, metaSetter

        for (expandoProp in expandoProperties) {
            String name = expandoProp.key

            /**
             * to make this work - get two closures, from real methods that take take the dynamic propertyName as key,  and will
             * access the expando's propertyMap.
             * Then curry the dynamicProperty  name on that closure so it takes the same number of params as an ordinary property would
             * create CachedMethods for the ::call() action
             */
            Closure getterClos = this::getAt.curry(name)
            Closure setterClos = this::putAt.curry( name)

            cacheGet = new CachedMethod (getterClos.getClass().getMethod( 'call'))
            cacheSet = new CachedMethod (setterClos.getClass().getMethod ('call', Object))

            metaGetter = new ClosureMetaMethod ('getAt', this.getClass(), getterClos, cacheGet)
            metaSetter = new ClosureMetaMethod ('getAt', this.getClass(), setterClos, cacheSet)
            MetaBeanProperty mbp = new MetaBeanProperty (name, this.getClass(), metaGetter, metaSetter)
            expandoMetaProperties.add (mbp)
        }

        mps.addAll (expandoMetaProperties)
        mps
    }

    List<MetaProperty> getStaticMetaProperties () {
        List<MetaProperty> smps = (List) metaClass.properties.findResults{Modifier.isStatic (it.modifiers) ? it :null}

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        List<MetaProperty> expandoMetaProperties  = []
        CachedMethod cacheGet, cacheSet
        MetaMethod metaGetter, metaSetter

        for (expandoProp in staticExpandoProperties) {
            String name = expandoProp.key

            /**
             * to make this work - get two closures, from real methods that take take the dynamic propertyName as key,  and will
             * access the expando's propertyMap.
             * Then curry the dynamicProperty  name on that closure so it takes the same number of params as an ordinary property would
             * create CachedMethods for the ::call() action
             */
            Closure getterClos = this::getAt.curry(name)
            Closure setterClos = this::putAt.curry( name)

            cacheGet = new CachedMethod (getterClos.getClass().getMethod( 'call'))
            cacheSet = new CachedMethod (setterClos.getClass().getMethod ('call', Object))

            metaGetter = new ClosureMetaMethod ('getAt', this.getClass(), getterClos, cacheGet)
            metaSetter = new ClosureMetaMethod ('getAt', this.getClass(), setterClos, cacheSet)
            MetaBeanProperty mbp = new MetaBeanProperty (name, this.getClass(), metaGetter, metaSetter)
            expandoMetaProperties.add (mbp)
        }

        smps.addAll (expandoMetaProperties)
        smps
    }

    def getStaticMetaProperty (String name){
        //check metaclass first
        def prop
        Method getterMethod, setterMethod
        CachedMethod cacheGet, cacheSet
        MetaMethod metaGetter, metaSetter

        MetaProperty mp = metaClass.getProperties().collect {Modifier.isStatic(it.modifiers)? it : null}.find{it.name == name}
        if (mp) {
            String camelCaseName = name[0].toUpperCase() +  name.substring(1)
            getterMethod = getClass().getMethod("get${camelCaseName}")
            setterMethod = getClass().getMethod("set${camelCaseName}",Object)

            cacheGet = new CachedMethod (getterMethod)
            cacheSet = new CachedMethod (setterMethod)

            metaGetter = new ReflectionMetaMethod(cacheGet)
            metaSetter = new ReflectionMetaMethod(cacheSet)
        } else {
            //ok go ahead and look in dynamic store next
            prop = staticExpandoProperties.containsKey(name)
            if (!prop) {
                throw new MissingPropertyException(name, WillsExpando)
            }

            /**
             * to make this work - get two closures, from real methods that take take the dynamic propertyName as key,  and will
             * access the expando's propertyMap.
             * Then curry the dynamicProperty  name on that closure so it takes the same number of params as an ordinary property would
             * create CachedMethods for the ::call() action
             */
            Closure getterClos = this::getAt.curry(name)
            Closure setterClos = this::putAt.curry( name)

            cacheGet = new CachedMethod (getterClos.getClass().getMethod( 'call'))
            cacheSet = new CachedMethod (setterClos.getClass().getMethod ('call', Object))

            metaGetter = new ClosureMetaMethod ('getAt', this.getClass(), getterClos, cacheGet)
            metaSetter = new ClosureMetaMethod ('getAt', this.getClass(), setterClos, cacheSet)
        }

        MetaBeanProperty mbp = new MetaBeanProperty (name, this.getClass(), metaGetter, metaSetter)
        mbp
    }


    def addMethod (String name , def value) {
        if (value instanceof Closure || value instanceof Callable || value instanceof Function)
            expandoMethods.put (name, value)
    }

    def removeMethod (String name) {
        expandoMethods.remove(name)
    }

    Closure getMethod (String name){
        List<MetaMethod> lmm = this.metaClass.getMethods()
        MetaMethod mm
       if (mm = lmm.findResult{it.name == name ?it:null}) {
           new MethodClosure (this, name)
        } else {
            expandoMethods[name]
        }
    }

    Map<String, Closure> getMethods() {
        List<MetaMethod> mms = this.metaClass.metaMethods

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        Map<String, Closure> m1 = [:]
        for (mm in mms) {
            def name = mm.name
            if (name == "properties" || name == "methods" || name == "staticProperties" || name == "staticMethods")   //skip recursion here
                continue
            if (Modifier.isStatic(mm.modifiers))  //remove static entries from the list
                continue
            def value = new MethodClosure (WillsExpando, name)
            m1.put  (name, value)
        }

        m1.putAll(expandoProperties)
        m1

    }

    //non static form as we know the call instance context here
    List<MetaMethod>  getMetaMethods () {

        List<MetaMethod> mms = this.metaClass.getMetaMethods().findAll{!Modifier.isStatic (it.modifiers)}.collect()

        List<MetaMethod> closureMetaMethodList =[]
        for (entry in expandoMethods) {
            CachedClass cachedClass = new CachedClass (this.getClass(), ClassInfo.getClassInfo(this.getClass()))
            Method closureMethod = entry.value.getClass().getMethod('call')
            CachedMethod cachedMethod = new CachedMethod (cachedClass, closureMethod)
            closureMetaMethodList.add (new ClosureMetaMethod((String) entry.key, this.getClass(), (Closure) entry.value, cachedMethod ) )
        }

        mms.addAll(closureMetaMethodList)
        mms
    }

    /**
     * checks for real method if available and returns that, else looks in expandoMethods and creates metaMethod from closure
     * stored with 'name' as its key
     * @param name
     * @param signature, varargs list of classes that the method is expected to take
     * @return
     */
    def getMetaMethod (String name, Class<?>... signature){
        //check metaclass first
        def expandoMethodClosure
        CachedMethod cacheMethod

        MetaMethod metaMethod = metaClass.getMetaMethod(name, signature)
        if (!metaMethod) {

            //ok go ahead and look in dynamic store next
            def exists = expandoMethods.containsKey(name)
            if (!exists) {
                throw new MissingMethodException(name, this.getClass())
            }
            expandoMethodClosure = expandoMethods[(name)]

            /**
             * to make this work - get two closures, from real methods that take take the dynamic propertyName as key,  and will
             * access the expando's propertyMap.
             * Then curry the dynamicProperty  name on that closure so it takes the same number of params as an ordinary property would
             * create CachedMethods for the ::call() action
             */
            cacheMethod = new CachedMethod (expandoMethodClosure.getClass().getMethod( 'call'))

            metaMethod = new ClosureMetaMethod ((String) name, this.getClass(), (Closure)expandoMethodClosure, cacheMethod)
        }

        return metaMethod
    }

    /**
     * checks for real static method if available and returns that, else looks in expandoStaticMethods and creates metaMethod from closure
     * stored with 'name' as its key
     * @param name
     * @param signature, varargs list of classes that the method is expected to take
     * @return
     */
    def getStaticMetaMethod (String name, Class<?>... signature){
        //check metaclass first
        Closure expandoMethodClosure
        CachedMethod cacheMethod

        MetaMethod metaMethod = metaClass.getStaticMetaMethod(name, signature)
        if (!metaMethod) {

            //ok go ahead and look in dynamic store next
            def exists = staticExpandoMethods.containsKey(name)
            if (!exists) {
                throw new MissingMethodException(name, this.getClass())
            }
            expandoMethodClosure = staticExpandoMethods[(name)]

            /**
             * to make this work - get two closures, from real methods that take take the dynamic propertyName as key,  and will
             * access the expando's propertyMap.
             * Then curry the dynamicProperty  name on that closure so it takes the same number of params as an ordinary property would
             * create CachedMethods for the ::call() action
             */
            cacheMethod = new CachedMethod (expandoMethodClosure.getClass().getMethod( 'call'))

            metaMethod = new ClosureStaticMetaMethod ((String) name, this.getClass(), (Closure)expandoMethodClosure)
        }

        return metaMethod
    }

    def invokeMethod (String name, args) {
        MetaMethod mm
        if ( mm = metaClass.getMetaMethod(name, args)) {
            mm.invoke(name, args)
        } else {
            Closure cmm  = expandoMethods[name]
            if (cmm) {
                if (args)
                    cmm.call(args)
                else
                    cmm.call()
            }
            else {
                throw new MissingMethodException(name, this.getClass())
            }
        }
    }

    def propertyMissing (String name) {
        //todo
        //look in class flex attributes first, then in metaClass if anything matches
        if (name == "static") {
            return getStatic()
        }
        def prop = expandoProperties[name]
        if (!prop) {
            prop = metaClass.getMetaProperty(name)
            if (!prop) {
                throw new MissingPropertyException (name, this.getClass())
            }
        }
        prop
    }

    def propertyMissing (String name, value) {
        if (value instanceof Closure || value instanceof Callable || value instanceof Function)
            addMethod (name, value)
        else
            addProperty(name, value)
    }

    def methodMissing (String name, args) {
        //invoked if you've done a call on the expando and it can't find one by normal path so try expandoMethods map
        Closure methodClosure = expandoMethods[name]
        if (!methodClosure) {
            MetaMethod mm = metaClass.getMetaMethod(name)
            if (!mm) {
                throw new MissingPropertyException (name, WillsExpando)
            }
            methodClosure = new MethodClosure (this, name)
        }

        methodClosure.delegate = this
        if (methodClosure.maximumNumberOfParameters > 0)
            methodClosure.call(args)
        else
            methodClosure.call()

    }


    //add static versions of propertyMissing
    static def $static_propertyMissing (String name) {
        //look in class flex attributes first, then in metaClass if anything matches
        def prop = staticExpandoProperties[name]
        if (!prop) {
            prop = this.getMetaClass().getMetaProperty(name)
            if (!prop) {
                throw new MissingPropertyException (name, WillsExpando)
            }
        }
        prop
    }

    static void $static_propertyMissing (String name, value) {
        staticExpandoProperties.put (name, value)
    }

    /*
     * use this as container for the WillsExpando to return on calls for .static
     * puts new properties and classes into the appropriate static map
     */
    class StaticContainer  {

        def propertyMissing (String name, value) {
            WillsExpando.addStaticProperty(name, value)
        }

        def propertyMissing (String name) {
            WillsExpando.getStaticProperty(name)
        }

        def methodMissing (String name, method) {
            WillsExpando.addStaticMethod(name, method)
        }

        def methodMissing (String name) {
            WillsExpando.getStaticMethod (name)
        }

        def getProperties() {
            WillsExpando.staticExpandoProperties
        }

        def getMethods() {
            WillsExpando.staticExpandoMethods
        }
    }
}
