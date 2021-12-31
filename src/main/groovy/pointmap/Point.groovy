package pointmap

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode (includeFields = true)
class Point {
    private Optional<Object> x = Optional.empty()  //1 dimension
    private Optional<Object> y = Optional.empty()  //2 dimension
    private Optional<Object> z = Optional.empty()  //3 dimension
    private Optional<Object> t = Optional.empty()  //4 dimension
    private Optional<Object> u = Optional.empty()  //5 dimension
    private Optional<Object> v = Optional.empty()  //6 dimension

    Point (x, y, z = null, t = null, u =null, v = null) {
        this.x = Optional.of (x)
        this.y = Optional.of (y)
        this.z = Optional.ofNullable (z)
        this.t = Optional.ofNullable (t)
        this.u = Optional.ofNullable (u)
        this.v = Optional.ofNullable (v)
    }

    Point (List idx) {
        assert "co-ordinate list cannot be null", idx
        assert "must have non nullable x and y co-ordinates",  idx.size() > 2

        x = Optional.of (idx?[0])
        y = Optional.of (idx?[1])
        z = Optional.ofNullable (idx?[2])
        t = Optional.ofNullable (idx?[3])
        u = Optional.ofNullable (idx?[4])
        v = Optional.ofNullable (idx?[5])

    }

    Object getX () {
        x.orElse (new Object())
    }

    Object getY () {
        y.orElse (new Object())
    }

    Object getZ () {
        z.orElse (new Object())
    }

    Object getT () {
        t.orElse (new Object())
    }

    Object getU () {
        u.orElse (new Object())
    }

    Object getV () {
        v.orElse (new Object())
    }

    //equals on object expects Object - so dont create alternative
    @Override
    boolean equals (Object other) {
        assert other instanceof Point, "other must be an instance of Point"

        this.@x == other.@x &&
        this.@y == other.@y &&
        this.@z == other.@z &&
        this.@t == other.@t &&
        this.@u == other.@u &&
        this.@v == other.@v
    }

    String toString() {
        "Point ($x, $y, $z, $t, $u, $v)"
    }
}
