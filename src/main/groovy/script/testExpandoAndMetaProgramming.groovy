package script

class MyClass2 extends Expando {
    String name

}

MyClass2 myc = new MyClass2()

myc.name = "william"
myc.dynamic = "dynamic"
myc.metaClass.static.getStaticDynamic = {"static dynamic value"}

//if you know the name of the closure method you can get its value
def res = myc.metaClass.getStaticMetaMethod("getStaticDynamic", [] as Object[]).doMethodInvoke(myc, [] as Object[])
println "myc get static method : [$res]"
println "myc meta methods : " + myc.metaClass.metaMethods.collect {it.name}
println "myc metaClass properties : "+ myc.metaClass.properties.collect {it.name}


myc.properties.each {println it}
