package pointmap

import groovy.transform.EqualsAndHashCode
import groovy.transform.MapConstructor
import groovy.transform.ToString

/***
 *
 * Point represents a point in 6D space.  It can be used as a key in a PointMap to store a value with the space
 *
 * Points can be optionally named.  the name does not form part of the hashCode for the point
 *
 * all the variables are stored as optionals internally.  but just requesting one of the dimensions can unpack the Optional and return a null
 * There are methods to directly access the optional dimensions as stored
 *
 * Points are nearly expected to be Immutable as they form the key in a PointMap
 *
 * PointMap allows you to retrieve any object value associated with this point.  The value can be a closure, value  value whos superclass
 * is Object , and null
 *
 * Points provide an accept method which takes a value instance, and a closure - part of visitor pattern starting from a PointMap.  The closure is invoked
 * is invoked with This (point), and the value.  PointMap will collect the returns of all visits
 *
 * point dimensions can be any object, but expectation is that they support the Comparable interface (implement compareTo())
 *
 * Copyright [2022] [Will Woodman]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ***/

@ToString
@EqualsAndHashCode (excludes =  "name")
class Point {
    Optional<String> name = Optional.empty()  //you can have a named point - but not part of its hashCode
    private final Optional<Object> x = Optional.empty()  //1 dimension
    private final Optional<Object> y = Optional.empty()  //2 dimension
    private final Optional<Object> z = Optional.empty()  //3 dimension
    private final Optional<Object> t = Optional.empty()  //4 dimension
    private final Optional<Object> u = Optional.empty()  //5 dimension
    private final Optional<Object> v = Optional.empty()  //6 dimension

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
    def accept (final Object value, final Closure visitor) {
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
        name.orElse("unNamed")
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

    List asList() {
        [getX(), getY(), getZ(), getT(), getU(), getV()].asImmutable()
    }

    List asNullTrimmedList() {
        List l = [getX(), getY(), getZ(), getT(), getU(), getV()]

        List reversed = []
        boolean trailingNulls = true
        for (i in -1..-6) {
            if (trailingNulls && l[i] == null ) {
                continue
            } else if (trailingNulls) {
                trailingNulls = false
            }
            reversed << l[i]
        }
        List trimmedList = reversed.reverse()
        trimmedList.asImmutable()
    }

    List asListOfOptionals() {
        [getOptionalX(), getOptionalY(), getOptionalZ(), getOptionalT(), getOptionalU(), getOptionalV()].asImmutable()
    }

    Map asMap () {
        [x:getX(), y:getY(), z:getZ(), t:getT(), u:getU(), v:getV()].asImmutable()
    }

    String toString() {
        "Point ([${getName()}]: $x, $y, $z, $t, $u, $v)"
    }
}
