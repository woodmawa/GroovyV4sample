package script.databaseApproach

import java.util.concurrent.atomic.AtomicLong

class GormClass {
    AtomicLong sequence = new AtomicLong (0)
    long id
    String status = "New"

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