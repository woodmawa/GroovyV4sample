package extensible

import org.codehaus.groovy.reflection.ReflectionUtils


import java.lang.reflect.Field

class ClassUtils {

    public static Object getStaticFieldValue(Class<?> clazz, String name) {

        Field field = ReflectionUtils.findField (clazz, name)
        if (field != null) {
            ReflectionUtils.makeAccessible(field)
            try {
                return field.get(clazz);
            } catch (IllegalAccessException ignored) {
            }
        }
        return null
    }
}
