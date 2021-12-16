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
    void accessViaNonStaticBeanSupplierGetter () {

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
    void accessViaNonStaticBeanFunctionTypeGetter () {

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
