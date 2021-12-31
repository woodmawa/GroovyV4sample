package pointmap

import spock.lang.Specification

class PointTest extends Specification {

    def "optional equality test " () {
        expect:
        //want this to work as Point as key in map should have fixed hash
        Optional.of ("will") == Optional.of ("will")
    }

    def "compare two optionals " () {
        given:
        Optional one = Optional.of(1)
        Optional two = Optional.of (2)
        def comparator = Comparator.comparing (Integer::compare(), Comparator.comparing(Optional::get))
        def result = comparator.comparing(one, two)

        expect:
        result
    }

    def "test point equality" () {
        expect:
        new Point (0,0) == new Point (0, 0)
    }

    def "test point hashCode equality" () {
        expect:
        new Point (0,0).hashCode() == new Point (0, 0).hashCode()
    }

    def "testToString" () {
        expect:
        new Point (0,0).toString() == "Point (Optional[0], Optional[0], Optional.empty, Optional.empty, Optional.empty, Optional.empty)"
    }
}
