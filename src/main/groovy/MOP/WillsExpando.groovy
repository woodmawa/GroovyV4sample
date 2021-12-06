package MOP

import com.sun.tools.jdi.JDWP
import groovy.transform.EqualsAndHashCode
import org.codehaus.groovy.runtime.MethodClosure

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
    static List<Map.Entry> getStaticProperties (def ofThing) {
        List mpl = ofThing.metaClass.getProperties()

        List<MetaProperty> mps = ofThing.metaClass.getProperties().findAll{Modifier.isStatic (it.modifiers)}.collect()

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        List l1 = []
        for (mp in mps) {
            def name = mp.name
            if (name == "properties" || name == "methods" ||name == "staticProperties" || name == "staticMethods")   //skip recursion here
                continue
            def value = mp.getProperty(this)
            l1 << [(name): value].collect()[0]
        }

        //cant seem to add a static property using MOP - so just get the staticExpandoProperties here
        List l2 = staticExpandoProperties.collect() //.asImmutable()
        (l1 + l2).asImmutable()
    }

    //non static form where we know the instance context class
    List<Map.Entry> getStaticProperties () {
        List<MetaProperty> mps = this.metaClass.getProperties().findAll{Modifier.isStatic (it.modifiers)}.collect()

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        List l1 = []
        for (mp in mps) {
            def name = mp.name
            if (name == "properties" || name == "methods" ||name == "staticProperties" || name == "staticMethods")   //skip recursion here
                continue
            def value = mp.getProperty(this)
            l1 << [(name): value].collect()[0]
        }

        //cant seem to add a static property using MOP - so just get the staticExpandoProperties here
        List l2 = staticExpandoProperties.collect() //.asImmutable()
        (l1 + l2).asImmutable()
    }

    static def addStaticMethod (String name , def value) {
        if (value instanceof Closure || value instanceof Callable || value instanceof Function)
            staticExpandoMethods.put(name, value)
    }

    static void removeStaticMethod (String name) {
        staticExpandoMethods.remove(name)
    }

    static def getStaticMethod (String name){
        staticExpandoMethods[name]
    }

    static boolean hasStaticExpandoProperty (String name) {
        staticExpandoProperties[name] ? true : false
    }

    //if used on static class we dont know the context so we have to pass as a param to get the MetaMethods
    static List<Map.Entry>  getStaticMethods (ofThing) {
        //todo : not working yet
        List<MetaMethod> mms = ofThing.metaClass.getMetaMethods().findAll{Modifier.isStatic (it.modifiers)}.collect()

        List l1 =[]
        for (mm in mms){
            def name = mm.name
            def value = new MethodClosure (WillsExpando,name)
            l1 << [(name): value]
        }
        List l2 = staticExpandoMethods.collect()
        (l1 + l2).asImmutable()
    }

    //non static form as we know the call instance context here
    List<Map.Entry>  getStaticMethods () {
        List<MetaMethod> mms = this.metaClass.getMetaMethods().findAll{Modifier.isStatic (it.modifiers)}.collect()

        List l1 =[]
        for (mm in mms){
            def name = mm.name
            def value = new MethodClosure (WillsExpando,name)
            l1 << [(name): value]
        }
        List l2 = staticExpandoMethods.collect()
        (l1 + l2).asImmutable()
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

    def getProperty (String name){
        if (name == "static") {
            return getStatic()
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

    boolean hasProperty (String name) {
        if (metaClass.hasProperty(this, name )){
            return true
        } else {
            expandoProperties[name] ? true : false
        }
    }

    List<Map.Entry> getProperties () {
        List<MetaProperty> mps = metaClass.properties

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        List l = []
        for (mp in mps) {
            def name = mp.name
            if (name == "properties" || name == "methods" || name == "staticProperties" || name == "staticMethods")   //skip recursion here
                continue
            if (Modifier.isStatic(mp.modifiers))  //remove static entries from the list
                continue
            def value = mp.getProperty(this)
            l << [(name): value].collect()[0]
        }

        def l2 = expandoProperties.collect()
        (l + l2).asImmutable()
    }

    def addMethod (String name , def value) {
        if (value instanceof Closure || value instanceof Callable || value instanceof Function)
            expandoMethods.put (name, value)
    }

    def removeMethod (String name) {
        expandoMethods.remove(name)
    }

    def getMethod (String name){
        List<MetaMethod> lmm = this.metaClass.getMethods()
        MetaMethod mm
       if (mm = lmm.findResult{it.name == name ?it:null}) {
           new MethodClosure (this, name)
        } else {
            expandoMethods[name]
        }
    }

    def getMethods() {
        List<MetaProperty> mms = this.metaClass.metaMethods

        // have to stop recursion on properties, and skip dynamic concurrent maps from showing
        List l = []
        for (mm in mms) {
            def name = mm.name
            if (name == "properties" || name == "methods" || name == "staticProperties" || name == "staticMethods")   //skip recursion here
                continue
            if (Modifier.isStatic(mm.modifiers))  //remove static entries from the list
                continue
            def value = new MethodClosure (WillsExpando, name)
            value.delegate = this
            l << [(name): value].collect()[0]  //get the  Map.Entry part
        }

        def l2 = expandoProperties.collect()
        (l + l2).asImmutable()

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
            WillsExpando.staticExpandoProperties.collect().asImmutable()
        }

        def getMethods() {
            WillsExpando.staticExpandoMethods.collect().asImmutable()
        }
    }
}
