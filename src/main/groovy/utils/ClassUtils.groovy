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
     * @param functionalInterfaceReturnClass
     * @param instance
     * @param sourceMethodName
     * @param sourceMethodArgTypes
     * @return
     */
    static def getLambdaFromReflectionMethod(Class<?> functionalInterfaceReturnClass, Object instance, String sourceMethodName, Class<?>... sourceMethodArgTypes) {
        Method reflectedCall
        String funcionalInterfaceMethodName

        switch (functionalInterfaceReturnClass) {
            case Supplier -> funcionalInterfaceMethodName = "get"
            case Function -> funcionalInterfaceMethodName = "apply"
            case BiFunction -> funcionalInterfaceMethodName = "apply"
            case Consumer -> funcionalInterfaceMethodName = "accept"
            case Predicate -> funcionalInterfaceMethodName = "test"
            case Callable -> funcionalInterfaceMethodName = "call"
            case Runnable -> funcionalInterfaceMethodName = "run"

            default -> funcionalInterfaceMethodName = "apply"
        }

        Class runtimeClazz = instance.getClass()
        def size = sourceMethodArgTypes.size()
        if (sourceMethodArgTypes?.size() > 0 ) {
            reflectedCall    = instance.class.getMethod(sourceMethodName, *sourceMethodArgTypes )
        } else {
            reflectedCall    = instance.class.getMethod(sourceMethodName)
        }

        MethodHandle delegateImplHandle  = lookup.unreflect(reflectedCall)

        MethodType invokedMethodType = MethodType.methodType(functionalInterfaceReturnClass, runtimeClazz)
        MethodType samMethodType = (instance instanceof Closure ) ? MethodType.methodType (Object)
                                                                    : delegateImplHandle.type().dropParameterTypes(0,1).erase()
        MethodType instantiatedMethodType = (instance instanceof Closure ) ? MethodType.methodType (Object)
                                                                            : delegateImplHandle.type().dropParameterTypes(0,1)

        //now get a callSite for the handle - https://wttech.blog/blog/2020/method-handles-and-lambda-metafactory/
        java.lang.invoke.CallSite callSite = LambdaMetafactory.metafactory(
                lookup,                     //calling Ctx for methods
                funcionalInterfaceMethodName,                 //name of the functional interface name to invoke
                invokedMethodType,          // MethodType.methodType(Supplier, Closure ),
                samMethodType,              //MethodType.methodType(Object),              // samMthodType: signature and return type of method to be implemented after type erasure
                delegateImplHandle,         //implMethod handle that does the work - the handle for closure call()
                instantiatedMethodType      //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        )

        MethodHandle factory = callSite.getTarget()

        return factory.bindTo(instance).invokeWithArguments()
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
    static def getLambdaFromStaticReflectionMethod(Class<?> functionalInterfaceClass, Class<?> sourceClazz, String sourceMethodName, Class<?>... sourceMethodArgTypes) {
        Method reflectedCall
        String functionalInterfaceMethodName

        switch (functionalInterfaceClass) {
            case Supplier -> functionalInterfaceMethodName = "get"
            case Function -> functionalInterfaceMethodName = "apply"
            case BiFunction -> functionalInterfaceMethodName = "apply"
            case Consumer -> functionalInterfaceMethodName = "accept"
            case Predicate -> functionalInterfaceMethodName = "test"
            case Callable -> functionalInterfaceMethodName = "call"
            case Runnable -> functionalInterfaceMethodName = "run"

            default -> functionalInterfaceMethodName = "apply"
        }

        Class runtimeClazz = sourceClazz
        Class closClazz = Closure.class

        if (sourceMethodArgTypes?.size() > 0 )
            reflectedCall = runtimeClazz.getMethod(sourceMethodName, *sourceMethodArgTypes )
        else
            reflectedCall = runtimeClazz.getMethod(sourceMethodName )

        MethodHandle delegateImplHandle  = lookup.unreflect(reflectedCall)

        /**
         * weird with closure instantiatedMethodType, and samMethodType seem to need form ()<returnType>
         * if using instance of ordinary class you can get form (<source>)<returnType>
         */
        MethodType invokedMethodType = MethodType.methodType(functionalInterfaceClass)
        MethodType samMethodType =  delegateImplHandle.type().erase()
        MethodType instantiatedMethodType = delegateImplHandle.type()

        /**
         * wont work at mo for static functions to be generated
         */
        //now get a callSite for the handle - https://wttech.blog/blog/2020/method-handles-and-lambda-metafactory/
        java.lang.invoke.CallSite callSite = LambdaMetafactory.metafactory(
                lookup,                     //calling Ctx for methods
                functionalInterfaceMethodName,                 //name of the functional interface name to invoke
                invokedMethodType,          // MethodType.methodType(Supplier, Closure ),
                samMethodType,              //MethodType.methodType(Object),              // samMthodType: signature and return type of method to be implemented after type erasure
                delegateImplHandle,         //implMethod handle that does the work - the handle for closure call()
                instantiatedMethodType      //instantiatedMethodType: signature and return type that should be forced dynamically at invocation.
        )

        MethodHandle factory = callSite.getTarget()

        return ( factory.invokeWithArguments() ).asType(functionalInterfaceClass)
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
