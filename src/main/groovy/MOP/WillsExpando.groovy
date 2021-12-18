package MOP

import com.sun.tools.jdi.JDWP
import groovy.transform.EqualsAndHashCode
import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

@EqualsAndHashCode (includeFields = true)
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

        List mps = ofThing.metaClass.getProperties().findAll{Modifier.isStatic (it.modifiers)}.collect()

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        Map m1 = [:]
        for (mp in mps) {
            def name = mp.name
            if (name == "properties" || name == "methods" ||name == "staticProperties" || name == "staticMethods")   //skip recursion here
                continue
            def value = mp.getProperty(this)
            m1 << [(name): value]
        }

        //cant seem to add a static property using MOP - so just get the staticExpandoProperties here
        //List l2 = staticExpandoProperties.collect() //.asImmutable()
        //(l1 + l2).asImmutable()
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
            m1 << [(name): value]
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

    static boolean hasStaticExpandoProperty (String name) {
        staticExpandoProperties[name] ? true : false
    }

    //if used on static class we dont know the context so we have to pass as a param to get the MetaMethods
    static Map  getStaticMethods (ofThing) {
        //todo : not working yet
        List<MetaMethod> mms = ofThing.metaClass.getMetaMethods().findAll{Modifier.isStatic (it.modifiers)}.collect()

        Map m1 = [:]
        for (mm in mms){
            def name = mm.name
            def value = new MethodClosure (WillsExpando,name)
            m1 << [(name): value]
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
            def value = new MethodClosure (WillsExpando,name)
            m1 << [(name): value]
        }
        m1.putAll(staticExpandoMethods)
        m1
    }

    def addProperty (String name , def value) {
        if (value instanceof Closure || value instanceof Callable || value instanceof Function )
            addMethod (name, value )
        else
            expandoProperties.put(name, value)
    }

    def addProperties (Map props) {
        props.each {prop, value -> addProperty (prop, value)}
    }

    def removeProperty (String name) {
        expandoProperties.remove(name)
    }

    /*
     * next two methods are delegating proxies for ConcurrentMap when generating MetaProperties for WillsExpando
     * defer to standard props first then look at the private expandoProperties ConcurrentMap
     * so we need to intercept these so that getter and setter refer to WillsExpando class
     */
    def getAt (Object key) {
        getProperty (key.asType(String))
    }

    def putAt (Object key, Object value) {
        setProperty (key.asType(String), value)
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
        if (metaClass.hasProperty(this, name)) {
            prop =  metaClass.getMetaProperty(name).getProperty(this)
            String camelCaseName = name[0].toUpperCase() +  name.substring(1)
            getterMethod = getClass().getMethod("get${camelCaseName}")
            setterMethod = getClass().getMethod("set${camelCaseName}",Object)
        } else {
            //ok go ahead and look in dynamic store next
            prop = expandoProperties.containsKey(name)
            if (!prop) {
                throw new MissingPropertyException(name, WillsExpando)
            }
            //todo: hard - need to do right curry on some thing to bind the prop name to the method

            Closure getterClos = this::getAt.rcurry(name)
            Closure setterClos = this::putAt.ncurry(1, name)

            getterMethod = getterClos.getClass().getMethod( 'call')
            setterMethod = setterClos.getClass().getMethod ('call', Object)

            /*getterMethod = getClass().getMethod( 'getAt', Object)
            setterMethod = getClass().getMethod ('putAt', Object, Object)*/
        }

        MetaMethod getter = new ReflectionMetaMethod (new CachedMethod(getterMethod))
        MetaMethod setter = new ReflectionMetaMethod (new CachedMethod(setterMethod))

        MetaBeanProperty mbp = new MetaBeanProperty (name, this.getClass(), getter, setter)
        mbp
    }

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
            if (name == "properties" || name == "methods" || name == "staticProperties" || name == "staticMethods")   //skip recursion here
                continue
            if (Modifier.isStatic(mp.modifiers))  //remove static entries from the list
                continue
            def value = mp.getProperty(this)
            m1 << [(name): value]
        }

        m1.putAll (expandoProperties)
        m1
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
        List<MetaProperty> mms = this.metaClass.metaMethods

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        Map m1 = [:]
        for (mm in mms) {
            def name = mm.name
            if (name == "properties" || name == "methods" || name == "staticProperties" || name == "staticMethods")   //skip recursion here
                continue
            if (Modifier.isStatic(mm.modifiers))  //remove static entries from the list
                continue
            def value = new MethodClosure (WillsExpando, name)
            m1 << [(name): value]
        }

        m1.putAll(expandoProperties)
        m1

    }

    def invokeMethod (name, args) {
        def mm
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
                new MissingMethodException("Cant find method $name with $args to invoke")
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
                throw new MissingPropertyException (name, WillsExpando)
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
        Closure method = expandoMethods[name]
        if (!method) {
            method = metaClass.getMetaMethod(name)
            if (!method) {
                throw new MissingPropertyException (name, WillsExpando)
            }
        }
        method.delegate = this
        if (method.maximumNumberOfParameters > 0)
            method.call(args)
        else
            method.call()

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
