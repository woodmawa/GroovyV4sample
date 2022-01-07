package MOP

import org.junit.jupiter.api.Test
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicLong


class SampleClass {
    String someProperty = "class property"
    String someMethod () {"someMethod return"}

    static String staticCounter = new AtomicLong(0)
    static String someStaticMethod () {"someStaticMethod return"}

}

/*
class WillsMetaClassTest extends Specification{

    @Shared SampleClass sample

    //run before each test
    def setup () {
        sample = new SampleClass()

    }


    def "test standard Mop"() {
        expect:
        sample.someProperty == "class property"
        sample.someMethod() == "someMethod return"

        sample.hasProperty('someProperty')
    }


}
*/



class MetaClassTest {
    @Test
    void test () {
        assert true
    }
}