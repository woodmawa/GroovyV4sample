package script

import org.codehaus.groovy.runtime.metaclass.ThreadManagedMetaBeanProperty

import java.lang.reflect.Modifier

Expando expando = new Expando ()

expando.name = "william"

expando.method = System.out::println

Map mp =  expando.getProperties()

mp.method ("called method")

mp
class MyClass {
    static String classDeclaredStatName = "in class definition static name"

    String name = "instance level name "

    def getPropertyValue (String propName) {
        def hasPropWill = MyClass.metaClass.hasProperty(this, propName)
        def propValue = MyClass.metaClass.getProperty(this, propName)

    }

    def getStaticAttributeWithValue(Object searchedValue) {
        MyClass.metaClass.properties
                .findAll{MetaProperty mp ->
                    println mp.getter.name
                    mp.getter.static }
                .each {it ->
                    MyClass[it.name]  }
    }

    def getStaticMethodsNames () {
        MyClass.metaClass.methods
            .findAll {MetaMethod mm ->
                Modifier.isStatic (mm.modifiers)
            }.collect {it.name}
    }
}

//metaclass behaves like an expando - add variable and give it a value
MyClass.metaClass.will = "hello"
//metaclass behaves like an expando - add static method that takes no params with getXxx to sumulate a property
MyClass.metaClass.static.getExtendedStaticName = {-> "returns extended static hello"}

MyClass num1 = new MyClass()

assert MyClass.metaClass.hasProperty(MyClass, 'name')
assert MyClass.metaClass.hasProperty(MyClass, 'classDeclaredStatName')


MetaMethod ext = MyClass.metaClass.getStaticMetaMethod('getExtendedStaticName')

println "read std static getExtendedStaticName closure : " + ext.invoke(MyClass )

println "static metaMethods :  " + num1.getStaticMethodsNames()

println "read prop will : " + num1.getPropertyValue("will")

//println "invoke external metaclass added closure " + num1.extendedStaticName

println "read static prop statName : " + num1.getStaticAttributeWithValue("static hello")

List<MetaProperty> staticProps = MyClass.metaClass.properties.findAll { Modifier.isStatic (it.modifiers) }
println "static dump " + staticProps.collect {it ->
     it.name}
//println num1.metaClass.static.statName

