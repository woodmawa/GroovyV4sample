package script

import org.codehaus.groovy.runtime.metaclass.ThreadManagedMetaBeanProperty

import java.lang.reflect.Modifier

class MyClass {
    static String classDeclaredStatName = "static will"

    String getStaticName () {

        this.metaClass.getProperty(this, "statName")
    }

    def getPropertyValue (String propName) {
        def hasPropWill = MyClass.metaClass.hasProperty(this, propName)
        def propValue = MyClass.metaClass.getProperty(this, propName)

    }

    def getStaticAttributeWithValue(Object searchedValue) {
        MyClass.metaClass.properties
                .findAll{MetaProperty mp ->
                    println mp.getter.name
                    mp.getter.static }
                .find {it ->
                    MyClass[it.name]  }
    }
}

//metaclass behaves like an exapndo - add variable and give it a value
MyClass.metaClass.will = "hello"
MyClass.metaClass.static.extendedStaticName = "extended static hello"

MyClass num1 = new MyClass()

println "read std static classDeclaredStatName : " + MyClass.classDeclaredStatName

println "read prop will : " + num1.getPropertyValue("will")

println "read static prop statName : " + num1.getStaticAttributeWithValue("static hello")

List<MetaProperty> staticProps = MyClass.metaClass.properties.findAll { Modifier.isStatic (it.modifiers) }
println "static dump " + staticProps.collect {it ->
     it.name}
//println num1.metaClass.static.statName