package script

import closure.SampleJavaClosure

SampleJavaClosure sample = new SampleJavaClosure()

//before we start
println "before state : " + sample.getCountRef().get()

//then - get the java closure implementation reference
println "after calling closure " + sample.getSampleClosure().call()

