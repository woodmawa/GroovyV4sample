package groovyGDK

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test

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
}
