package script

import MOP.WillsExpando
import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

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

extendedWillsExpando.addProperty("dynamicProp", "dynamic property")
def stdProp = extendedWillsExpando.getProperty("stdProp")
Map propMap = willsExpando.getProperties()

MetaBeanProperty mbp = extendedWillsExpando.getMetaProperty("stdProp")
def statVal = mbp.getter.invoke(extendedWillsExpando)
mbp.setter.invoke ( extendedWillsExpando, "changed std prop Value")
assert extendedWillsExpando.stdProp == "changed std prop Value"
assert mbp.getProperty(extendedWillsExpando) == "changed std prop Value"

/** -------
Closure getterClos = extendedWillsExpando::getAt
Closure setterClos = extendedWillsExpando::putAt
def ans = getterClos('dynamicProp')

Closure curriedGetterClos = getterClos.curry('dynamicProp')
Closure curriedSetterClos = setterClos.curry( 'dynamicProp')
ans = curriedGetterClos()
curriedSetterClos('new dynamic value')
assert curriedGetterClos() == 'new dynamic value'

Method getterMethod = curriedGetterClos.getClass().getMethod( 'call')
Method setterMethod = curriedSetterClos.getClass().getMethod ('call', Object)

def decClazz = getterMethod.declaringClass

CachedMethod cacheGet = new CachedMethod(getterMethod )
CachedMethod cacheSet = new CachedMethod(setterMethod )

def res = cacheGet.getSignature()
res = cacheGet.invoke(curriedGetterClos)

MetaMethod getterMeth = new ClosureMetaMethod ('getAt', ExtendedWillsExpando, curriedGetterClos, cacheGet )
MetaMethod setterMeth = new ClosureMetaMethod ('setAt', ExtendedWillsExpando, curriedSetterClos, cacheSet )

res = getterMeth.invoke(extendedWillsExpando)

mbp = new MetaBeanProperty('dynamicProp', ExtendedWillsExpando, getterMeth, setterMeth)
res = mbp.getProperty()


/** ------- */

mbp = extendedWillsExpando.getMetaProperty("dynamicProp")
MetaMethod getter = mbp.getter
def dynVal = getter.invoke(extendedWillsExpando)  //sadly getter is from closure - and extendedWillsExpando isnt - fails
mbp.setter.invoke ( extendedWillsExpando, "changed dynamic property")
assert extendedWillsExpando.dynamicProp == "changed dynamic property"
assert mbp.getProperty(extendedWillsExpando) == "changed dynamic property"

println "l2props : " + willsExpando.static.properties

println ".static gets : " + stat
//println " with content " + l[0] + " and with " + l[0].class

