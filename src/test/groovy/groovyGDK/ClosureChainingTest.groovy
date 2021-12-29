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
}
