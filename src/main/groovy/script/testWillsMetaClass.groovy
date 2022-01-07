package script


import mop.WillsMetaClass2

import java.lang.reflect.Modifier

class WillsClass {
    String prop = "class property"

    static String sprop = "static class property "
}

WillsClass wo = WillsClass::new()

WillsMetaClass2 wmc = new WillsMetaClass2(WillsClass, true, true)
wmc.dynProp = "wmc dynamic prop"

//wmc.initialize()
wo.setMetaClass(wmc)   //set new metaclass

def newMetaClass = wo.metaClass
assert wo.metaClass.class == WillsMetaClass2

def dynProp = wo.dynProp
//def unknown = wo.unknown

String modType (int mod) {
    if (Modifier.isStatic(mod))
        return "static"
    if (Modifier.isPublic(mod))
        return "public"
    if (Modifier.isProtected(mod))
        return "protected"
    if (Modifier.isPrivate(mod))
        return "private"
}


Collection props = wo.metaClass.getExpandoProperties().collect{"name:${it.name}, mod:${modType(it.modifiers)}"}
Collection meths = wo.metaClass.getMethods().collect{"name:${it.name}, mod:${modType(it.modifiers)}"}

def womc = wo.metaClass
def stat = womc.'static'


stat.myStaticProp = 10
def result = stat.myStaticProp
assert wo.hasProperty( "dynProp")

def  sprops = wo.metaClass.'static'.getProperties()

def allSprops = wo.metaClass.getStaticProperties()
///------
println wo.dynProp

def mm = wo.metaClass.getMetaMethod('hello')

//println mm.invoke(wmc)

println wo.invokeMethod('hello', null)

println wo.willsMetaClassProperty