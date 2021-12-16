package LambdaMetaclassTests

import extensible.WillsTestSubExpando
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import script.ExampleBeanClass

import java.lang.invoke.CallSite
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.util.function.Supplier

class LambdaBeanClassAccessTest {

    ExampleBeanClass bean
    MethodHandles.Lookup callerCtx
    MethodHandle implementationDelegate

    @BeforeEach
    void init() {
        bean = new ExampleBeanClass()
        callerCtx = MethodHandles.lookup()
    }

    @Test
    void dummyTest () {
        assert true
        assert bean
        assert callerCtx
    }


}
