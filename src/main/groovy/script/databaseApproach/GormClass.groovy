package script.databaseApproach

import org.codehaus.groovy.runtime.MethodClosure

import java.util.concurrent.atomic.AtomicLong

class GormClass {
    AtomicLong sequence = new AtomicLong (0)
    long id
    String status = "New"

    //see if these can be added dynamically to a domain class via ExpandoMetaClass
    static Map gormMethods = [
            'save':this::save, 'delete':this::delete, 'where':this::where
    ]

    static def of (def instance) {
        ExpandoMetaClass emc = new ExpandoMetaClass (instance.getClass(), true, true)
        gormMethods.each {methodName, closure -> emc.registerInstanceMethod(methodName, closure.rehydrate(instance, instance, instance))}
        emc.initialize()
        instance.setMetaClass (emc)
    }

    static Class of (Class <?> clazz) {
        ExpandoMetaClass emc = new ExpandoMetaClass (clazz, true, true)
        gormMethods.each {methodName, closure -> emc.registerInstanceMethod(methodName, closure.rehydrate(clazz, clazz, clazz))}
        emc.initialize()
        clazz.setMetaClass (emc)
        clazz
    }


    def save () {
        id = sequence.incrementAndGet()
        status = "attached"
        Database.db.putIfAbsent(id, this)

    }

    void delete () {
        def obj = Database.db.remove(id)
        status = "soft deleted"
    }

    def where (Closure closure) {
        Closure constraint = closure.clone()
    }
}