package pointmap

import spock.lang.Specification

class PointMapTest extends Specification {

    def "add multiple points as two rows" () {
        given:
        Point p1 = new Point (0,0)
        Point p2 = new Point (0,1)
        Point p3 = new Point (1,0)
        Point p4 = new Point (1,1)
        Point p5 = new Point (1,1,1)

        PointMap map = new PointMap ()


        when:
        map.put (p1, "will")

        map.put (p2, "woodman")
        map.put (p3, "maz")
        map.put (p4, "woodman")
        map.put (p5, "all at home")


        List<Point> rowZero  = map.getRowEntryList(0)
        List<Point> colOne  = map.getColumnEntryList(1)
        List<Point> tOne  = map.getTindexEntryList(1)

        then:
        rowZero
        rowZero.size() == 2
        rowZero == [p1, p2]

        and:
        colOne
        colOne.size() == 3
        colOne[0] == p2
        colOne[1] == p4
    }

    def "save entry and get it back from PointMap" () {
        given:
        Point p1 = new Point (0,0)
        PointMap map = new PointMap ()

        when:
        map.put (p1, "william")
        def result = map.get (new Point(0,0))

        then:
        result == "william"
    }

    def "add rows using String variable instead of number " () {
        given:
        Point p1 = new Point("a", 0)
        Point p2 = new Point("a", 1)
        Point p3 = new Point("b", 0)
        Point p4 = new Point("b", 1)
        Point p5 = new Point("b", 1, 1)

        PointMap map = new PointMap()

        when:
        map.put(p1, "will")
        map.put(p2, "woodman")
        map.put(p3, "maz")
        map.put(p4, "woodman")
        map.put (p5, "all at home")


        def rowEntries = map.getRowEntryList("a")
        def rowCount = map.getRowCount()
        def colCount = map.getColumnCount()
        def zIndexCount = map.getZindexCount()

        def cols = map.getColumnEntryList(1)

        then:
         rowEntries
         rowCount == 2
         colCount == 2
         zIndexCount == 1
    }

    def "test visitor on PointMap " () {
        given:
        Point p1 = new Point(0, 0)
        Point p2 = new Point(0, 1)
        Point p3 = new Point(1, 0)
        Point p4 = new Point(1, 1)

        PointMap map = new PointMap()

        map.put (p1,"a")
        map.put (p2,"b")
        map.put (p3,"c")
        map.put (p4,"d")

        Closure visitor = {point, value -> value} //every entry in pointmap will be called with this
        def result = map.visit (visitor)

        expect:
        result == ["a","b","c","d"]
    }

    def "adding multiple values via the same key should overwrite  "() {
        given :
        Point p1 = new Point(0, 0)

        PointMap map = new PointMap()

        map.put (p1,"a")
        map.put (p1,"b")

        expect:
        map.size() == 1
        map.get (p1) == "b"


    }

    def "verify natural sorting of points" () {
        given:
        Point p1 = new Point(1, 1)
        Point p2 = new Point(1, 0)
        Point p3 = new Point(0, 0)
        Point p4 = new Point(0, 1)

        PointMap map = new PointMap()

        map.put (p1,[1,1])
        map.put (p2,[1,0])
        map.put (p3,[0,0])
        map.put (p4,[0,1])

        expect:
        map.getSortedKeySet() == [p3,p4,p2,p1 ]

    }
}
