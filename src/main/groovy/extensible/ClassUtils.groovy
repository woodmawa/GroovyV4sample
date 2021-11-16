package extensible


import org.codehaus.groovy.reflection.ReflectionUtils
import org.codehaus.groovy.runtime.metaclass.ClosureStaticMetaMethod

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import  java.util.stream.Collectors

class ClassUtils {

    public static Object getStaticFieldValue(Class<?> clazz, String name) {

        List l = Arrays.stream (clazz.getDeclaredFields()).filter (f -> Modifier.isStatic (f.getModifiers())).map (f -> f.name).collect (Collectors.toList())
        l

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
        Map m = MBprops.findAll{Modifier.isPublic(it.modifiers)}.collectEntries{[(it.name), it.getProperty(clazz)] }

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
}
