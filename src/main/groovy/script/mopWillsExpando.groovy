package script

import MOP.WillsExpando

import java.util.concurrent.ConcurrentHashMap

Map bucket = [:]
Map staticBucket = [:]
class AA {}
AA.getMetaClass().propertyMissing << {String name, value  -> println "setting prop $name with value $value"
    bucket.put(name, value)

}
AA.getMetaClass().propertyMissing << {String name  -> println "getting unknown prop $name "
    bucket.get(name)
}
AA.getMetaClass().$static_propertyMissing << {String name, value  -> println "setting static prop $name with value $value"
    staticBucket.put(name, value)
}
AA.getMetaClass().$static_propertyMissing << {String name -> println "getting static prop $name "
    staticBucket.get(name)
}

AA.statNewProp = "new stat prop"  //trigger adding new static property
def statRes = AA.statNewProp

def aa = new AA()
aa.newProp = "my new prop"
def res = aa.newProp
assert res == "my new prop"

WillsExpando mc = new WillsExpando()

mc.foo = "bar"


mc.addStaticProperty("myStaticProp", "static howdi")

def stat = mc.static
assert stat.getClass() == WillsExpando.StaticContainer


mc.static.myStaticProp2 = "another static prop"

def val = mc.getStaticProperty("myStaticProp")
println "static prop readf was $val "

List l = mc.getStaticProperties()

println "l2props : " + mc.static.properties

println ".static gets : " + stat
println " with content " + l + " and with " + l[0].class

