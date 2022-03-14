package script

import static org.apache.groovy.ginq.GinqGroovyMethods.GQ
import groovy.ginq.transform.GQ

def numbers = [0, 1, 2]

//annotation to a method
@groovy.ginq.transform.GQ
def ginq (list) {
    from n in list
    select n
}

//use static method as well works!
def result = GQ {
    from n in numbers
    select n
}.toList()

println result
println ginq (numbers)

@GQ(parallel=true)
def ginq2(x) {
    from n in [1, 2, 3]
    where n < x
    select n
}

println ginq2(3).toList()

result = GQ {
    from n in [1, 2, 2, 3, 3, 3]
    select distinct(n)
}
println "distinct " + result.toList()

result = GQ {
    from n1 in [1, 2, 3]
    innerjoin n2 in [1, 3] on n1 == n2
    select n1, n2
}

println "inner join" + result

result = GQ {
    from n1 in [2, 3, 4]
    rightjoin n2 in [1, 2, 3] on n1 == n2
    select n1, n2
}
println "right join" + result

result = GQ {
    from n1 in [1, 2, 3]
    leftjoin n2 in [2, 3, 4] on n1 == n2
    select n1, n2
}

println "left join" + result

result = GQ {
    from n in [1, 1, 3, 3, 6, 6, 6]
    groupby n
    select n, count(n)
}

println "groupBy " + result

//this does run, even if compiler doesnt like it !
/*result = GQ {
    from v in (
            from n in [1, 2, 3]
    select n
    )
    select v
}
println "nested  " + result*/