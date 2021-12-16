package lambda

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import lamda.ExampleBeanClass

import java.lang.invoke.CallSite
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.util.function.Function
import java.util.function.Supplier


/**
 * LambdaMetaclass description :
 "When the target of the CallSite returned from this method is invoked, the resulting function objects are instances of a class which implements the interface named by the return type of invokedType, declares a method with the name given by invokedName and the signature given by samMethodType. It may also override additional methods from Object.

 Parameters:
 caller - Represents a lookup context with the accessibility privileges of the caller. Specifically, the lookup context must have private access privileges. When used with invokedynamic, this is stacked automatically by the VM.
 invokedName - The name of the method to implement. When used with invokedynamic, this is provided by the NameAndType of the InvokeDynamic structure and is stacked automatically by the VM.
 invokedType - The expected signature of the CallSite. The parameter types represent the types of capture variables; the return type is the interface to implement. When used with invokedynamic, this is provided by the NameAndType of the InvokeDynamic structure and is stacked automatically by the VM. In the event that the implementation method is an instance method and this signature has any parameters, the first parameter in the invocation signature must correspond to the receiver.
 samMethodType - Signature and return type of method to be implemented by the function object.
 implMethod - A direct method handle describing the implementation method which should be called (with suitable adaptation of argument types, return types, and with captured arguments prepended to the invocation arguments) at invocation time.
 instantiatedMethodType - The signature and return type that should be enforced dynamically at invocation time. This may be the same as samMethodType, or may be a specialization of it.
 Returns:
 a CallSite whose target can be used to perform capture, generating instances of the interface named by invokedType"
 *
 */
class InvokeDynamicTest {

    ExampleBeanClass bean
    MethodHandles.Lookup callerCtx
    MethodHandle implementationDelegate

    @BeforeEach
    void init() {
        bean = new ExampleBeanClass()
        callerCtx = MethodHandles.lookup()
    }

    @Test
    void dummyTest () {
        assert true
        assert bean
        assert callerCtx
    }

    /**
     * get value by generating a Supplier interface reference
     */
    @Test
    void accessViaNonStaticBeanGetterAsSupplier () {

        Method reflected = ExampleBeanClass.class.getDeclaredMethod("getValue")
        implementationDelegate = callerCtx.unreflect (reflected)

        //if you want bind an instance value to your lambda, you have to include those types in the InvokedType signature, and then do the bind
        CallSite site = LambdaMetafactory.metafactory(
                callerCtx,
                "get",  //functional interface method name
                MethodType.methodType (Supplier.class, ExampleBeanClass),       //invoked type
                MethodType.methodType (Object.class),                           // SAM method type signature of required interface
                implementationDelegate,                                         //code thats doing the real work
                MethodType.methodType (String)                                  //expected return type of instantiated method, expected as subtype of SAM type
        )

        MethodHandle factory = site.getTarget().bindTo (bean)                   //invokedType defined bean class, so now bind one here

        Supplier func = (Supplier) factory.invokeWithArguments()

        assert func.get() == "hello from getter"
    }

    /**
     * get value by generating a functional interface reference
     */
    @Test
    void accessViaNonStaticBeanGetterAsFunctionWithBoundInstance () {

        Method reflected = ExampleBeanClass.class.getDeclaredMethod("getValue")
        implementationDelegate = callerCtx.unreflect (reflected)

        //if you want bind an instance value to your lambda, you have to include those types in the InvokedType signature, and then do the bind
        CallSite site = LambdaMetafactory.metafactory(
                callerCtx,
                "apply",                                       //functional interface method name
                MethodType.methodType (Function.class, ExampleBeanClass),       //invoked type
                MethodType.methodType (Object.class),                           // SAM method type signature of required interface
                implementationDelegate,                                         //code thats doing the real work
                MethodType.methodType (String)                                  //expected return type of instantiated method, expected as subtype of SAM type
        )

        MethodHandle factory = site.getTarget().bindTo (bean)                   //invokedType defined bean class, so now bind one here

        Function func = (Function) factory.invokeWithArguments()

        assert func.apply() == "hello from getter"
    }

    /**
     * alternative implementation where you call accept with bean
     * in this case the instantiatedMethodType has to show the return and the param to be passed
     * This relies on the fact that first parameter in the delegateImpl is the hidden this param at args[0]
     * so you need to simulate that by passing the 'this' instance to the delegate
     */
    @Test
    void accessViaNonStaticBeanGetterAsFunctionPassingBeanAsParam () {

        Method reflected = ExampleBeanClass.class.getDeclaredMethod("getValue")
        implementationDelegate = callerCtx.unreflect (reflected)

        MethodType erasedType = implementationDelegate.type().erase()
        MethodType instantiatedType = implementationDelegate.type()
        MethodType instantiatedType2 = MethodType.methodType (String)
        MethodType instantiatedType3 = MethodType.methodType (String, ExampleBeanClass)
        assert instantiatedType == instantiatedType3

        //if you want bind an instance value to your lambda, you have to include those types in the InvokedType signature, and then do the bind
        CallSite site = LambdaMetafactory.metafactory(
                callerCtx,
                "apply",                                       //functional interface method name
                MethodType.methodType (Function.class),                         //invoked type
                erasedType,                                                         // SAM method type signature of required interface
                implementationDelegate,                                         //code thats doing the real work
                //instantiatedType //(this works)                               //expected return type of instantiated method, expected as subtype of SAM type
                MethodType.methodType (String, ExampleBeanClass)                //have to use this form as we are going call function with bean param
        )

        MethodHandle factory = site.getTarget()                //invokedType defined bean class, so now bind one here

        Function func = (Function) factory.invokeWithArguments()

        assert func.apply(bean) == "hello from getter"
    }

    @Test
    void accessViaStaticBeanGetter () {

        Method reflected = ExampleBeanClass.class.getDeclaredMethod("getStaticValue")
        implementationDelegate = callerCtx.unreflect (reflected)

        //as we are invoking static type we don't need to bind an instance to the site for this test case
        CallSite site = LambdaMetafactory.metafactory(
                callerCtx,
                "get",  //functional interface method name
                MethodType.methodType (Supplier.class),       //invoked type, doesnt need bean class for static invocation
                MethodType.methodType (Object.class),         // SAM method type signature of required interface
                implementationDelegate,                       //code thats doing the real work
                MethodType.methodType (String)                //expected return type of instantiated method, expected as subtype of SAM type
        )

        MethodHandle factory = site.getTarget()

        Supplier func = (Supplier) factory.invokeWithArguments()

        assert func.get() == "static string value"
    }
}
