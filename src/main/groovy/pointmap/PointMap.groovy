package pointmap

import java.util.concurrent.ConcurrentHashMap
import static java.util.Comparator.comparing

class PointMap {
    @Delegate
    private ConcurrentHashMap<Point, Object> multiMap = new ConcurrentHashMap()

    /**
     * optional doesnt implement comparable - local closure to do the job
     */
    private Closure compareOptionals = {Optional first, Optional second ->
        def firstVal = first.orElse(null)
        def secondVal = second.orElse(null)

        if (firstVal && secondVal)
            assert firstVal.getClass() == secondVal.getClass()

        firstVal <=> secondVal
    }

    Point origin = new Point (0,0,0)

    void clear () {
        multiMap.clear()
    }

    def put (final Point point, def value ) {
        multiMap.put (point, value)
    }

    def put (x,y,z, def value) {
        final Point point = new Point (x,y,z)
        put (point, value)
    }

    def get (final Point point) {
        def value  = multiMap.get(point)
        value
    }

    List<Point> getRowEntryList (final def rowNumber) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> rowEntries = keys.iterator().findAll { Point p -> p.x == rowNumber}
        rowEntries.sort(false){Point a, Point b ->
            compareOptionals(a.getOptionalAxis("x"),b.getOptionalAxis("x")) ?:
                    compareOptionals(a.getOptionalAxis("y"), b.getOptionalAxis("y")) ?:
                            compareOptionals(a.getOptionalAxis("z"), b.getOptionalAxis("z")) ?:
                                    compareOptionals(a.getOptionalAxis("t"), b.getOptionalAxis("t")) ?:
                                            compareOptionals(a.getOptionalAxis("u"), b.getOptionalAxis("u")) ?:
                                                    compareOptionals(a.getOptionalAxis("v"), b.getOptionalAxis("v"))

        }.asImmutable()
    }

    long getRowCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@x != Optional.empty()}.groupBy {Point p -> p.@x}.size()
        count
    }

    List<Point> getColumnEntryList (colNumber) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> colEntries = keys.iterator().findAll { Point p -> p.y == colNumber}
        List<Point> sorted = colEntries.sort(false){Point a, Point b ->
             compareOptionals(a.getOptionalAxis("x"),b.getOptionalAxis("x")) ?:
                    compareOptionals(a.getOptionalAxis("y"), b.getOptionalAxis("y")) ?:
                            compareOptionals(a.getOptionalAxis("z"), b.getOptionalAxis("z")) ?:
                                    compareOptionals(a.getOptionalAxis("t"), b.getOptionalAxis("t")) ?:
                                            compareOptionals(a.getOptionalAxis("u"), b.getOptionalAxis("u")) ?:
                                                    compareOptionals(a.getOptionalAxis("v"), b.getOptionalAxis("v"))

        }.asImmutable()
        sorted
    }

    long getColumnCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@y != Optional.empty()}.groupBy {Point p -> p.@y}.size()
        count
    }

    List<Point> getZindexEntryList (zIdx) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> zEntries = keys.iterator().findAll { Point p -> p.z == zIdx}
        zEntries.sort(false){Point a, Point b ->
            compareOptionals(a.getOptionalAxis("x"),b.getOptionalAxis("x")) ?:
                    compareOptionals(a.getOptionalAxis("y"), b.getOptionalAxis("y")) ?:
                            compareOptionals(a.getOptionalAxis("z"), b.getOptionalAxis("z")) ?:
                                    compareOptionals(a.getOptionalAxis("t"), b.getOptionalAxis("t")) ?:
                                            compareOptionals(a.getOptionalAxis("u"), b.getOptionalAxis("u")) ?:
                                                    compareOptionals(a.getOptionalAxis("v"), b.getOptionalAxis("v"))

        }.asImmutable()
    }

    long getZindexCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@z != Optional.empty()}.groupBy {Point p -> p.@z}.size()
        count
    }

    List<Point> getTindexEntryList (tIdx) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> tEntries = keys.iterator().findAll { Point p -> p.t == tIdx}
        tEntries.sort(false){Point a, Point b ->
            compareOptionals(a.getOptionalAxis("x"),b.getOptionalAxis("x")) ?:
                    compareOptionals(a.getOptionalAxis("y"), b.getOptionalAxis("y")) ?:
                            compareOptionals(a.getOptionalAxis("z"), b.getOptionalAxis("z")) ?:
                                    compareOptionals(a.getOptionalAxis("t"), b.getOptionalAxis("t")) ?:
                                            compareOptionals(a.getOptionalAxis("u"), b.getOptionalAxis("u")) ?:
                                                    compareOptionals(a.getOptionalAxis("v"), b.getOptionalAxis("v"))

        }.asImmutable()
    }

    long getTindexCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@t != Optional.empty()}.groupBy {Point p -> p.@t}.size()
        count
    }

    List<Point> getUindexEntryList (uIdx) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> uEntries = keys.iterator().findAll { Point p -> p.u == uIdx}
        uEntries.sort(false){Point a, Point b ->
            compareOptionals(a.getOptionalAxis("x"),b.getOptionalAxis("x")) ?:
                    compareOptionals(a.getOptionalAxis("y"), b.getOptionalAxis("y")) ?:
                            compareOptionals(a.getOptionalAxis("z"), b.getOptionalAxis("z")) ?:
                                    compareOptionals(a.getOptionalAxis("t"), b.getOptionalAxis("t")) ?:
                                            compareOptionals(a.getOptionalAxis("u"), b.getOptionalAxis("u")) ?:
                                                    compareOptionals(a.getOptionalAxis("v"), b.getOptionalAxis("v"))

        }.asImmutable()
    }

    long getUindexCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@u != Optional.empty()}.groupBy {Point p -> p.@u}.size()
        count
    }

    List<Point> getVindexEntryList (vIdx) {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        List<Point> vEntries = keys.iterator().findAll { Point p -> p.v == vIdx}
        vEntries.sort(false){Point a, Point b ->
            compareOptionals(a.getOptionalAxis("x"),b.getOptionalAxis("x")) ?:
                    compareOptionals(a.getOptionalAxis("y"), b.getOptionalAxis("y")) ?:
                            compareOptionals(a.getOptionalAxis("z"), b.getOptionalAxis("z")) ?:
                                    compareOptionals(a.getOptionalAxis("t"), b.getOptionalAxis("t")) ?:
                                            compareOptionals(a.getOptionalAxis("u"), b.getOptionalAxis("u")) ?:
                                                    compareOptionals(a.getOptionalAxis("v"), b.getOptionalAxis("v"))

        }.asImmutable()
    }

    long getVindexCount () {
        ConcurrentHashMap.KeySetView keys = multiMap.keySet()

        long count = keys.iterator().findAll { Point p -> p.@v != Optional.empty()}.groupBy {Point p -> p.@v}.size()
        count
    }

    /**
     * for visitor pattern normally invoked from PointMap iteration with a visitor closure
     *
     * @param visit {Point p -> ...}  - a function that takes a function and returns the result
     * @return
     */
    def visit  (Closure visitor) {
        assert visitor

        //todo - need to think whats expected from this
        List<Point> points = multiMap.keySet().asList()

        List<Point> sorted = points.sort(false){Point a, Point b ->
            compareOptionals(a.getOptionalAxis("x"),b.getOptionalAxis("x")) ?:
                    compareOptionals(a.getOptionalAxis("y"), b.getOptionalAxis("y")) ?:
                            compareOptionals(a.getOptionalAxis("z"), b.getOptionalAxis("z")) ?:
                                    compareOptionals(a.getOptionalAxis("t"), b.getOptionalAxis("t")) ?:
                                            compareOptionals(a.getOptionalAxis("u"), b.getOptionalAxis("u")) ?:
                                                    compareOptionals(a.getOptionalAxis("v"), b.getOptionalAxis("v"))

        }
        // cant use collect closure here as context would be the list, and then couldnt access the multimap -
        // use normal iteration instead
        def visitResults = []
        for (point in sorted) {
            visitResults << point.accept(multiMap[point], visitor)
        }

        visitResults
    }
}
