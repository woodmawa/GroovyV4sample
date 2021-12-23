package utils

import spock.lang.Specification

class SpockTest extends Specification {

    def "one plus one should equal 2 " () {
        expect:
        1 + 1 == 2
    }

    def "collection methods results checking " () {
        expect:
        def list = [1,2,3,3,4,5,5,6]

        list.findResults { it % 2} == [1, 0, 1, 1, 0, 1, 1, 0]
        list.take(3) == [1,2,3]
        list.findAll {it % 2 == 0 } == [2,4,6]
        list.takeWhile {it < 4 } == [1,2,3,3]
        list.dropWhile {it < 4} == [4,5,5,6]
        list.grep {it % 2 == 0} == [2,4,6]
        list.inject (0) { sum, it -> sum + it} == 29
        list.toSet() == new HashSet([1,2,3,3,4,5,5,6])
        list.unique() == [1,2,3,4,5,6]
        list - [4,5,6] == [1,2,3]  // removes all numbers from the sublist
        list.split { it > 3} [[1,2,3,3], [4,5,5,6]]
    }
}
