package mop

import spock.lang.Specification

class Dummy {
    int intVal = 0
}

class StaticExpandoPropertyTest extends Specification {

    def "create proxied property" () {

        given :
        WillsMetaClass2 mwc = new WillsMetaClass2(Dummy, false, true)

        Dummy dummy = new Dummy ()
        int dummyHash = dummy.hashCode()

        dummy.intVal = 5
        int revisedDummyHash = dummy.hashCode()

        //setup new expando proxy property
        StaticExpandoProperty prop = new StaticExpandoProperty(Dummy, 'myProp', String, "initial value")

        //only at this point when getter or setter is invoked is value point in PROPNAME_TO_MAP backing store
        prop.setProperty(dummy, "new value")


        def res = prop.getProperty (dummy)
        expect:

        dummyHash == revisedDummyHash       //confirm that instance hash hasnt changed when class variable is updated

        res == "new value"

    }
}
