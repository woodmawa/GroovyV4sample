package groovyGDK

import org.junit.Test
import spock.lang.Specification

class ClosureChainingTest extends Specification {

    @Test
    def "closure chaining" () {
        given:
        Closure plusThree = {it + 3}
        Closure timesTwo = {it * 2}

        when:
        def plusThreeTimesTwo = plusThree  >> timesTwo  //rightShift: plusThree(timesTwo(x))
        def plusThreeTimesTwoMkII = timesTwo << plusThree //leftShift: plusThree(timesTwo(x))
        def timesTwoPlusThree = timesTwo >> plusThree
        def timesTwoPlusThreeMkII = plusThree << timesTwo //= plusThree(timesTwo(x))

        then :
        plusThreeTimesTwo(2) == 10
        plusThreeTimesTwoMkII (2) == 10
        timesTwoPlusThree (2) == 7
        timesTwoPlusThreeMkII (2 ) == 7
    }

    @Test
    def "closure chaining again" () {
        given:
        Closure f = {it + 2}
        Closure g = {it * 2}

        when:
        def g_After_f = g << f
        def f_Before_g = f >> g

        then:
        g_After_f (1) == 6
        f_Before_g(1 ) == 6

    }

    @Test
    def " try out" () {
        given :
            MyClass mc = new MyClass ()
            mc.processClosure {value * 2 * it }
            mc.value = 1
        when :
            def result = mc >> {it + 100} //rightShift applies mc.value to the closure and returns the result
        then:
        //210 == mc.myClosure(1)
            result == 202
    }
}

class MyClass {
    def value
    Closure closure
    Closure myClosure

    def rightShift (Closure clos) {
        //invoke clos with value and pass to myClosure
        def result = myClosure?.call (clos(value))

    }

    void processClosure (@DelegatesTo (MyClass) Closure clos) {
        //sets up myClosure
        myClosure = clos.clone()
        myClosure.delegate = this
    }
}