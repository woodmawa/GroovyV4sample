package extensible

import org.codehaus.groovy.runtime.HandleMetaClass
import org.codehaus.groovy.runtime.MethodClosure
import org.junit.jupiter.api.Test

import java.lang.reflect.Constructor

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows


class DynamicExtendableClassTest {


    @Test
    void testDeclaredStaticStuff () {

        assertEquals ("static method returning string", DynamicExtendableClass.getDeclaredMethodStaticString())
        assertEquals ("declared static string",  DynamicExtendableClass.declaredStaticString)
    }

    @Test
    void testExtendedMetaClassStuff () {

        DynamicExtendableClass testInstance = new DynamicExtendableClass()

        assertEquals ("added property to class metaClass", testInstance.addedProperty)
        assertEquals ("added closure as static method", testInstance.getStaticAddedMethod())  //calls getStaticAddedMethod - groovy trick
        assertEquals ("added closure as method", testInstance.addedMethod)  //works.  calls getAddedMethod - groovy trick for getXxx as property
        assertEquals ("added closure as static method", DynamicExtendableClass.staticAddedMethod )  //works class static class Closure

    }

    @Test
    void testMetaClassPropertiesAndMethods () {
        //assertEquals (["declaredStaticString", "class", "declaredMethodStaticString", "addedProperty", "addedMethod"], DynamicExtendableClass.metaClass.properties.collect {it.name})
        //assertEquals (["getAddedProperty", "getStaticAddedMethod", "getAddedMethod","getClass", "\$getLookup", "getDeclaredMethodStaticString", "getDeclaredStaticString", "getMetaClass"], DynamicExtendableClass.metaClass.methods.findAll {it.name.contains("get")}.collect{it.name} )

    }

    @Test
    void testMetaClassStatic () {

        println DynamicExtendableClass.metaClass

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


    }
}
