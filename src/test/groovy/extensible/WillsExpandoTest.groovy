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
        assert wtse.hasProperty('statProp')
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
        //only gives map of name, and value
        Map smeths = wtse.getStaticMethods(WillsTestSubExpando)

        int msize = smeths.size()
        assert msize == 1  //two static sleep methods

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

    @Test
    void invokeMetaMethodTest () {
        MetaMethod mm = wtse.getMetaMethod('getAt', String)

        assert mm
        assert mm.getName() == 'getAt'
        assert mm.isStatic() == false
        assert mm.returnType == Object
        assert mm.signature == "getAt(Ljava/lang/String;)Ljava/lang/Object;"

        wtse.dynamicMethod = {'hello from dynamicMethod'}
        mm = wtse.getMetaMethod('dynamicMethod')
        assert mm.getName() == 'dynamicMethod'
        assert mm.isStatic() == false
        assert mm.returnType == Object
        assert mm.invoke(wtse) == 'hello from dynamicMethod'

    }

    @Test
    void invokeStaticMetaMethodTest () {
        wtse.addStaticMethod ('staticDynamicMethod', {'hello from staticDynamicMethod'})
        MetaMethod smm = wtse.getStaticMetaMethod('staticDynamicMethod')
        assert smm.getName() == 'staticDynamicMethod'
        assert smm.isStatic() == true
        assert smm.returnType == Object
        assert smm.invoke(wtse) == 'hello from staticDynamicMethod'

        wtse.removeStaticMethod ('staticDynamicMethod')
    }

    @Test
    void examineStaticMetaMethods () {

        wtse.addStaticMethod ('staticDynamicMethod', {'hello from staticDynamicMethod'})

        List<MetaMethod> mms = wtse.getStaticMetaMethods()


        List mnames = mms.collect{it.name}

        MetaMethod dmm = mms.find{it.name == 'staticDynamicMethod'}

        assert mms
        assert dmm.isStatic()
        dmm.invoke(wtse) == 'staticDynamicMethod'

        wtse.removeStaticMethod ('staticDynamicMethod')

    }

    @Test
    void examineMetaMethods () {

        wtse.addMethod ('dynamicMethod', {'hello from dynamicMethod'})

        List<MetaMethod> mms = wtse.getMetaMethods()


        List mnames = mms.collect{it.name}

        MetaMethod dmm = mms.find{it.name == 'dynamicMethod'}

        assert mms
        assert dmm.isStatic() == false
        dmm.invoke(wtse) == 'dynamicMethod'

        wtse.removeMethod('dynamicMethod')
    }

    @Test
    void examineMetaProperties () {

        wtse.addProperty ('dynamicProperty', 'dynamic property value')

        List<MetaMethod> mps = wtse.getMetaProperties()


        List mnames = mps.collect{it.name}

        MetaProperty dmp = mps.find{it.name == 'dynamicProperty'}

        assert mps
        dmp.getProperty(wtse) == 'dynamic property value'

        wtse.removeProperty('dynamicProperty')
    }

    @Test
    void examineStaticMetaProperties () {

        wtse.addStaticProperty ('staticDynamicProperty', 'static dynamic property value')

        List<MetaMethod> smps = wtse.getStaticMetaProperties()


        List mnames = smps.collect{it.name}

        MetaProperty dmp = smps.find{it.name == 'staticDynamicProperty'}

        assert smps
        dmp.getProperty(wtse) == 'static dynamic property value'

        wtse.removeStaticProperty('staticDynamicProperty')
    }
}
