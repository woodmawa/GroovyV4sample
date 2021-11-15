package extensible

import org.codehaus.groovy.reflection.ReflectionUtils


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
}
