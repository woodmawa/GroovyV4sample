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
        multiMap[point]
    }

    List<Point> getRowEntryList (rowNumber) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> rowEntries = keys.iterator().findAll { Point p -> p.x == rowNumber}
        rowEntries.asImmutable()
    }

    long getRowCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@x != Optional.empty()}.groupBy {Point p -> p.@x}.size()
        count
    }

    List<Point> getColumnEntryList (colNumber) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> colEntries = keys.iterator().findAll { Point p -> p.y == colNumber}
        colEntries.asImmutable()
    }

    long getColumnCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@y != Optional.empty()}.groupBy {Point p -> p.@y}.size()
        count
    }

    List<Point> getZindexEntryList (zIdx) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> zEntries = keys.iterator().findAll { Point p -> p.z == zIdx}
        zEntries.asImmutable()
    }

    long getZindexCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@z != Optional.empty()}.groupBy {Point p -> p.@z}.size()
        count
    }

    List<Point> getTindexEntryList (tIdx) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> tEntries = keys.iterator().findAll { Point p -> p.t == tIdx}
        tEntries.asImmutable()
    }

    long getTindexCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@t != Optional.empty()}.groupBy {Point p -> p.@t}.size()
        count
    }

    List<Point> getUindexEntryList (uIdx) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> uEntries = keys.iterator().findAll { Point p -> p.u == uIdx}
        uEntries.asImmutable()
    }

    long getUindexCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@u != Optional.empty()}.groupBy {Point p -> p.@u}.size()
        count
    }

    List<Point> getVindexEntryList (vIdx) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> vEntries = keys.iterator().findAll { Point p -> p.v == vIdx}
        vEntries.asImmutable()
    }

    long getVindexCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@v != Optional.empty()}.groupBy {Point p -> p.@v}.size()
        count
    }
}
