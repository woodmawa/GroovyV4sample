package mop

import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.reflection.ReflectionUtils
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod

import java.lang.reflect.Method

class StandardClassPropertyBean extends MetaProperty {

    Class declaringClass
    String propertyName

    MetaMethod getter
    MetaMethod setter

    CachedMethod cachedGetMethod
    CachedMethod cachedSetMethod

    StandardClassPropertyBean (Object instance, String property) {
        super (property ,instance.getClass())
        this.declaringClass = instance.getClass()
        this.propertyName = property


        boolean hasProp = instance.hasProperty(property)
        if (hasProp) {
            String getMethodName = "get"+ property[0].toUpperCase() + property.substring(1)

            Method getMethod = instance.getClass().getMethod(getMethodName, [] as Class[])

            if (getMethod)
                cachedGetMethod = new CachedMethod(getMethod)
            else
                throw new MissingMethodException(getMethodName)

            getter = new ReflectionMetaMethod (cachedGetMethod)

            String setMethodName = "set"+ property[0].toUpperCase() + property.substring(1)
            Method setMethod = instance.getClass().getMethods().find{it.name == setMethodName}

            if (setMethod) {
                cachedSetMethod = new CachedMethod(setMethod)
            }

            setter = new ReflectionMetaMethod (cachedSetMethod)

        } else {
            throw new MissingPropertyException(property)
        }


    }

    /**
     * Get the getter method.
     */
    public MetaMethod getGetter() {
        return getter
    }
    /**
     * Get the setter method.
     */
    public MetaMethod getSetter() {
        return setter
    }

    @Override
    Object getProperty(Object object) {
        return null
    }

    @Override
    void setProperty(Object object, Object newValue) {

    }
}
