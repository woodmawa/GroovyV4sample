package script

import groovy.inspect.Inspector
import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.reflection.ReflectionCache
import org.codehaus.groovy.runtime.MetaClassHelper
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod
import org.codehaus.groovy.runtime.metaclass.NewMetaMethod
import org.codehaus.groovy.runtime.metaclass.NewStaticMetaMethod

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.util.function.Supplier

class Dummy {
    def someMethod () { println "\t(someMethod) hello"}
}

Dummy.metaClass.dynMethod = {println "\t(metaClass: dynMethod) hello"}  //extend the class

Dummy dummy = new Dummy()


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

//get reflection Method for closures "call" method name
 Method reflectedCall = Closure.class.getMethod ("call")


def getLambdaFromReflectionMethod(Class<?> returnClass, Object instance, String methodName, Object[] args) {
    Method reflectedCall
    MethodHandles.Lookup lkup= MethodHandles.lookup()

    Class runtimeClazz = instance.getClass()
    Class closClazz = Closure.class
    if (instance instanceof Closure ){
        reflectedCall = Closure.class.getMethod (methodName)
    } else {
        reflectedCall = instance.class.getMethod(methodName, MetaClassHelper.castArgumentsToClassArray (args) )
    }
    MethodHandle handle  = lkup.unreflect(reflectedCall)

    Class clazz = instance instanceof Closure ? Closure.class : instance.getClass()

    //now get a callSite for the handle - https://wttech.blog/blog/2020/method-handles-and-lambda-metafactory/
    java.lang.invoke.CallSite callSite = LambdaMetafactory.metafactory(
            //method handle lookup to use
            lkup,
            //invoked name, name of method on Supplier interface
            "get",
            //expected signature of the callsite, invoked type, here invoked arg is Closure and returns Supplier
            //                   -- ret type --   -- invoked type -- on bindTo
            MethodType.methodType(returnClass, clazz),
            // signature and return type of method to be implemented  by the function object, type erasure, Supplier will return an Object
            MethodType.methodType (Object.class),
            //implMethod handle that does the work - the handle for closure call()
            handle,
            //signature and return type that should be forced dynamically at invocation.  supplier method real signature  accepts no params and returns string
            MethodType.methodType(returnClass)
    )

    return callSite.getTarget().bindTo(instance).invokeWithArguments().asType(returnClass)
}

//object to invoke reflected call on
Supplier<String> lambda = getLambdaFromReflectionMethod (Supplier<String>, myClos, 'call' )
String val = lambda ()  //invoke get() on Supplier
println val

