package groovyGDK


import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

/**
 * tests using GroovyTestCase, which includes standard assertions for you
 */
class GroovyTestCaseTest extends GroovyTestCase {

    @Test
    void testAssertions () {
        assertTrue (1 == 1)
        assertEquals("test", "test")

        def x = "42"
        assertNotNull "x cant be null", x
        assertNull null

        assertSame x,x

        //groovyTestCase.shouldFail returns the message, GroovyAssert.shouldFail returns the exception
        String  exception = shouldFail {1/0}
        shouldFail (Exception) {1/0}
        assert exception == "Division by zero"
    }

    @Test
    void testMetaClassStatic () {

        Integer num = 1
        num.metaClass.static.getStatProp = {"dynamic static property"}

        shouldFail (MissingPropertyException) {num.statProp == "dynamic static property"}

        def mm = num.metaClass.getStaticMetaMethod('getStatProp', Object[])
        assertTrue mm.isStatic()
        assertEquals mm.invoke(num, []), "dynamic static property"

        // but cant'ask for a list of staticMetaMethods - you have to know the name in advance

        //ask for hasProperty  - says there is a prop called statProp
        MetaProperty mp = num.hasProperty('statProp')
        assertTrue Modifier.isStatic (mp.modifiers)
        assertTrue num.hasProperty ('statProp').getProperty(num) == "dynamic static property"

        // num.metaClass.getStaticMetaProperty ('statProp')  - method doesnt exist

        //try and get properties - and statProp is not there
        Map props  = num.getProperties()
        assert props.size() == 1
        assert props.get ('class')  == Integer
        assert props.get ('statProp') == null
        mp = props.find{it.key == 'statProp'}
        assert mp == null

    }
}
