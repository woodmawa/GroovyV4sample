package LambdaMetaclassTests

import extensible.WillsTestSubExpando
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import script.ExampleBeanClass

class LambdaBeanClassAccessTests {

    ExampleBeanClass bean
    @BeforeEach
    void init() {
        bean = new ExampleBeanClass()
    }

    @Test
    void accessViaGetter () {


    }
}
