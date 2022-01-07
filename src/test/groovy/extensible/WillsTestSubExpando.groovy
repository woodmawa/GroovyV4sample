package extensible

import mop.WillsExpando
import org.junit.jupiter.api.Test

//subclass of wills expando to test subclasses of WillsExpando

class WillsTestSubExpando extends WillsExpando {
    String stdProp = "defaultClassProp"
    static String statProp = "defaultClassStaticProp"

    def testMethod (String test) {
        test + " : hello from test method"
    }

    static def staticTestMethod (String test) {
        test + " : hello from static test method"
    }
}
