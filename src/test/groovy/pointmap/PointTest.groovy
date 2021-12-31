package pointmap

import spock.lang.Specification

class PointTest extends Specification {

    def "optional equality test " () {
        expect:
        //want this to work as Point as key in map should have fixed hash
        Optional.of ("will") == Optional.of ("will")
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
