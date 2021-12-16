package utils

import lamda.ExampleBeanClass
import org.junit.jupiter.api.Test

import java.util.function.Supplier

class ClassUtilsTest {

    @Test
    void generateSupplierFromClosure () {

        Closure myClos = {"hello"}

        Supplier supplier = ClassUtils.getLambdaFromReflectionMethod(Supplier, myClos, 'call')
        supplier.get() == "hello"

    }

    @Test
    void generateSupplierFromBeanClassInstance () {

        ExampleBeanClass bean = new ExampleBeanClass()

        Supplier supplier = ClassUtils.getLambdaFromReflectionMethod(Supplier, bean, 'getValue')
        supplier.get() == "hello from getter"

    }
}
