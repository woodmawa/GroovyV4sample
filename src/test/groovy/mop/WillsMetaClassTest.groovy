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

    @Shared
    SampleClass sample
    @Shared
    WillsMetaClass2 wmc

    //run before each test
    def setup() {
        sample = new SampleClass()

        List consList = WillsMetaClass2.constructors.collect { "${it.name}, ${it.parameterTypes}" }
        Constructor cons = WillsMetaClass2.constructors.find { it.parameterTypes == [Class, boolean, boolean] }

        wmc = new WillsMetaClass2(SampleClass, true, true)
        wmc.initialize()
        assert wmc


    }


    def "test standard Mop"() {
        given:
        def NO_ARGS = (Object[]) []

        expect:
        sample.someProperty == "class property"
        sample.someMethod() == "someMethod return"

        sample.hasProperty('someProperty')

        //normal method
        sample.respondsTo('someMethod', NO_ARGS)
        MetaMethod metaMethod = sample.metaClass.pickMethod('someMethod', null)
        metaMethod.name == 'someMethod'
        Modifier.isPublic(metaMethod.modifiers)
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
        sample.properties.toString() == [someProperty: "class property", staticCounter: 0, class: mop.SampleClass].toString()

        sample.metaClass.methods.size() == 18
    }

    def "test replacement WillsMetaClass with properties "() {
        given:
        MetaClass mc

        when:

        sample.setMetaClass(wmc)   //set new metaclass

        assert sample.metaClass == wmc

        shouldFail(MissingPropertyException) {
            sample.dynamicProperty
        }

        sample.metaClass.dynamicProperty = "dynamic metaClass property"


        //set static property for first time -
        // NB you can only do this from metaClass, as GroovyObject mappings cant be changed, once in its held in mbp properties map along with others
        sample.metaClass.setStaticProperty("newStaticDynamicProperty", "added static dynamic metaClass property")

        then:

        sample.hasProperty("dynamicProperty")
        sample.dynamicProperty == "dynamic metaClass property"

        //this doesnt count the static properties
        sample.properties.size() == 4

        sample.hasProperty("newStaticDynamicProperty")
        sample.metaClass.hasMetaProperty("newStaticDynamicProperty")

        // read static for first time - uses the initial value initialiser and put entry into mbp backing map for this instance
        sample.newStaticDynamicProperty == "added static dynamic metaClass property"
        //setup parity for staticProperties as you see with properties
        sample.metaClass.staticProperties.size() == 2
        sample.staticProperties.size() == 2


        and: "change value of the static property, and read it back  "

        sample.setProperty('newStaticDynamicProperty', "modified static property") == null
        sample.newStaticDynamicProperty == "modified static property"

    }

    def "test replacement WillsMetaClass with methods  "() {
        given:
        MetaClass mc

        when:

        sample.setMetaClass(wmc)   //set new metaclass

        assert sample.metaClass == wmc

        shouldFail(MissingMethodException) {
            sample.dynamicMethod(1)
        }

        assert    !sample.respondsTo("dynamicMethod")

        def countOfMethods = sample.metaClass.getMethods().size()
        def countOfMetaMethods = sample.metaClass.getMetaMethods().size()

        //now create dynamic method
        sample.metaClass.dynamicMethod = { it}

        def countOfMethodsAfter = sample.metaClass.getMethods().size()
        def countOfMetaMethodsAfter = sample.metaClass.getMetaMethods().size()

        then: ""
        sample.respondsTo("someMethod")
        sample.someMethod() == "someMethod return"
        sample.respondsTo("dynamicMethod")
        sample.dynamicMethod(1) == 1

        //adding closure adds a method with no args and a method with one arg - hence 2
        countOfMethods == 18
        countOfMethodsAfter == countOfMethods + 2

        //these inherited methods by mop dont seem to change
        countOfMetaMethods == 69
        countOfMetaMethodsAfter == countOfMetaMethods

    }

    def "def test of metaClass.static" () {
        given:

        when:
        sample.setMetaClass(wmc)   //set new metaclass

        assert sample.metaClass == wmc

        def stat =  sample.metaClass.'static'

        def beforePropsSize = stat.properties.size ()
        def beforeStaticPropsSize = stat.methods.size ()

        stat.newStatProp = 10
        stat.newStatMethod = {"new static method"}

        def afterPropsSize = stat.properties.size ()
        def afterStaticPropsSize = stat.methods.size ()

        then:

        //todo : need to clear out statics as they persist across tests and buggers the counts
        stat != null
        stat.getClass() == WillsMetaClass2.WillsExpandoMetaProperty
        beforePropsSize == 0
        beforeStaticPropsSize == 0
        afterPropsSize == 1
        afterStaticPropsSize == 2 //gettter and setter

    }
}