package script

import MOP.WillsExpando

import java.util.concurrent.ConcurrentHashMap

WillsExpando mc = new WillsExpando()

mc.foo = "bar"

mc.func = {-> println "hello from dynamic method "}

def f = mc.func ()

def m = mc.getMethod("testMethod")
println m()

mc.addStaticProperty("myStaticProp", "static howdi")

def stat = mc.static
assert stat.getClass() == WillsExpando.StaticContainer


mc.static.myStaticProp2 = "another static prop"

def val = mc.getStaticProperty("myStaticProp")
println "static prop readf was $val "

List l = mc.getStaticProperties()

mc.addProperty("dynamic prop", "flex value")
def stdProp = mc.getProperty("stdProp")
List props = mc.getProperties()

MetaBeanProperty mbp = mc.getMetaProperty("dynamic prop")
def dynVal = mbp.getter.invoke(mbp, [] as ArrayList)
mbp.setter.invoke (mbp, "changed flex Value")
dynVal = mbp.getter.invoke(mbp, [] as ArrayList)

println "l2props : " + mc.static.properties

println ".static gets : " + stat
println " with content " + l[0] + " and with " + l[0].class

