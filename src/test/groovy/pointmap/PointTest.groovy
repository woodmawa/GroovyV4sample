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

    //exploration test to try compare by using Comparator static method generation of a comparator
    def "compare two optionals " () {
        given:
        Optional one = Optional.of(1)
        Optional two = Optional.of (2)
        Optional empty = Optional.ofNullable (null)

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
        Comparator<Integer> compareIncludesOptionals = comparing (
                        IncludesOptionals::getVar,
                        //comparing(opt -> opt.orElse(null), nullsLast(naturalOrder()))  - )
                        //nullsFirst assumes null is less than a non-null value
                        comparing(opt -> opt.orElse(null), Comparator.nullsFirst(Comparator.naturalOrder()))
                )

        def lowerLessRes = compareIncludesOptionals.compare(io1,io2)
        def higherGreaterRes = compareIncludesOptionals.compare(io2,io1)
        def equalRes = compareIncludesOptionals.compare(io1,io1)

        def lowerLessNullRes = compareIncludesOptionals.compare(ioNull,io1)

        Comparator compareOptionals = comparing (opt -> opt.orElse(null), Comparator.nullsFirst(Comparator.naturalOrder())
        )

        Closure cmpOptionals = {Optional first, Optional second ->
            def firstVal = first.orElse(null)
            def secondVal = second.orElse(null)

            if (firstVal && secondVal)
                assert firstVal.getClass() == secondVal.getClass()

            firstVal <=> secondVal
        }


        expect:
        //result
        lowerLessRes == -1
        higherGreaterRes == 1
        equalRes == 0
        lowerLessNullRes == -1

        cmpOptionals (one,two) == -1
        cmpOptionals (two,one) == 1
        cmpOptionals (empty,one) == -1

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
        new Point (0,0).toString() == "Point ([unNamed]: Optional[0], Optional[0], Optional.empty, Optional.empty, Optional.empty, Optional.empty)"
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


    def "confirm named and unnamed point have same hashCode" () {
        given:
        Point p1 = new Point (0,0)
        Point p1Named = new Point (0,0)
        p1Named.name = "Will"

        expect:
        p1 == p1Named
        p1.hashCode() == p1Named.hashCode() //naming a point doesnt change its hashCode
    }

    def "check asList returns expected results" () {
        given:
        Point p1 = new Point (0,0)

        expect:
        p1.asList() == [0,0,null,null,null,null]  //returns 6D point as list
    }

    def "check asNullTrimmedList returns List with trailing nulls removed returns expected results" () {
        given:
        Point p1 = new Point (0,0, null, 0)

        expect:
        p1.asNullTrimmedList() == [0,0,null,0]  //returns 6D point as list
    }

}
