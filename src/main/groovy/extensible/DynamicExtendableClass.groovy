package extensible

class DynamicExtendableClass {

    static String declaredStaticString = "declared static string"

    static String getDeclaredMethodStaticString () {
        "static method returning string"
    }

        static {
            //ExpandoMetaClass emc = new ExpandoMetaClass (DynamicExtendableClass, false, true)
            //emc.initialize()

            //DynamicExtendableClass.metaClass.setMetaClass( emc)

            //DynamicExtendableClass.metaClass.setMetaClass (groovy.lang.ExpandoMetaClass)  //force upfront to Expand metaClass
            println "static initialiser - adding dynamic properties and methods to metaClass"
            DynamicExtendableClass.metaClass.addedProperty = "added property to class metaClass"
            DynamicExtendableClass.metaClass.getAddedMethod = { -> "added closure as method to class metaClass" }
            DynamicExtendableClass.metaClass.static.getAddedStaticMethod = { -> "added closure as static method to class metaClass"}

        }
}
