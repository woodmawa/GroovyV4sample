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
        rowZero[0] == p1
        rowZero[1] == p2

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

    def "validate row and column sizing " () {
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


        then:
         rowEntries
         rowCount == 2
         colCount == 2
         zIndexCount == 1
    }
}
