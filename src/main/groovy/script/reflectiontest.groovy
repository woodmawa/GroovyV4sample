package script

import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.reflection.ClassInfo
import org.codehaus.groovy.reflection.ReflectionCache
import org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod
import org.codehaus.groovy.runtime.metaclass.NewInstanceMetaMethod
import org.codehaus.groovy.runtime.metaclass.NewStaticMetaMethod

import java.lang.invoke.SerializedLambda
import java.lang.reflect.Method

class Dummy {
    def someMethod () {println "\thello"}
}
Dummy dummy = new Dummy()


List<Method> lmeths = Dummy.getDeclaredMethods()
lmeths

Method meth =  lmeths.find{it.name == "someMethod" }  //it knows this is method of Dummy

//create cached class
//def cinfo = ClassInfo.getClassInfo (Dummy)
//def cclazz = new CachedClass( Dummy, cinfo )

//use this its quicker
CachedClass refCclazz = ReflectionCache.getCachedClass(Dummy)


//create a cached method for cached class
CachedMethod cm = CachedMethod.find (meth) //new CachedMethod (refCclazz, meth)
CachedClass dec = cm.getDeclaringClass()
println "methods declaring class is > " + dec.name

println "cached method invoke printed:"
cm.invoke(dummy)

NewInstanceMetaMethod newMetaMethod = new NewInstanceMetaMethod (cm)

println newMetaMethod.dump()

def decCacheMethodName = newMetaMethod.getName()
def ans = newMetaMethod.isStatic()
ans = newMetaMethod.getModifiers()
CachedClass decCacheClass = newMetaMethod.getCachedMethod().declaringClass
Class theClass = decCacheClass.getTheClass()
//BUG here def theClazz =  newMetaMethod.getDeclaringClass().getTheClass()  //bug here, have to get
println "dec class for new metaMethod is " + decCacheClass + " ( " + theClass + ")"

MetaMethod mm = newMetaMethod.getCachedMethod()
println "meta method invoke : "
mm.invoke(dummy)
//ClosureMetaMethod myCmm = new ClosureMetaMethod ('Dummy', Dummy, {println "anon closure meta method"}, cm)
//myCmm.invoke (dummy, [] as Object[])

NewStaticMetaMethod newStaticMetaMethod = new NewStaticMetaMethod (cm)
assert newStaticMetaMethod.isStatic()

Closure clos = () -> println "my lambda"
def closClazz = clos.getClass()
clos()



//cm = new CachedMethod()