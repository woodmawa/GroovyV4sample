package benchmark

class Benchmark {

    /**
     * very simple benchmark - you can discard first iterations to allow for hotspot optimisations
     */

    static def timeit = {String message, int count=500, int discard = 20, Closure cl->
        // Warming period
        discard.times { cl() }
        def startTime = System.currentTimeMillis()
        count.times { cl() }
        def deltaTime = System.currentTimeMillis() - startTime
        def average = deltaTime / count
        println "$message:\n\trepeat count: $count \ttime (ms): $deltaTime \taverage (ms): $average"
    }

    static def nanoTimeit = {String message, int count=500, int discard = 20, Closure cl->
        // Warming period
        discard.times { cl() }
        def startTime = System.nanoTime()
        count.times { cl() }
        def deltaTime = (System.nanoTime() - startTime)/1e6
        def average = deltaTime / count
        println "$message:\n\trepeat count: $count \ttime (ms): ${deltaTime.round(4)} \taverage (ms): ${average.round(3)}"
    }
}
