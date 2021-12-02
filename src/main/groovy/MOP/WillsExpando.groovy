package MOP

import groovy.transform.EqualsAndHashCode

import java.util.concurrent.ConcurrentHashMap

@EqualsAndHashCode (includeFields = true)
class WillsExpando {

    static protected Map staticMetaProperties = new ConcurrentHashMap()
    static protected Map staticMetaMethods = new ConcurrentHashMap()

    protected Map metaProperties = new ConcurrentHashMap()
    protected Map metaMethods = new ConcurrentHashMap()


    WillsExpando () {}

    WillsExpando (Map initialProperties) {
        metaProperties.putAll(initialProperties)
    }


    StaticContainer getStatic () {
        new StaticContainer()  //(staticMetaProperties.collect(), staticMetaMethods.collect())
     }

    static def addStaticProperty (String name , def value) {
        staticMetaProperties.put(name, value)
        return null
    }

    static def removeStaticProperty (String name) {
        staticMetaProperties.remove(name)
    }

    static def getStaticProperty (String name){
        staticMetaProperties[name]
    }

    static List<Map.Entry> getStaticProperties () {
        staticMetaProperties.collect().asImmutable()
    }

    static def addStaticMethod (String name , def value) {
        staticMetaMethods.put(name, value)
    }

    static def removeStaticMethod (String name) {
        staticMetaMethods.remove(name)
    }

    static def getStaticMethod (String name){
        staticMetaMethods[name]
    }

    boolean hasStaticMetaProperty (String name) {
        staticMetaProperties[name] ? true : false
    }

    static List<Map.Entry>  getStaticMethods () {
        staticMetaMethods.collect().asImmutable()
    }


    def addProperty (String name , def value) {
        metaProperties.put(name, value)
    }

    def removeProperty (String name) {
        metaProperties.remove(name)
    }

    def getProperty (String name){
        if (name == "static") {
            return getStatic()
        }
        //check metaclass first
        if (metaClass.hasProperty(this, name)) {
            return metaClass.getMetaProperty(name).getProperty(this)
        }

        //ok go ahead and look in dynamic store next
        def prop = metaProperties[name]
        if (!prop){
            throw new MissingPropertyException (name, WillsExpando)
        }
        prop
    }



    boolean hasMetaProperty (String name) {
        metaProperties[name] ? true : false
    }

    List<Map.Entry> getProperties () {
        metaProperties.collect().asImmutable()
    }

    //voided by creating getProperty()
    def propertyMissing (String name) {
        //todo
        //look in class flex attributes first, then in metaClass if anything matches
        if (name == "static") {
            return getStatic()
        }
        def prop = metaProperties[name]
        if (!prop) {
            prop = metaClass.getMetaProperty(name)
            if (!prop) {
                throw new MissingPropertyException (name, WillsExpando)
            }
        }
        prop
    }

    def propertyMissing (String name, value) {
        addProperty(name, value)
    }

    //add static versions of propertyMissing
    static def $static_propertyMissing (String name) {
        //todo
        //look in class flex attributes first, then in metaClass if anything matches
        if (name == "static") {
            return getStatic()
        }
        def prop = staticMetaProperties[name]
        if (!prop) {
            prop = this.getMetaClass().getMetaProperty(name)
            if (!prop) {
                throw new MissingPropertyException (name, WillsExpando)
            }
        }
        prop
    }

    static def $static_propertyMissing (String name, value) {
        staticMetaProperties.addStaticProperty(name, value)
    }

    /*
     * use this as container for the WillsExpando to return calls on .static
     */
    class StaticContainer  {

        // ref to class static
        private List staticProperties
        private List staticMethods

        /*
        StaticContainer (staticProperties, staticMethods) {
            this.staticProperties = staticProperties
            this.staticMethods = staticMethods
        }*/

        def propertyMissing (String name, value) {
            println "setting prop $name with value $value"
            WillsExpando.addStaticProperty(name, value)
        }

        def propertyMissing (String name) {
            println "getting unknown prop $name "
            WillsExpando.getStaticProperty(name)
        }

        def getProperties() {
            WillsExpando.staticMetaProperties.collect().asImmutable()
        }

        def getMethods() {
            WillsExpando.staticMetaMethods.collect().asImmutable()
        }
    }
}