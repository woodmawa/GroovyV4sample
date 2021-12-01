package script

import MOP.WillsExpando


WillsExpando mc = new WillsExpando()

mc.foo = "bar"


mc.addStaticProperty("myStaticProp", "static howdi")

mc.static.myStaticProp2 = "another static prop"

def val = mc.getStaticProperty("myProp")
println "static prop readf was $val "

List l = mc.getStaticProperties()

println "l2props : " + mc.static.properties

println ".static gets : " + map
println " with content " + l + " and class " + l[0].class

