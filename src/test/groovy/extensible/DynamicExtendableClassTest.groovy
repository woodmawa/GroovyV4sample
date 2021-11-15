package extensible

import org.codehaus.groovy.runtime.HandleMetaClass
import org.codehaus.groovy.runtime.MethodClosure
import org.junit.jupiter.api.Test

import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows


class DynamicExtendableClassTest {


    @Test
    void testDeclaredStaticStuff () {

         assertEquals ("static method returning string", DynamicExtendableClass.getDeclaredMethodStaticString())
        assertEquals ("declared static string",  DynamicExtendableClass.declaredStaticString)
        assert DynamicExtendableClass.metaClass.getClass() == ExpandoMetaClass
    }

    @Test
    void testExtendedMetaClassStuff () {

        //assert DynamicExtendableClass.metaClass.getClass() == HandleMetaClass

        DynamicExtendableClass testInstance = new DynamicExtendableClass()
        assert DynamicExtendableClass.metaClass.getClass() == ExpandoMetaClass

        assertEquals ("added property to class metaClass", testInstance.addedProperty)
        assertEquals ("added closure as static method", testInstance.getStaticAddedMethod())  //calls getStaticAddedMethod - groovy trick
        assertEquals ("added closure as method", testInstance.addedMethod)  //works.  calls getAddedMethod - groovy trick for getXxx as property
        assertEquals ("added closure as static method", DynamicExtendableClass.staticAddedMethod )  //works class static class Closure

    }

    @Test
    void testMetaClassPropertiesAndMethods () {

        assert DynamicExtendableClass.metaClass.getClass() == ExpandoMetaClass

        assertEquals (["declaredStaticString", "class", "declaredMethodStaticString", "addedProperty", "addedMethod"].sort(), DynamicExtendableClass.metaClass.properties.collect {it.name}.sort())
        assertEquals (["getAddedProperty", "getStaticAddedMethod", "getAddedMethod","getClass", "\$getLookup", "getDeclaredMethodStaticString", "getDeclaredStaticString", "getMetaClass"].sort(), DynamicExtendableClass.metaClass.methods.findAll {it.name.contains("get")}.collect{it.name}.sort() )

    }

    @Test
    void testStaticMethodsFoundByReflection () {
        assert ["getDeclaredStaticString", "getDeclaredMethodStaticString", "setDeclaredStaticString"].sort() == DynamicExtendableClass.getDeclaredMethods().findAll{ Modifier.isStatic(it.modifiers) && !it.name.contains("\$")}.collect{it.name}.sort()
    }

    @Test
    void testStaticPropertiesFoundByReflection () {

        assert ['declaredMethodStaticString', 'declaredStaticString'].sort() == DynamicExtendableClass.metaClass.getProperties().findAll{ Modifier.isStatic(it.modifiers) && !it.name.contains("\$")}.collect{it.name}.sort()
    }

    @Test
    void testStaticMetaMethodsFoundByReflection () {

        assert ['sleep', 'sleep'].sort() == DynamicExtendableClass.metaClass.getMetaMethods().findAll{ Modifier.isStatic(it.modifiers) && !it.name.contains("\$")}.collect{it.name}.sort()
    }

    @Test
    void testMethodsFoundByReflection () {
        assert ['getDeclaredMethodStaticString', 'getDeclaredStaticString', 'getMetaClass', 'setDeclaredStaticString', 'setMetaClass'].sort() == DynamicExtendableClass.getDeclaredMethods().findAll{ Modifier.isPublic(it.modifiers) && !it.name.contains("\$")}.collect{it.name}.sort()
    }

    @Test
    void testPropertiesFoundByReflection () {

        assert ['class', 'declaredMethodStaticString', 'declaredStaticString'].sort() == DynamicExtendableClass.metaClass.getProperties().findAll{ Modifier.isPublic(it.modifiers) && !it.name.contains("\$")}.collect{it.name}.sort()
    }

    @Test
    void testClassUtilGetStaticValue () {

        def val = ClassUtils.getStaticFieldValue(DynamicExtendableClass, "declaredStaticString" )
        val
     }

    @Test
    void testMetaClassStatic () {

        /*    println DynamicExtendableClass.metaClass

            MetaClassRegistry registry = GroovySystem.getMetaClassRegistry()
            MetaClass origMC = registry.getMetaClass(DynamicExtendableClass)
            assert origMC.getClass() == MetaClassImpl  //default implementation

            def  constructors = MetaClassImpl.getConstructors()

            ExpandoMetaClass emc = new ExpandoMetaClass (DynamicExtendableClass, true, true)
            emc.static.getStaticAddedMethod = {-> "static hello from my emc"}

            emc.constructor = { new DynamicExtendableClass() }
          emc.initialize()

            registry.removeMetaClass(DynamicExtendableClass)
            registry.setMetaClass(DynamicExtendableClass, emc)

            assert DynamicExtendableClass.metaClass.getClass() == ExpandoMetaClass

            assert DynamicExtendableClass.staticAddedMethod == "static hello from my emc"

             registry.removeMetaClass(DynamicExtendableClass)
            registry.setMetaClass(DynamicExtendableClass, origMC)

            assert DynamicExtendableClass.metaClass.getClass() == HandleMetaClass
    */

    }

}
