package script


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
