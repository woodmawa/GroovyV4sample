package utils

import spock.lang.Specification

class SpockTest extends Specification {

    def "one plus one should equal 2 " () {
        expect:
        1 + 1 == 2
    }
}
