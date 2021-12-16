package utils

import org.codehaus.groovy.reflection.ReflectionUtils
import org.codehaus.groovy.runtime.MetaClassHelper
import org.codehaus.groovy.runtime.metaclass.ClosureStaticMetaMethod

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.Callable
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

class ClassUtils {

    private static MethodHandles.Lookup lookup = MethodHandles.lookup()

    /**
     * generates a functional interface from a callSite
     *
     * @param returnClass
     * @param instance
     * @param sourceMethodName
     * @param args
     * @return
     */
    static def getLambdaFromReflectionMethod(Class<?> returnClass, Object instance, String sourceMethodName, Object... args) {
        Method reflectedCall
        String methodName

        switch (returnClass) {
            case Supplier -> methodName = "get"
            case Function -> methodName = "accept"
            case BiFunction -> methodName = "apply"
            case Consumer -> methodName = "accept"
            case Predicate -> methodName = "test"
            case Callable -> methodName = "call"
            case Runnable -> methodName = "run"

            default -> methodName = "accept"
        }

        Class runtimeClazz = instance.getClass()
        Class closClazz = Closure.class
        if (instance instanceof Closure ){
            reflectedCall = Closure.class.getMethod ("call")
        } else {
            reflectedCall = instance.class.getMethod(methodName, MetaClassHelper.castArgumentsToClassArray (args) )
        }
        MethodHandle delegateImplHandle  = lookup.unreflect(reflectedCall)

        Class clazz = instance instanceof Closure ? Closure.class : instance.getClass()

        /**
         * weird with closure instantiatedMethodType, and samMethodType seem to need form ()<returnType>
         * if using instance of ordinary class you can get form (<source>)<returnType>
         */
        MethodType instantiatedMethodType = (instance instanceof Closure ) ? MethodType.methodType (Object): delegateImplHandle.type()
        MethodType samMethodType = (instance instanceof Closure ) ? MethodType.methodType (Object): delegateImplHandle.type().erase()
        ArrayList argsTypeList = MetaClassHelper.castArgumentsToClassArray (args)

        MethodType invokedMethodType
        if (argsTypeList.size() == 0) {
            invokedMethodType = MethodType.methodType(returnClass, clazz)
        } else {
            invokedMethodType = MethodType.methodType(returnClass, clazz, *argsTypeList)
        }

        MethodType samType = MethodType.methodType(Object )
        MethodType insType = MethodType.methodType(String )

        /**
         * wont work at mo for static functions to be generated
         */
        //now get a callSite for the handle - https://wttech.blog/blog/2020/method-handles-and-lambda-metafactory/
        java.lang.invoke.CallSite callSite = LambdaMetafactory.metafactory(
                lookup,                     //calling Ctx for methods
                methodName,                 //name of the functional interface name to invoke
                invokedMethodType,          // MethodType.methodType(Supplier, Closure ),
                samMethodType,              //MethodType.methodType(Object),              // samMthodType: signature and return type of method to be implemented after type erasure
                delegateImplHandle,         //implMethod handle that does the work - the handle for closure call()
                instantiatedMethodType      //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        )

        MethodHandle factory = callSite.getTarget()

        return ( factory.bindTo(instance).invokeWithArguments() ).asType(returnClass)
    }

    /**
     * generates a functional interface from a callSite
     *
     * @param returnClass
     * @param instance
     * @param sourceMethodName
     * @param args
     * @return
     */
    static def getLambdaFromStaticReflectionMethod(Class<?> returnClass, Class<?> clazz, String sourceMethodName, Object... args) {
        Method reflectedCall
        String methodName

        switch (returnClass) {
            case Supplier -> methodName = "get"
            case Function -> methodName = "accept"
            case BiFunction -> methodName = "apply"
            case Consumer -> methodName = "accept"
            case Predicate -> methodName = "test"
            case Callable -> methodName = "call"
            case Runnable -> methodName = "run"

            default -> methodName = "accept"
        }

        Class runtimeClazz = clazz
        Class closClazz = Closure.class

        reflectedCall = runtimeClazz.getMethod(methodName, MetaClassHelper.castArgumentsToClassArray (args) )

        MethodHandle delegateImplHandle  = lookup.unreflect(reflectedCall)

        /**
         * weird with closure instantiatedMethodType, and samMethodType seem to need form ()<returnType>
         * if using instance of ordinary class you can get form (<source>)<returnType>
         */
        MethodType instantiatedMethodType = MethodType.methodType (Object)
        MethodType samMethodType =  delegateImplHandle.type().erase()
        ArrayList argsTypeList = MetaClassHelper.castArgumentsToClassArray (args)

        MethodType invokedMethodType
        if (argsTypeList.size() == 0) {
            invokedMethodType = MethodType.methodType(returnClass)
        } else {
            invokedMethodType = MethodType.methodType(returnClass, *argsTypeList)
        }

        /**
         * wont work at mo for static functions to be generated
         */
        //now get a callSite for the handle - https://wttech.blog/blog/2020/method-handles-and-lambda-metafactory/
        java.lang.invoke.CallSite callSite = LambdaMetafactory.metafactory(
                lookup,                     //calling Ctx for methods
                methodName,                 //name of the functional interface name to invoke
                invokedMethodType,          // MethodType.methodType(Supplier, Closure ),
                samMethodType,              //MethodType.methodType(Object),              // samMthodType: signature and return type of method to be implemented after type erasure
                delegateImplHandle,         //implMethod handle that does the work - the handle for closure call()
                instantiatedMethodType      //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        )

        MethodHandle factory = callSite.getTarget()

        return ( factory.invokeWithArguments() ).asType(returnClass)
    }

    /**
     *
     * @param obj
     * @param name
     * @return
     */
    public static Object getFieldValue(Object obj, String name) {
        Class<?> clazz = obj.getClass()
        Field field = clazz.getDeclaredField(name)
        if (field != null) {
            try {
                ReflectionUtils.makeAccessible(field)
                return field.get(obj)
            }
            catch (Exception e) {
                return null
            }
        }
        return null
    }

    public static Object getStaticFieldValue(Class<?> clazz, String name) {

        //List l = Arrays.stream (clazz.getDeclaredFields()).filter (f -> Modifier.isStatic (f.getModifiers())).map (f -> f.name).collect (Collectors.toList())

        Field field = clazz.getDeclaredField(name)
        if (field != null) {
            ReflectionUtils.makeAccessible(field)
            try {
                return field.get(clazz)
            } catch (IllegalAccessException ignored) {
            }
        }
        return null
    }

    public static Object getStaticMetaClassFieldValue(Class<?> clazz,  String name) {

        List l //= Arrays.stream (clazz.metaClass.getDeclaredFields()).filter (f -> Modifier.isStatic (f.getModifiers())).map (f -> f.name).collect (Collectors.toList())

        MetaClass thisMc = clazz.metaClass

        PropertyValue properties = clazz.metaClass.getMetaPropertyValues().find{it.name == 'properties'  }
        List<MetaBeanProperty> MBprops= properties.getValue()
        Map m = MBprops.findAll{ Modifier.isPublic(it.modifiers)}.collectEntries{[(it.name), it.getProperty(clazz)] }

        PropertyValue expandoProperties = clazz.metaClass.getMetaPropertyValues().find{it.name == 'expandoProperties'}
        List<MetaBeanProperty> MBprops2= properties.getValue()
        Map m2 = MBprops.findAll{Modifier.isPublic(it.modifiers)}.collectEntries{[(it.name), it.getProperty(clazz)] }

        ExpandoMetaClass.ExpandoMetaProperty whatIs = clazz.metaClass.static
        boolean isStatic = whatIs.static
        //def val = whatIs.getProperty("AddedStaticMethod")
        def unk = whatIs.@this$0
        Map m3 = unk.@expandoProperties
        Map m4 = thisMc.@expandoProperties

        MetaBeanProperty asm = m4['addedStaticMethod']
        def val2 = asm.getProperty(clazz)
        ClosureStaticMetaMethod getter = asm.getter
        Closure clos = getter.getClosure()
        def val = clos()
        boolean staticprop = Modifier.isStatic(asm.modifiers)

        MetaBeanProperty ap  = m4['addedProperty']
        def val3 = ap.getProperty(clazz)
        boolean staticprop2 = Modifier.isStatic(ap.modifiers)


        Field field = clazz.getDeclaredField(name)
        if (field != null) {
            ReflectionUtils.makeAccessible(field)
            try {
                return field.get(clazz)
            } catch (IllegalAccessException ignored) {
            }
        }
        return null
    }

    public static boolean isPublicStatic(Method m) {
        final int modifiers = m.getModifiers()
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)
    }

    public static boolean isPublicStatic(MetaMethod m) {
        final int modifiers = m.getModifiers()
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)
    }

    public static boolean isPublicStatic(Field f) {
        final int modifiers = f.getModifiers()
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)
    }

}
