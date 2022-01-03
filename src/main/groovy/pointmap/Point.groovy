package pointmap

import groovy.transform.EqualsAndHashCode
import groovy.transform.MapConstructor
import groovy.transform.ToString

@ToString
@EqualsAndHashCode (excludes =  "name")
@MapConstructor
class Point {
    Optional<String> name = Optional.empty()  //you can have a named point - but not part of its hashCode
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
        assert idx, "co-ordinate list cannot be null"
        assert idx.size() > 2, "must have non nullable x and y co-ordinates"

        x = Optional.of (idx?[0])
        y = Optional.of (idx?[1])
        z = Optional.ofNullable (idx?[2])
        t = Optional.ofNullable (idx?[3])
        u = Optional.ofNullable (idx?[4])
        v = Optional.ofNullable (idx?[5])

    }

    Optional getOptionalAxis (String axis) {
        String lowercase = axis.toLowerCase()
        Optional axisResult = switch (lowercase) {
            case "x" -> this.optionalX
            case "y" -> this.optionalY
            case "z" -> this.optionalZ
            case "t" -> this.optionalT
            case "u" -> this.optionalU
            case "v" -> this.optionalV
            default -> this.optionalX
        }
        axisResult
    }

    String getName() {
        name.orElse("unknown")
    }

    void setName (String pointName) {
        name = Optional.of (pointName)
    }

    Object getX () {
        x.orElse (null)
    }

    Optional getOptionalX () {
        x
    }

    Object getY () {
        y.orElse (null)
    }

    Optional getOptionalY () {
        y
    }

    Object getZ () {
        z.orElse (null)
    }

    Optional getOptionalZ () {
        z
    }

    Object getT () {
        t.orElse (null)
    }

    Optional getOptionalT() {
        t
    }

    Object getU () {
        u.orElse (null)
    }

    Optional getOptionalU () {
        u
    }

    Object getV () {
        v.orElse (null)
    }

    Optional getOptionalV () {
        v
    }

    //equals on object expects Object - so dont create alternative
    @Override
    boolean equals (Object other) {
        assert other instanceof Point, "other must be an instance of Point"

        optionalX == other.optionalX &&
        optionalY == other.optionalY &&
        optionalZ == other.optionalZ &&
        optionalT == other.optionalT &&
        optionalU == other.optionalU &&
        optionalV == other.optionalV
    }

    int compareTo (Point otherPoint) {
        int cmp = optionalX.orElse(null) <=> otherPoint.optionalX.orElse (null) ?:
                optionalY.orElse(null) <=> otherPoint.optionalY.orElse (null) ?:
                        optionalZ.orElse(null) <=> otherPoint.optionalZ.orElse (null) ?:
                                optionalT.orElse(null) <=> otherPoint.optionalT.orElse (null) ?:
                                        optionalU.orElse(null) <=> otherPoint.optionalU.orElse (null) ?:
                                               optionalV.orElse(null) <=> otherPoint.optionalV.orElse (null)
        cmp

    }

    String toString() {
        "Point ($x, $y, $z, $t, $u, $v)"
    }
}
