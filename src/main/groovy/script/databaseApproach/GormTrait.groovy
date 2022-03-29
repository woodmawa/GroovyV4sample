package script.databaseApproach

import groovy.util.logging.Slf4j

import java.util.concurrent.atomic.AtomicLong
import groovy.lang.DelegatesTo

@Slf4j
trait GormTrait {
    static AtomicLong sequence = new AtomicLong (0)
    long id
    String status = "New"
    String name


    def self () {
        this
    }

    //trait name is really class_name__name and this overrides the super class actual instance name if it exists
    //so we check the delegate first and if it has name we return that one - else we use the
    //traits name instead
    def getName() {
        if ($delegate.hasProperty ('name')) {
            println ">>get delegate name, [" + $delegate.name + "]"
            $delegate.name
        }
        else {
            println ">>delegate instance doesnt have name, use the traits  [$name]"

            name //else use traits name
        }
    }

    void setName(String nm) {
        if ($delegate.hasProperty ('name')) {
            println ">>set delegate name"
            $delegate.name = nm
            name = nm

        }
        else {
            println ">>delagate has no name - use the traits instead "
            name = nm //else use traits name
        }
    }

    def save () {
        id = sequence.incrementAndGet()
        if (status == "new")
            status = "attached"
        println ">>saving $id " + $delegate.name
        Database.db.putIfAbsent(id, this)

    }

    void delete () {
        Database.db.remove(id)
        status = "soft deleted"
    }

    List  where (@DelegatesTo (DomainClass) Closure closure) {
        Closure constraint = closure.clone()

        def inTraitThisIs = this  //the proxy
        def inTraitDelegateIs = this.$delegate //is the actual original class instance

        def values = Database.db.values().toList()
        List matched = []
        values.each{record->

            //for each record in the DB avaluate to get the closure where the delegate is this record proxy, and owner is original instance and evaluate it
            //rehydrate (delegate, owner, this)
            def constraintClos = constraint.rehydrate(record, record.$delegate, record.$delegate)

            def closureRet = constraintClos()
            if (closureRet)
                matched << record
        }
        matched ?: []
    }

    String toString () {
        //in trait we invoke the base classes toString
        //super.toString()
        String nm = getName()
        "DomainClass (id: $id, name: [$nm])"

    }
}
