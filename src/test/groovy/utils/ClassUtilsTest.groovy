package utils

import lamda.ExampleBeanClass
import org.junit.jupiter.api.Test

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

class ClassUtilsTest {

    @Test
    void generateSupplierFromClosure () {

        Closure myClos = {"hello"}

        Supplier supplier = ClassUtils.getLambdaFromReflectionMethod(Supplier, myClos, 'call')
        supplier.get() == "hello"

    }

    @Test
    void generateSupplierFromBeanClassInstance () {

        ExampleBeanClass bean = new ExampleBeanClass()

        Supplier supplier = ClassUtils.getLambdaFromReflectionMethod(Supplier, bean, 'getValue')
        supplier.get() == "hello from getter"

    }

    /**
     * slightly unnatural but you can get a functional interface for a getter,
     * when you invoke just invoke with empty args list -
     * bit using Supplier interface feels better fit 
     */
    @Test
    void generateFunctionFromBeanClassInstance () {

        ExampleBeanClass bean = new ExampleBeanClass()

        Function function = ClassUtils.getLambdaFromReflectionMethod(Function, bean, 'getValue')
        function.apply() == "hello from getter"

    }

    @Test
    void generateSupplierFromBeanClassStaticMethod  () {

        ExampleBeanClass bean = new ExampleBeanClass()

        Supplier supplier = ClassUtils.getLambdaFromStaticReflectionMethod(Supplier, bean.getClass(), 'getStaticValue')
        supplier.get() == "static string value"

    }


    @Test
    void generatePredicateFromBeanClassInstance () {

        ExampleBeanClass bean = new ExampleBeanClass()

        Predicate predicate = ClassUtils.getLambdaFromReflectionMethod(Predicate, bean, 'test', Object)
        predicate.test(10) == true

    }

    @Test
    void generateSupplierFromBeanClassInstance2 () {

        ExampleBeanClass bean = new ExampleBeanClass()

        MethodHandles.Lookup lookup = MethodHandles.lookup()
        MethodHandle delegateImplHandle = lookup.findVirtual(ExampleBeanClass,'getValue',MethodType.methodType(String))

        MethodType invokedMethodType = MethodType.methodType(Supplier, ExampleBeanClass)
        MethodType sam = MethodType.methodType (Object.class)
        MethodType samMethodTypeNoDrop = delegateImplHandle.type().erase()
        MethodType samMethodType = delegateImplHandle.type().dropParameterTypes(0,1).erase()
        MethodType ins = MethodType.methodType (String.class)
        MethodType instantiatedMethodType = delegateImplHandle.type().dropParameterTypes(0,1)


        java.lang.invoke.CallSite callSite = LambdaMetafactory.metafactory(
                lookup,                     //calling Ctx for methods
                'get',                 //name of the functional interface name to invoke
                invokedMethodType,          // MethodType.methodType(Supplier, Closure ),
                samMethodType,              //MethodType.methodType(Object),              // samMthodType: signature and return type of method to be implemented after type erasure
                delegateImplHandle,         //implMethod handle that does the work - the handle for closure call()
                instantiatedMethodType      //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        )

        MethodHandle factory = callSite.getTarget()

        Supplier supplier = factory.bindTo(bean).invokeWithArguments()
        supplier.get() == "hello from getter"
    }

    @Test
    void generateFunctionFromBeanClassInstance2 () {

        ExampleBeanClass bean = new ExampleBeanClass()

        MethodHandles.Lookup lookup = MethodHandles.lookup()
        MethodHandle delegateImplHandle = lookup.findVirtual(ExampleBeanClass,'getValue',MethodType.methodType(String))

        MethodType invokedMethodType = MethodType.methodType(Function, ExampleBeanClass)
        MethodType sam = MethodType.methodType (Object.class)
        MethodType samMethodTypeNoDrop = delegateImplHandle.type().erase()
        MethodType samMethodType = delegateImplHandle.type().dropParameterTypes(0,1).erase()
        MethodType ins = MethodType.methodType (String.class)
        MethodType instantiatedMethodType = delegateImplHandle.type().dropParameterTypes(0,1)


        java.lang.invoke.CallSite callSite = LambdaMetafactory.metafactory(
                lookup,                     //calling Ctx for methods
                'apply',                 //name of the functional interface name to invoke
                invokedMethodType,          // MethodType.methodType(Supplier, Closure ),
                samMethodType,              //MethodType.methodType(Object),              // samMthodType: signature and return type of method to be implemented after type erasure
                delegateImplHandle,         //implMethod handle that does the work - the handle for closure call()
                instantiatedMethodType      //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        )

        MethodHandle factory = callSite.getTarget()

        Function function = factory.bindTo(bean).invokeWithArguments()
        function.apply() == "hello from getter"
    }

    @Test
    void generatePredicateFromBeanClassInstance2 () {

        ExampleBeanClass bean = new ExampleBeanClass()

        MethodHandles.Lookup lookup = MethodHandles.lookup()

        MethodHandle delegateImplHandle = lookup.findVirtual(ExampleBeanClass,'test',MethodType.methodType(boolean.class, Object))

        MethodType invokedMethodType = MethodType.methodType(Predicate, ExampleBeanClass)
        MethodType sam = MethodType.methodType (boolean.class, Object)
        MethodType samMethodType = delegateImplHandle.type().dropParameterTypes(0,1).erase()
        MethodType ins = MethodType.methodType (boolean.class, Object)
        MethodType instantiatedMethodType = delegateImplHandle.type().dropParameterTypes(0,1)


        java.lang.invoke.CallSite callSite = LambdaMetafactory.metafactory(
                lookup,                     //calling Ctx for methods
                'test',                 //name of the functional interface name to invoke
                invokedMethodType,          // MethodType.methodType(Predicate, ExampleBeanClass ),
                samMethodType,              //MethodType.methodType(Object),              // samMthodType: signature and return type of method to be implemented after type erasure
                delegateImplHandle,         //implMethod handle that does the work - the handle for closure call()
                instantiatedMethodType      //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        )

        MethodHandle factory = callSite.getTarget()

        Predicate predicate = factory.bindTo(bean).invokeWithArguments()
        predicate.test(10) == true

    }
}
