package script

class MyClass2 extends Expando {
    String name

}

MyClass2 myc = new MyClass2()

myc.name = "william"
myc.dynamic = "dynamic"
myc.metaClass.static.getStaticDynamicMethod = {"static dynamic closure value"}
myc.metaClass.static.staticDynamicProperty = "static dynamic property value"

//if you know the name of the closure method you can get its value
def res = myc.metaClass.getStaticMetaMethod("getStaticDynamicMethod", [] as Object[]).doMethodInvoke(myc, [] as Object[])
println "myc get static method : [$res]"

//no means to get this property
def propRes = myc.metaClass.getStaticMetaMethod("getStaticDynamicProperty", [] as Object[]).doMethodInvoke(myc, [] as Object[])
println "myc get static property : [$propRes]"

println "myc meta methods : " + myc.metaClass.metaMethods.collect {it.name}
println "myc metaClass properties : "+ myc.metaClass.properties.collect {it.name}


myc.properties.each {println it}
