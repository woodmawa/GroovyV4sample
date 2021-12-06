package extensible

import MOP.WillsExpando
import org.codehaus.groovy.runtime.MethodClosure
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test



class WillsExpandoTest {

    def shouldFail (Closure codeToTest, Class e) {
        try {
            codeToTest()
        } catch (Exception ex) {
            assert ex.getClass() == e
        }
    }


    WillsTestSubExpando wtse

    @BeforeEach
    void init() {
        wtse = new WillsTestSubExpando()
    }

    @Test
    void testStandardPropertyExists () {
        assert wtse.getProperty("stdProp") == "defaultClassProp"
    }

    @Test
    void testStandardMethodExists() {
        MethodClosure m = wtse.getMethod ("testMethod")
        assert m
        assert m("will") == "will" + " : hello from test method"
    }

    @Test
    void testNormalGetClass() {
        assert wtse.getClass() == WillsTestSubExpando
    }

    @Test
    void addDynamicProperty () {
        wtse.myProp = "my dynamic prop"

        assert wtse.myProp == "my dynamic prop"
    }

    @Test
    void getUnspecifiedProperty () {

        shouldFail ({ wtse.anotherProp == null}, MissingPropertyException)
    }

    @Test
    void addDynamicPropertyByMethod () {
        wtse.addProperty("myProp", "my dynamic prop")

        assert wtse.getProperty("myProp") == "my dynamic prop"
    }

    @Test
    void addDynamicPropertiesAndCheckProperties () {
        wtse.myProp = "my dynamic prop"
        wtse.myProp2 = "my 2nd dynamic prop"

        Map props = wtse.properties

        assert props.size() == 5
        assert props["myProp"] == "my dynamic prop"
    }

    @Test
    void queryProperties () {
        //get default properties
        Map props = wtse.properties

        assert props.find{it.key.contains("stdProp")}.key == 'stdProp'  //standard class property
        assert props.find {it.key.contains("class")}.key == "class"
        assert props.find{it.key.contains("static")}.key == 'static'
        assert props.size() == 3

        wtse.dynProp = "added property"
        props = wtse.properties
        assert props.size() == 4
        assert props.find{it.key.contains("dynProp")}.key == 'dynProp'


    }

    @Test
    void queryStaticProperties () {

       //get default static properties
        Map props = wtse.staticProperties

        assert props.find{it.key.contains("statProp")}.key == 'statProp'  //standard static class property


        int csize = props.size()
        assert csize == 1

        wtse.addStaticProperty ("dynStatProp",  "added static property")
        props = wtse.getStaticProperties (WillsTestSubExpando)
        assert props.size() == csize + 1
        assert props.find{it.key.contains("dynStatProp")}.key == 'dynStatProp'

        //remove so as not to muddy the static properties for other tests
        wtse.removeStaticProperty("dynStatProp")

    }

    @Test
    void queryStaticMethods () {
        Map smeths = wtse.getStaticMethods(WillsTestSubExpando)

        int msize = smeths.size()
        assert msize == 2  //two static sleep methods

        wtse.addStaticMethod ("myStaticMethod", {"my static method"})
        assert wtse.staticMethods.size() == msize + 1
        Closure method = wtse.getStaticMethod ("myStaticMethod")
        assert method() == "my static method"
        assert method.delegate == this
        assert wtse.static.methods.size() == 1
        assert wtse.static.properties.size() == 0 //there is an existing test static prop in the class

        //clean up statics for next test
        wtse.removeStaticMethod ("myStaticMethod") // remove for other tests
        assert wtse.getStaticMethod ("myStaticMethod") == null
    }

    @Test
    void testStaticContainer () {
        WillsExpando.StaticContainer stat = wtse.static

        Map sprops = stat.properties
        Map smeths = stat.methods

        //initially the static.properties and methods, maps in WillsExpando start out empty
        assert stat.properties.size() == 0
        assert stat.methods.size() == 0

        wtse.addStaticProperty ("statProp", 42)
        wtse.addStaticMethod ("statMethod", {})

        stat.anotherStatProp = 100

        //refresh unmodifiable lists
        sprops = stat.properties
        smeths = stat.methods

        assert stat.anotherStatProp == 100 //stat.properties.anotherStatProp == 100

        assert stat.properties.size() == 2
        assert stat.methods.size() == 1

        //clean up statics for other tests
        wtse.removeStaticProperty("statProp")
        wtse.removeStaticMethod("statMethod")
        wtse.removeStaticProperty ("anotherStatProp")
        assert stat.properties.size() == 0
        assert stat.methods.size() == 0

    }

    @Test
    void testAddingStaticToClassDirectly () {
        WillsExpando.statProp = "new direct adding stat prop"

        def sprop = WillsExpando.getStaticProperty("statProp")

        def sprop2 =  WillsExpando.statProp  //does this invoke $static_propertyMissing?

        assert sprop2 == "new direct adding stat prop"

        //tidy up for other tests
        WillsExpando.removeStaticProperty("statProp")
    }
}
