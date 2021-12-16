package script


import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.function.Supplier

//moved ExampleBeanClass to its on class file

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
        //supplier method real signature  accepts no params and returns string
        MethodType.methodType(String)
)

MethodHandle closureFactory = closureCallSite.getTarget()

Supplier closureLambda = closureFactory.bindTo(myClosure).invokeWithArguments()
def res = closureLambda.get()
println res


/**
 * LambdaMetafactory example with standard class  - cant get to work with any combinations
 */


ExampleBeanClass instance = new ExampleBeanClass()  //now from its own class file

MethodHandle getterDelegateImpl = lookup.findVirtual(ExampleBeanClass.class, "getValue", MethodType.methodType (String.class))

/*java.lang.invoke.CallSite getterFunctionCallSite = LambdaMetafactory.metafactory(
        lookup,
        "apply",
        //invokedType: expected signature of the callsite, The parameter types represent the types of capture variables, here invoked arg is Closure and returns Supplier
        MethodType.methodType(Function.class),
        // samMthodType: signature and return type of method to be implemented  by the function object, type erasure, Supplier will return an Object
        getterDelegateImpl.type().erase(),  //MethodType.methodType (Object.class, ExampleClass),
        //implMethod handle that does the work - the handle for closure call()
        getterDelegateImpl,
        //This may be the same as samMethodType, or may be a specialization of it.
        //supplier method real signature  accepts no params and returns string
        getterDelegateImpl.type()//methodType.methodType(String.class)
)

MethodHandle classFunctionFactory = getterFunctionCallSite.getTarget()

Function funcLambda =  (Function) classFunctionFactory.invokeWithArguments()
def fret = funcLambda.apply (instance)
println fret*/

java.lang.invoke.CallSite getterCallSite = LambdaMetafactory.metafactory(
         lookup,
        "get",
        //invokedType: expected signature of the callsite, The parameter types represent the types of capture variables, here invoked arg is Closure and returns Supplier
        MethodType.methodType(Supplier.class, ExampleBeanClass.class),
        //MethodType.methodType(Supplier.class, ExampleClass),
       // samMthodType: signature and return type of method to be implemented  by the function object, type erasure, Supplier will return an Object
        MethodType.methodType (Object.class),
        //implMethod handle that does the work - the handle for closure call()
        getterDelegateImpl,
         //This may be the same as samMethodType, or may be a specialization of it.
        //supplier method real signature  accepts no params and returns string
        MethodType.methodType(String.class)
)

MethodHandle classFactory = getterCallSite.getTarget()

Supplier suppLambda =  classFactory.bindTo(instance).invokeWithArguments()
def sret = suppLambda.get ()
println sret