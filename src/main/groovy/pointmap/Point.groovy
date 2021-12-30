package pointmap

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includeFields = true)
@ToString
class Point {
    Optional<Object> xIndex = Optional.empty()
    Optional<Object> yIndex = Optional.empty()
    Optional<Object> zIndex = Optional.empty()

    Point (x, y, z = null) {
        xIndex = Optional.of (x)
        yIndex = Optional.of (y)
        zIndex = Optional.ofNullable (z)
    }

    Point (List idx) {
        assert "co-ordinate list cannot be null", idx
        assert "must have non nullable x and y co-ordinates",  idx.size() > 2

        xIndex = Optional.of (idx?[0])
        yIndex = Optional.of (idx?[1])
        zIndex = Optional.ofNullable (idx?[2])
    }

    Object getX () {
        xIndex.orElse (new Object())
    }

    Object getY () {
        yIndex.orElse (new Object())
    }

    Object getZ () {
        zIndex.orElse (new Object())
    }
}
