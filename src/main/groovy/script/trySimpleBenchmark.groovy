package script

import benchmark.Benchmark

import java.util.stream.Stream

Benchmark.nanoTimeit ("for loop ", 1000) {
    int sum = 0
    for (int i=0; i<100; i++) {
        sum++
    }
}


Benchmark.nanoTimeit ("groovy each{} ", 1000) {
    int sum = 0

    100.each {sum++}
}

Benchmark.nanoTimeit ("stream process ", 1000) {
    int sum = 0

    Stream.of(0..9).forEach(i -> sum+=1)
}