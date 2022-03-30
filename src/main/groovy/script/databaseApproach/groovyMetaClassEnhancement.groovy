package script.databaseApproach

import org.codehaus.groovy.runtime.HandleMetaClass

MetaClass originalMc = DomainClass.metaClass
assert originalMc instanceof HandleMetaClass

DomainClass.metaClass.thing = {println "hello william"}
MetaClass adjMc = DomainClass.metaClass
//assert adjMc.metaClass instanceof ExpandoMetaClass

def inst = new DomainClass ()
List lm = inst.metaClass.methods.collect{it.name}
//List lem = (inst.metaClass as ExpandoMetaClass).getExpandoMethods()

print "un-enhanced inst : "
inst.thing()

Class<DomainClass> EnhClass = GormClass.of (DomainClass)
assert EnhClass.metaClass instanceof ExpandoMetaClass
List enhlem = (EnhClass.metaClass as ExpandoMetaClass).getExpandoMethods()

DomainClass inst2 = EnhClass.constructors[0].newInstance()
inst2 = GormClass.of(inst2)

//List enhmm = inst2.metaClass.methods.collect{it.name}
//List enhlem = (inst2.metaClass as ExpandoMetaClass).getExpandoMethods()
//inst2.metaClass.hello = {GormClass::hello}

print "enhanced inst2 : "
//inst2.thing()
inst2.hello()

boolean cando = inst2.respondsTo ('hello')

//assert inst2.metaClass == inst.metaClass

def insMc = inst.metaClass
def ins2Mc = inst2.metaClass

List insMcMethods = insMc.methods.collect{it.name}
List ins2McMethods = ins2Mc.methods.collect{it.name}
//assert inst.metaClass.respondsTo('thing')

DomainClass customer = EnhClass.constructors[0].newInstance()//enhClass::new()
//assert customer.metaClass instanceof ExpandoMetaClass

List enhInstMethods = customer.metaClass.methods.collect{it.name}
List enhInstMetaMethods = customer.metaClass.methods.collect{it.name}

def res = customer.save()

res

