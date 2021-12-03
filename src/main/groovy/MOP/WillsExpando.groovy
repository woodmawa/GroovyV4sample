package MOP

import groovy.transform.EqualsAndHashCode
import org.codehaus.groovy.runtime.MethodClosure

import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

@EqualsAndHashCode (includeFields = true)
class WillsExpando {

    static protected Map staticExpandoProperties = new ConcurrentHashMap()
    static protected Map staticExpandoMethods = new ConcurrentHashMap()

    protected Map expandoProperties = new ConcurrentHashMap()
    protected Map expandoMethods = new ConcurrentHashMap()

    String stdProp = "defaultClassProp"

    def testMethod (String test) {
        test + " : hello from test method"
    }

    WillsExpando () {}

    WillsExpando (Map initialProperties) {
        addProperties(initialProperties)
    }


    StaticContainer getStatic () {
        new StaticContainer()  //(staticMetaProperties.collect(), staticMetaMethods.collect())
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

    static def getStaticProperty (String name){
        staticExpandoProperties[name]
    }

    static List<Map.Entry> getStaticProperties () {
        //cant seem to add a static property using MOP - so just get the staticExpandoProperties here
        staticExpandoProperties.collect().asImmutable()
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

    static List<Map.Entry>  getStaticMethods () {
        staticExpandoMethods.collect().asImmutable()
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

    //voided by creating getProperty() - this gets first dibs
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
        //invoked if youve don a call on the expando and it can't find one by normal path so try expandoMethods map
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
        //todo
        //look in class flex attributes first, then in metaClass if anything matches
        if (name == "static") {
            return getStatic()  //todo: not static method - will fail
        }
        def prop = staticExpandoProperties[name]
        if (!prop) {
            prop = this.getMetaClass().getMetaProperty(name)
            if (!prop) {
                throw new MissingPropertyException (name, WillsExpando)
            }
        }
        prop
    }

    static def $static_propertyMissing (String name, value) {
        staticExpandoProperties.addStaticProperty(name, value)
    }

    /*
     * use this as container for the WillsExpando to return calls for .static
     */
    class StaticContainer  {

        def propertyMissing (String name, value) {
            println "setting prop $name with value $value"
            WillsExpando.addStaticProperty(name, value)
        }

        def propertyMissing (String name) {
            println "getting unknown prop $name "
            WillsExpando.getStaticProperty(name)
        }

        def methodMissing (String name, method) {
            println "setting method $name with value $method"
            WillsExpando.addStaticMethod(name, method)
        }

        def methodMissing (String name) {
            println "getting method  $name "
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
