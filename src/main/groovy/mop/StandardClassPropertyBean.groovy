package mop

import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod

class StandardClassPropertyBean extends MetaProperty {

    Class declaringClass
    String propertyName

    MetaMethod getter
    MetaMethod setter

    CachedMethod cachedGetMethod
    CachedMethod cachedSetMethod

    StandardClassPropertyBean (Object instance, String property) {
        super ()
        this.declaringClass = instance.getClass()
        this.propertyName = property


        String getMethodName = "get"+ property[0].toLowerCase() + property.substring(1)
        if (declaringClass.hasProperty(property))
            cachedGetMethod = new CachedMethod(instance::"$getMethodName")

        getter = new ReflectionMetaMethod (cachedGetMethod)

        String setMethodName = "set"+ property[0].toLowerCase() + property.substring(1)
        if (declaringClass.hasProperty(property))
            cachedSetMethod = new CachedMethod(instance::"$setMethodName")

        setter = new ReflectionMetaMethod (cachedSetMethod)

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
