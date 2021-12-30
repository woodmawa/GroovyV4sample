package pointmap

import spock.lang.Specification

class PointMapTest extends Specification {

    def "add two points as a row" () {
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


        List<Point> rowZero  = map.getRow(0)
        List<Point> colOne  = map.getColumn(1)

        then:
        rowZero
        rowZero.size() == 2
        rowZero[0] == p1
        rowZero[1] == p2

        colOne
        colOne.size() == 2
        colOne[0] == p2
        colOne[1] == p4
    }
}
