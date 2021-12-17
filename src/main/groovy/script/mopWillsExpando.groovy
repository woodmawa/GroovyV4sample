package script

import MOP.WillsExpando

import java.util.concurrent.ConcurrentHashMap

WillsExpando willsExpando = new WillsExpando()

class ExtendedWillsExpando extends WillsExpando {
    def testMethod () {
        "testMethod"
    }
    def stdProp = "stdProp"
}
ExtendedWillsExpando extendedWillsExpando = new ExtendedWillsExpando()

willsExpando.foo = "bar"

willsExpando.func = {-> println "hello from dynamic method "}

def f = willsExpando.func ()

def m = extendedWillsExpando.getMethod("testMethod")

println m.call()

willsExpando.addStaticProperty("myStaticProp", "static howdi")

def stat = willsExpando.static
assert stat.getClass() == WillsExpando.StaticContainer


willsExpando.static.myStaticProp2 = "another static prop"

def val = willsExpando.getStaticProperty("myStaticProp")
println "static prop readf was $val "

Map staticPropMap = willsExpando.getStaticProperties()

extendedWillsExpando.addProperty("dynamic prop", "flex value")
def stdProp = extendedWillsExpando.getProperty("stdProp")
Map propMap = willsExpando.getProperties()

/*MetaBeanProperty mbp = willsExpando.getProperty("dynamic prop")
def dynVal = mbp.getter.invoke(mbp, [] as ArrayList)
mbp.setter.invoke (mbp, "changed flex Value")
dynVal = mbp.getter.invoke(mbp, [] as ArrayList)*/

println "l2props : " + willsExpando.static.properties

println ".static gets : " + stat
//println " with content " + l[0] + " and with " + l[0].class

