package extensible

import MOP.WillsExpando
import org.codehaus.groovy.runtime.MethodClosure
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class WillsExpandoTest {

    def shouldFail (Closure codeToTest, Class e) {
        try {
            codeToTest()
        } catch (Exception ex) {
            assert ex.getClass() == e
        }
    }

    WillsExpando we

    @BeforeEach
    void init() {
        we = new WillsExpando()
    }

    @Test
    void testStandardPropertyExists () {
        assert we.getProperty("stdProp") == "defaultClassProp"
    }

    @Test
    void testStandardMethodExists() {
        MethodClosure m = we.getMethod ("testMethod")
        assert m
        assert m("will") == "will" + " : hello from test method"
    }

    @Test
    void testNormalGetClass() {
        assert we.getClass() == WillsExpando
    }

    @Test
    void addDynamicProperty () {
        we.myProp = "my dynamic prop"

        assert we.myProp == "my dynamic prop"
    }

    @Test
    void getUnspecifiedProperty () {

        shouldFail ({ we.anotherProp == null}, MissingPropertyException)
    }

    @Test
    void addDynamicPropertyByMethod () {
        we.addProperty("myProp", "my dynamic prop")

        assert we.getProperty("myProp") == "my dynamic prop"
    }

    @Test
    void addDynamicPropertiesAndCheckProperties () {
        we.myProp = "my dynamic prop"
        we.myProp2 = "my 2nd dynamic prop"

        List props = we.properties

        assert props.size() == 5
        assert props[0] instanceof Map.Entry
        //assert props.sort() == [stdProp:defaultClassProp, class:MOP.WillsExpando, static: WillsExpando$StaticContainer@16423501, myProp=my dynamic prop, myProp2=my 2nd dynamic prop]

    }

    @Test
    void queryProperties () {
        //get default properties
        List<Map.Entry> props = we.properties

        assert props.find{it.key.contains("stdProp")}.key == 'stdProp'  //standard class property
        assert props.find {it.key.contains("class")}.key == "class"
        assert props.find{it.key.contains("static")}.key == 'static'
        assert props.size() == 3

        we.dynProp = "added property"
        props = we.properties
        assert props.size() == 4
        assert props.find{it.key.contains("dynProp")}.key == 'dynProp'


    }

    @Test
    void queryStaticProperties () {
        //get default static properties
        List<Map.Entry> props = we.getStaticProperties()

        assert props.find{it.key.contains("statProp")}.key == 'statProp'  //standard static class property


        int csize = props.size()
        assert csize == 1

        we.addStaticProperty ("dynStatProp",  "added static property")
        props = we.staticProperties
        assert props.size() == csize + 1
        assert props.find{it.key.contains("dynStatProp")}.key == 'dynStatProp'


    }

    @Test
    void queryStaticMethods () {
        List<Map.Entry> smeths = we.getStaticMethods()

        int msize = smeths.size()
        assert msize == 2  //two static sleep methods

        we.addStaticMethod ("myStaticMethod", {"my static method"})
        assert we.getStaticMethods().size() == msize + 1
        Closure meth = we.getStaticMethod ("myStaticMethod")
        assert meth() == "my static method"
        assert meth.delegate == this
        assert we.static.methods.size() == 1
        assert we.static.properties.size() == 1 //there is an existing test static prop in the class
        assert we.staticProperties[0].key == "statProp"
    }
}
