package pointmap

import spock.lang.Specification

import static java.util.Comparator.comparing

class IncludesOptionals {
    Optional var = Optional.empty()
}

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
        //def comparator = Comparator.comparing (Integer::compare(), Comparator.comparing(Optional::get))
        //def result = comparator.comparing(one, two)

        IncludesOptionals io1 = new IncludesOptionals()
        io1.var = Optional.of(1)
        IncludesOptionals io2 = new IncludesOptionals()
        io2.var = Optional.of(2)
        IncludesOptionals ioNull = new IncludesOptionals()

        def oneLessThantwo = 1.compareTo(2)
        def twoGreaterThanOne = 2.compareTo(1)

        /** create an integer comparator using nested comparing.  External means to generate a compare, when you cant implement compareTo
         *  first function gets the optional itself, the second comparing gets the value from the optional
         * however this wouldnt handle Nulls, without the NullsFirst() which assumes null < value
         */
        Comparator<Integer> compareOptionals = comparing (
                        IncludesOptionals::getVar,
                        //comparing(opt -> opt.orElse(null), nullsLast(naturalOrder()))  - )
                        //nullsFirst assumes null is less than a non-null value
                        comparing(opt -> opt.orElse(null), Comparator.nullsFirst(Comparator.naturalOrder()))
                )

        def lowerLessRes = compareOptionals.compare(io1,io2)
        def higherGreaterRes = compareOptionals.compare(io2,io1)
        def equalRes = compareOptionals.compare(io1,io1)

        def lowerLessNullRes = icomp.compare(ioNull,io1)

        expect:
        //result
        lowerLessRes == -1
        higherGreaterRes == 1
        equalRes == 0
        lowerLessNullRes == -1
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

    def "test compare Points" () {
        given:
        Point pNull = new Point (null, 0)
        Point p1 = new Point (0,0)
        Point p2 = new Point (1,0)
        Point p3 = new Point (0, 1)
        Point pt = new Point (0,0, 1)

        expect:
        p1.compareTo(p2) == -1
        pNull.compareTo(p1) == -1
        p2.compareTo(p3) == 1
        p1.compareTo(p3) == -1
    }
}
