package extensible

import MOP.WillsExpando

//subclass of wills expando to test subclasses of WillsExpando

class WillsTestSubExpando extends WillsExpando {
        String stdProp = "defaultClassProp"
        static String statProp = "defaultClassStaticProp"

        def testMethod (String test) {
            test + " : hello from test method"
        }
}
