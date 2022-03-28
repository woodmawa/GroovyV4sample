package script.databaseApproach

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

DomainClass customer = new DomainClass (name:"HSBC")
DomainClass customer2 = new DomainClass (name:"natwest")

assert customer.name == "HSBC"

def enhanced = customer.withTraits (GormTrait)


println "self shows " + enhanced.self()
enhanced.propx = 10

assert enhanced.name == "HSBC"
enhanced.save()
def enhanced2 = customer2.withTraits (GormTrait)
enhanced2.save()

println "database (size: ${Database.db.size()} has entries : " + Database.db

def GormEnrichedDomainClass = DomainClass as GormTrait
List res = GormEnrichedDomainClass.where {it ->
    name == "natwest" && propx == 10}  //validates to DomainClass
println "find results is $res"




