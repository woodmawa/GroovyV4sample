package script

import org.codehaus.groovy.runtime.MethodClosure

import java.lang.reflect.Modifier

def lambda = () -> println "hello"

assert lambda instanceof Closure
println lambda.class
lambda ()

class AAA {}
AAA.metaClass.myProp = 42  //is this done as static as its on class ?


AAA example = new AAA()

List l = example.metaClass.properties.find{it.name == "myProp"}.collect()

MetaProperty prop =  l[0]

println prop.dump()  //returns ThreadManagedMetaBeanProperty
assert Modifier.isStatic (prop.modifiers)  //fails it's not static internally

