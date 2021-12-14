package script

import org.codehaus.groovy.runtime.MetaClassHelper

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.util.function.Function
import java.util.function.Supplier

Closure exampleClosure = {"hello from closure"}

MethodHandles.Lookup lookup= MethodHandles.lookup()
MethodHandle closHandle = lookup.findVirtual(Closure.class, "call", MethodType.methodType (Object.class))
java.lang.invoke.CallSite closureCallSite = LambdaMetafactory.metafactory(
        lookup,
        "get",  //invoked name, name of method on Supplier interface
        //             -- req interface type -- from class     factory type required, get Supplier from Closure
        MethodType.methodType(Supplier.class, Closure.class),
        // samMthodType: signature and return type of method to be implemented  by the function object, type erasure, Supplier will return an Object
        MethodType.methodType (Object.class),
        //implMethod handle that does the work - the handle for closure call()
        lookup.findVirtual(Closure.class, "call", MethodType.methodType (Object.class)),
        //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        //This may be the same as samMethodType, or may be a specialization of it.
        MethodType.methodType (Supplier.class)
)

MethodHandle closFactory = closureCallSite.getTarget()
//binds instances as first arg of a method handle without invoking it
Supplier closAsSupplier = closFactory.bindTo(exampleClosure).invokeWithArguments()

//now invoke the lambda
def closRes = closAsSupplier.get()      //this works!

/**
 * now try generate a Supplier lambda for a concrete class
 */
class ExampleClass {
    private String value = "hello"

    ExampleClass() {}  //constructor

    String getValue () {return value}
    void setValue (String val) {value = val}
}

ExampleClass instance = new ExampleClass()

MethodHandle getter = lookup.unreflect (ExampleClass.class.getMethod('getValue'))
MethodHandle getterDirect = lookup.findVirtual(ExampleClass.class, "getValue", MethodType.methodType (String.class))

java.lang.invoke.CallSite methodCallSite = LambdaMetafactory.metafactory(
         lookup,
        //invoked name, name of method on Supplier interface
        "get",
        MethodType.methodType(Supplier.class, ExampleClass),  //, ExampleClass.class
        // samMthodType: signature and return type of method to be implemented  by the function object, type erasure, Supplier will return an Object
        MethodType.methodType (Object.class),
        //implMethod handle that does the work - the handle for closure call()
        getter,
        getter.type()
)

MethodHandle factory = methodCallSite.getTarget()
Supplier lambda =  factory.bindTo(instance).invokeWithArguments()

//now invoke it
def ret = lambda.get (instance)
println ret