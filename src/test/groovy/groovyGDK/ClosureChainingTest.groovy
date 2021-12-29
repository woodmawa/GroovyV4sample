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
        def plusThreeTimesTwo = plusThree  >> timesTwo
        def plusThreeTimesTwoMkII = timesTwo << plusThree
        def timesTwoPlusThree = timesTwo >> plusThree
        def timesTwoPlusThreeMkII = plusThree << timesTwo //= plusThree(timesTwo(x))

        then :
        plusThreeTimesTwo(2) == 10
        plusThreeTimesTwoMkII (2) == 10
        timesTwoPlusThree (2) == 7
        timesTwoPlusThreeMkII (2 ) == 7
    }
}
