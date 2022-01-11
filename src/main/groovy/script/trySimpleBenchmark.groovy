package script

import benchmark.Benchmark

Benchmark.nanoTimeit ("for loop ", 100) {
    for (int i=0; i<100; i++) {
        return  i
    }
}

Benchmark.nanoTimeit ("groovy each{} ", 100) {
    100.each {return it}
}