package pointmap

import groovy.transform.EqualsAndHashCode
import groovy.transform.MapConstructor
import groovy.transform.ToString

/**
 *
 * 2021
 * @Author Will Woodman
 */
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

    Point (final List idx) {
        assert idx, "co-ordinate list cannot be null"
        assert idx.size() > 2, "must have non nullable x and y co-ordinates"

        x = Optional.of (idx?[0])
        y = Optional.of (idx?[1])
        z = Optional.ofNullable (idx?[2])
        t = Optional.ofNullable (idx?[3])
        u = Optional.ofNullable (idx?[4])
        v = Optional.ofNullable (idx?[5])
    }

    Point (final Map midx) {
        assert midx, "co-ordinate initialiser map cannot be null"
        assert midx.containsKey('x') && midx.containsKey('y'), "expecting minimum of (lowercase) 'x' and 'y' co-ordinate map entries "

        name = Optional.ofNullable(midx[name])
        x = Optional.of (midx[x])
        y = Optional.of (midx[y])
        z = Optional.ofNullable (midx[z])
        t = Optional.ofNullable (midx[t])
        u = Optional.ofNullable (midx[u])
        v = Optional.ofNullable (midx[v])
    }

    /**
     * for visitor pattern normally invoked from PointMap iteration with a visitor closure
     *
     * @param yield {Point p, Object value -> ...}  - a function that takes a Closure and returns the result
     * @return
     */
    def accept (Object value, Closure visitor) {
        assert visitor

        if (value == null)
            return null
        else
            //invoke the visitor with this Point and value stored with the Point
            visitor.call (this, value)
    }

    /**
     * returns the Optional axes reference value by selecting from axis name, default returns x index
     * @param String axis - name of axes you want the result to return, as an Optional
     * @return Optional value of point for selected axis
     */
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
        //equality doesnt include name when doing the check, just the position
        assert other instanceof Point, "other must be an instance of Point"

        x == other.optionalX &&
        y == other.optionalY &&
        z == other.optionalZ &&
        t == other.optionalT &&
        u == other.optionalU &&
        v == other.optionalV
    }

    /**
     * used for normal sort, compares by position axes, starting with x as most significant axis, then y, then z, then...
     * @param otherPoint to compare to this point
     * @return -1 (this is less than), 0 (equal), 1 (this is greater than)
     */
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
