package script

import groovy.inspect.Inspector
import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.reflection.ReflectionCache
import org.codehaus.groovy.runtime.MetaClassHelper
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod
import org.codehaus.groovy.runtime.metaclass.NewMetaMethod
import org.codehaus.groovy.runtime.metaclass.NewStaticMetaMethod
import utils.ClassUtils

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.util.function.Function
import java.util.function.Supplier

class Dummy {
    def someMethod () { println "\t(someMethod) hello"}
}

Dummy.metaClass.dynMethod = {println "\t(metaClass: dynMethod) hello"}  //extend the class

Dummy dummy = new Dummy()

Function<Object, String> func = {it -> "\t(function) hello"}
println func.apply()

Supplier<String> supplier = {-> "\t(supplier) hello"}
println supplier.get()

//use java reflection here
List<Method> lmeths = Dummy.getDeclaredMethods()
lmeths

//uses Java reflection Method
Method meth =  lmeths.find{it.name == "someMethod" }  //it knows this is method of Dummy

//create cached class
//def cinfo = ClassInfo.getClassInfo (Dummy)
//def cclazz = new CachedClass( Dummy, cinfo )

//use this its quicker
CachedClass refCclazz = ReflectionCache.getCachedClass(Dummy)


//uses groovy types here - create a cached method for cached class
CachedMethod cm = CachedMethod.find (meth) //new CachedMethod (refCclazz, meth)
CachedClass dec = cm.getDeclaringClass()
println "methods declaring class is > " + dec.name

println "groovy cached method invoke printed:"
cm.invoke(dummy)

//groovy provides both of these methods
MetaMethod newMetaMethod = new NewMetaMethod (cm)
NewInstanceMetaMethod newInstanceMetaMethodMetaMethod = new NewInstanceMetaMethod (cm)

println "dump of newMetaMethod " + newMetaMethod.dump()
println "dump of newInstanceMetaMethod " + newInstanceMetaMethodMetaMethod.dump()

def decCacheMethodName = newInstanceMetaMethodMetaMethod.getName()
def ans = newInstanceMetaMethodMetaMethod.isStatic()
ans = newInstanceMetaMethodMetaMethod.getModifiers()
CachedClass decCacheClass = newInstanceMetaMethodMetaMethod.getCachedMethod().declaringClass
Class theClass = decCacheClass.getTheClass()
//BUG here def theClazz =  newMetaMethod.getDeclaringClass().getTheClass()  //bug here, have to get
println "dec class for new metaMethod is " + decCacheClass + " ( " + theClass + ")"

MetaMethod nmm = newMetaMethod.getCachedMethod()
println "newMeta method invoke call printed  : "
nmm.invoke(dummy)

MetaMethod nimm = newInstanceMetaMethodMetaMethod.getCachedMethod()
println "metaInstanceMeta method invoke call printed  : "
nimm.invoke(dummy)

//ClosureMetaMethod myCmm = new ClosureMetaMethod ('Dummy', Dummy, {println "anon closure meta method"}, cm)
//myCmm.invoke (dummy, [] as Object[])

NewStaticMetaMethod newStaticMetaMethod = new NewStaticMetaMethod (cm)
assert newStaticMetaMethod.isStatic()

Closure clos = () -> "\t(lambda expr) my lambda"  //implicit cast from lambda to closure
def closClazz = clos.getClass()
println "closure> $clos.class,  delegate:$clos.delegate, owner:$clos.owner, in script where this:$this with ${this.getClass()}"
println "direct closure invoke printed  : "
println clos()

println "clos methods " + clos.owner.class.methods.collect{it.name}

Closure refclos = dummy::someMethod
refclos()

//MethodClosure mclos2 = new MethodClosure (dummy,  "someMethod")
//println mclos2.method

def getMethodArgTypes(String name, Object[] argTypes) {
    //just converts arg values into classes of each arg
    Class[] classes = MetaClassHelper.castArgumentsToClassArray (argTypes)
}

List l = getMethodArgTypes ("dynMethod", [1, "two"] as Object[])

def dyn = dummy.metaClass.getMetaMethod("dynMethod")
dyn.invoke(dummy)

Inspector ins = new Inspector(clos)
println ins.propertyInfo


//try this
Closure myClos = {"dynamic method returned String result"}

//object to invoke reflected call on
//                                                       returnType        obj instance      methodName
//Function lambda = getLambdaFromReflectionMethod (Function, myClos, 'call' )
Supplier<String> lambda = ClassUtils.getLambdaFromReflectionMethod(Supplier<String>, myClos, 'call' )
String val = lambda.get ()  //invoke get() on Supplier
println val

