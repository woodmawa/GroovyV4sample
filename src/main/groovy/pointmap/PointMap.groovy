package pointmap

import java.util.concurrent.ConcurrentHashMap

class PointMap {
    private ConcurrentHashMap multiMap = new ConcurrentHashMap()

    Point origin = new Point (0,0,0)

    void clear () {
        multiMap.clear()
    }

    void put (Point point, def value ) {
        multiMap.put (point, value)
    }

    void put (x,y,z, def value) {
        Point point = new Point (x,y,z)
        put (point, value)
    }

    def get (Point point) {
        multiMap[Point]
    }

    List<Point> getRow (rowNumber) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> rowEntries = keys.iterator().findAll { Point p -> p.x == rowNumber}
        rowEntries.asImmutable()
    }

    List<Point> getColumn (colNumber) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> colEntries = keys.iterator().findAll { Point p -> p.y == colNumber}
        colEntries.asImmutable()
    }

    List<Point> getZindex (zIdx) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> zEntries = keys.iterator().findAll { Point p -> p.z == zIdx}
        zEntries.asImmutable()
    }
}
