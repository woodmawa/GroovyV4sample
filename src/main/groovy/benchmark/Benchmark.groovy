package benchmark

class Benchmark {

    /**
     * very simple benchmark - you can discard first iterations to allow for hotspot optimisations
     */

    def timeit = {String message, int count=500, int discard = 20, Closure cl->
        // Warming period
        discard.times { cl() }
        def startTime = System.currentTimeMillis()
        count.times { cl() }
        def deltaTime = System.currentTimeMillis() - startTime
        def average = deltaTime / count
        println "$message:\tcount: $count \ttime: $deltaTime \taverage: $average"
    }

    def nanoTimeit = {String message, int count=500, int discard = 20, Closure cl->
        // Warming period
        discard.times { cl() }
        def startTime = System.nanoTime()
        count.times { cl() }
        def deltaTime = System.nanoTime() - startTime
        def average = (deltaTime / count)/1e6
        println "$message:\tcount: $count \ttime: $deltaTime \taverage: $average"
    }
}
