package script.databaseApproach

DomainClass.metaClass.thing = {println "hello william"}

Class<DomainClass> EnhClass = GormClass.of (DomainClass)
assert EnhClass.metaClass instanceof ExpandoMetaClass

def inst = new DomainClass ()
inst.thing()

inst2 = EnhClass.constructors[0].newInstance()
inst2.thing()

assert inst2.metaClass == inst.metaClass

def insMc = inst.metaClass
//assert inst.metaClass.respondsTo('thing')

DomainClass customer = EnhClass.constructors[0].newInstance()//enhClass::new()
//assert customer.metaClass instanceof ExpandoMetaClass

List methods = customer.metaClass.methods.collect{it.name}

def res = customer.save()

res

