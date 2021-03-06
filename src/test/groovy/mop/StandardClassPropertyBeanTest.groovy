package mop

import spock.lang.Specification


class StandardClassPropertyBeanTest extends Specification {

    def "create standard metaMethod from standard class " () {

        given:

        TestClass tc = new TestClass()

        StandardClassPropertyBean mbp = new StandardClassPropertyBean(tc, 'intVal')

        MetaMethod mm = mbp.getGetter()

        expect:
        mm.invoke(tc) == 10 

    }

}

class TestClass {
    int intVal = 10
}
