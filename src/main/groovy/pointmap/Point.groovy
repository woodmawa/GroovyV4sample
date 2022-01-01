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
        this.x = Optional.ofNullable (x)
        this.y = Optional.ofNullable (y)
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

    Object getOptionalAxis (String axis) {
        switch (axis) {
            case "x" -> x
            case "y" -> y
            case "z" -> z
            case "t" -> t
            case "u" -> u
            case "v" -> v
            default -> x
        }
    }

    Object getX () {
        x.orElse (null)
    }

    Object getOptionalX () {
        x
    }

    Object getY () {
        y.orElse (null)
    }

    Object getOptionalY () {
        y
    }

    Object getZ () {
        z.orElse (null)
    }

    Object getOptionalZ () {
        z
    }

    Object getT () {
        t.orElse (null)
    }

    Object getOptionalT() {
        t
    }

    Object getU () {
        u.orElse (null)
    }

    Object getOptionalU () {
        u
    }

    Object getV () {
        v.orElse (null)
    }

    Object getOptionalV () {
        v
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

    int compareTo (Point otherPoint) {

        def compareX = x.orElse(null) <=> otherPoint.@x.orElse (null)
        def compareY = y.orElse(null) <=> otherPoint.@y.orElse (null)

        if (compareX < 0) {
            return -1
        } else if (compareX == 0 && compareY <= 0) {
            return -1
        } else if (compareX == 0 && compareY == 0) {
            return 0
        } else if (compareX >= 0 && compareY <= 0) {
            return 1
        } else if (compareX <=0 && compareY > 0) {
            return 1
        }

        int cmp = x.orElse(null) <=> otherPoint.@x.orElse (null) ?:
                y.orElse(null <=> otherPoint.@y.orElse (null)) ?:
                        z.orElse(null <=> otherPoint.@z.orElse (null)) ?:
                                t.orElse(null <=> otherPoint.@t.orElse (null)) ?:
                                        u.orElse(null <=> otherPoint.@u.orElse (null)) ?:
                                                v.orElse(null <=> otherPoint.@v.orElse (null))
        cmp

    }

    String toString() {
        "Point ($x, $y, $z, $t, $u, $v)"
    }
}
