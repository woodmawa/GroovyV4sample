package script

import org.codehaus.groovy.runtime.MetaClassHelper

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.util.function.Function
import java.util.function.Supplier



class ExampleClass {
    private String value = "hello"

    ExampleClass() {}  //constructor

    String getValue () {return value}
    void setValue (String val) {value = val}
}

ExampleClass instance = new ExampleClass()

MethodHandles.Lookup lookup= MethodHandles.lookup()


// 2. Creates a MethodType
//MethodType sourceMethodType = MethodType.methodType(String.class, []);
//                                            ^-----------^        ^----------^
//                                             return type         argument class

// 3. Find the MethodHandle
//MethodHandle metaHandle = lookup.findVirtual(ExampleClass.class, "getValue", sourceMethodType);
//                                       ^----------^               ^-------------^
//                                            |                     name of method
//                             class from which method is accessed

// 4. Invoke the method
//String strVal
//strVal = (String) metaHandle.bindTo(instance).invokeWithArguments()  // this works
//                                  ^----^                  ^----^
//                                    |                    argument
//                       instance your class  to invoke the method on

// strVal = (String) metaHandle.invokeExact(instance);  //throws java.lang.UnsupportedOperationException: cannot reflectively invoke MethodHandle



//use reflection to get method - then unreflect to get handle
Method reflectedCall = instance.class.getMethod("getValue" )
MethodHandle handle  = lookup.unreflect(reflectedCall)

//get handle by lookup
MethodHandle virtRef = lookup.findVirtual(ExampleClass.class, "getValue", MethodType.methodType (String.class))

assert handle.toString() == virtRef.toString()

MethodType mt = handle.type()

//getter can be treated as Function where the compiler injects this reference as hiddden arg
MethodType factoryMethodType = MethodType.methodType(Function.class, ExampleClass)

java.lang.invoke.CallSite callSite = LambdaMetafactory.metafactory(
         lookup,
        //invoked name, name of method on Supplier interface
        "getValue",
        //invokedType: expected signature of the callsite, The parameter types represent the types of capture variables, here invoked arg is Closure and returns Supplier
        //                   -- ret type --   -- invoked type -- on bindTo
        factoryMethodType,
        //MethodType.methodType(Supplier.class, []),
        // samMthodType: signature and return type of method to be implemented  by the function object, type erasure, Supplier will return an Object
        MethodType.methodType (Object.class),
        //implMethod handle that does the work - the handle for closure call()
        virtRef, //handle,  //lookup.findVirtual(ExampleClass.class, "getValue", MethodType.methodType (String.class)),
        //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        //This may be the same as samMethodType, or may be a specialization of it.
        //supplier method real signature  accepts no params and returns string
        MethodType.methodType(String.class)
)

MethodHandle factory = callSite.getTarget()

def lambda =  factory.bindTo(instance).invokeWithArguments().asType (String)
def ret = lambda ()
println ret