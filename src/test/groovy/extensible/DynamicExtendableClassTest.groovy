package extensible

import org.junit.jupiter.api.Test
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

        //ExpandoMetaClass.ExpandoMetaProperty mprop = DynamicExtendableClass.metaClass.static.getAt("blah")
        //mprop.setProperty("staticAddedMethod", "wills new value")
        //Map staticProps = DynamicExtendableClass.metaClass.static.getProperties()
        println DynamicExtendableClass.metaClass

        new DynamicExtendableClass() //forces chnage

        println DynamicExtendableClass.metaClass

        /*MetaClassRegistry registry = GroovySystem.getMetaClassRegistry()
        MetaClass origMC = registry.getMetaClass(DynamicExtendableClass)

        ExpandoMetaClass emc = new ExpandoMetaClass (DynamicExtendableClass, false, true)

        emc.static.getStaticAddedMethod {-> "hello from my emc"}
        emc.initialize()

        registry.setMetaClass(DynamicExtendableClass, emc)

        MetaClass newMC = registry.getMetaClass(DynamicExtendableClass)
*/

        //DynamicExtendableClass testInstance = new DynamicExtendableClass()

        //println DynamicExtendableClass.metaClass.getClass()
        println DynamicExtendableClass.staticAddedMethod
        //println  DynamicExtendableClass.addedMethod

    }
}
