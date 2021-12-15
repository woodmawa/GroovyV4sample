package script

import org.codehaus.groovy.runtime.MetaClassHelper

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.util.function.Function
import java.util.function.Supplier

/**
 * LambdaMetafactory example with closure - works
 */
Closure myClosure = {"hello from closure"}

MethodHandles.Lookup lookup= MethodHandles.lookup()

def delegateImpl = lookup.findVirtual(Closure.class, "call", MethodType.methodType (Object.class))

//now get a callSite for the handle - https://wttech.blog/blog/2020/method-handles-and-lambda-metafactory/
java.lang.invoke.CallSite closureCallSite = LambdaMetafactory.metafactory(
        lookup,
        "get",
        //invokedType: expected signature of the callsite, The parameter types represent the types of capture variables, here invoked arg is Closure and returns Supplier
        //                   -- ret type --   -- invoked type
        MethodType.methodType(Supplier.class, Closure.class),
        // samMthodType: signature and return type of method to be implemented  by the function object, type erasure, Supplier will return an Object
        MethodType.methodType (Object.class),
        //implMethod handle that does the work - the handle for closure call()
        delegateImpl,
        //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        //This may be the same as samMethodType, or may be a specialization of it.
        //supplier method real signature  accepts no params and returns string
        MethodType.methodType(String)
)

MethodHandle closureFactory = closureCallSite.getTarget()

Supplier closureLambda = closureFactory.bindTo(myClosure).invokeWithArguments()
def res = closureLambda.get()
println res


/**
 * LambdaMetafactory example with stad class  - cant get to work with any combinations
 */
class ExampleClass {
    private String value = "hello from getter"

    ExampleClass() {}  //constructor

    String getValue () {return value}
    void setValue (String val) {value = val}
}

ExampleClass instance = new ExampleClass()

MethodHandle getterDelegateImpl = lookup.findVirtual(ExampleClass.class, "getValue", MethodType.methodType (String.class))

java.lang.invoke.CallSite getterCallSite = LambdaMetafactory.metafactory(
         lookup,
        "get",
        //invokedType: expected signature of the callsite, The parameter types represent the types of capture variables, here invoked arg is Closure and returns Supplier
        //                   -- ret type --   -- invoked type -- on bindTo
        MethodType.methodType(Supplier.class, ExampleClass),
       // samMthodType: signature and return type of method to be implemented  by the function object, type erasure, Supplier will return an Object
        MethodType.methodType (Object.class),
        //implMethod handle that does the work - the handle for closure call()
        getterDelegateImpl,
        //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        //This may be the same as samMethodType, or may be a specialization of it.
        //supplier method real signature  accepts no params and returns string
        MethodType.methodType(String.class)
)

MethodHandle classFactory = getterCallSite.getTarget()

Supplier lambda =  classFactory.bindTo(instance).invokeWithArguments()
def ret = lambda.get ()
println ret