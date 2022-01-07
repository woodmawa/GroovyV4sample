package mop

import groovy.test.GroovyAssert
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicLong

import static groovy.test.GroovyAssert.*

class SampleClass {
    String someProperty = "class property"
    String someMethod () {"someMethod return"}

    static String staticCounter = new AtomicLong(0)
    static String someStaticMethod () {"someStaticMethod return"}

}

class WillsMetaClassTest extends Specification {

    @Shared SampleClass sample
    @Shared WillsMetaClass2 wmc

    //run before each test
    def setup () {
        sample = new SampleClass()

        List consList = WillsMetaClass2.constructors.collect{"${it.name}, ${it.parameterTypes}" }
        Constructor cons = WillsMetaClass2.constructors.find{it.parameterTypes == [Class, boolean, boolean]}

        wmc = new WillsMetaClass2 (SampleClass, true, true)
        wmc.initialize()
        assert wmc


    }


    def "test standard Mop"() {
        given:
        def NO_ARGS = (Object[])[]

        expect:
        sample.someProperty == "class property"
        sample.someMethod() == "someMethod return"

        sample.hasProperty('someProperty')

        //normal method
        sample.respondsTo('someMethod', NO_ARGS)
        MetaMethod metaMethod = sample.metaClass.pickMethod('someMethod', null)
        metaMethod.name == 'someMethod'
        Modifier.isPublic( metaMethod.modifiers)
        sample.invokeMethod('someMethod', null) == "someMethod return"

        GroovyAssert.shouldFail(MissingMethodException) {
            sample.invokeMethod('dynamicMethod', null) == "dynamicMethod return"
        }

        shouldFail(MissingPropertyException) {
            sample.dynamicProperty
        }

        //class static method
        sample.respondsTo('someStaticMethod', NO_ARGS)
        sample.invokeMethod('someStaticMethod', null) == "someStaticMethod return"

        sample.properties.size() == 3
        sample.properties.toString() == [someProperty:"class property", staticCounter:0, class:mop.SampleClass].toString()

        sample.metaClass.methods.size() == 18
    }

    def "test replacement WillsMetaClass" () {
        given:
        MetaClass mc

        when:

        sample.setMetaClass(wmc)   //set new metaclass

        assert sample.metaClass == wmc

        /*shouldFail(MissingPropertyException) {
            sample.dynamicProperty
        }

        sample.metaClass.dynamicProperty = "dynamic metaClass property"
        */

        //set it first time
        sample.metaClass.setStaticProperty ("newStaticDynamicProperty", "added static dynamic metaClass property")
        mc = sample.metaClass

        then:

        //sample.hasProperty("dynamicProperty")
        //sample.dynamicProperty == "dynamic metaClass property"

        //sample.hasProperty("newStaticDynamicProperty")
        //sample.metaClass.hasMetaProperty ("newStaticDynamicProperty")

        // read once
        sample.newStaticDynamicProperty == "added static dynamic metaClass property"


        and:

        //set again
        sample.setProperty('newStaticDynamicProperty',  "modified static property") == null
        //re read
        sample.newStaticDynamicProperty == "modified static property"

    }
}
