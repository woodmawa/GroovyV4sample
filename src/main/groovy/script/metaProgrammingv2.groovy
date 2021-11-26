package script


import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

import org.codehaus.groovy.reflection.ReflectionUtils


class SomeClass {
    static String classDeclaredStatName = "in class definition static name"

    String name = "instance level name"
}

//add a new property to metaclass - works fine
SomeClass.metaClass.dynProp = "dynamic property added"
SomeClass.metaClass.dynMethod = {"dynamic method added as closure"}

SomeClass.metaClass.static.dynStaticProp = "dynamic static property added"
SomeClass.metaClass.static.dynStaticMethod = {"dynamic static method added"}

assert SomeClass.classDeclaredStatName == "in class definition static name"
assert SomeClass.dynStaticMethod() == "dynamic static method added"

//this forces conversion of metaClassImpl to expandoMetaClass
SomeClass myc = new SomeClass()

assert myc.name == "instance level name"
assert myc.classDeclaredStatName == "in class definition static name"
assert myc.dynProp == "dynamic property added"
assert myc.dynMethod() == "dynamic method added as closure"

assert myc.dynStaticMethod() == "dynamic static method added"

def res
res = myc.metaClass.properties  //returns list of metaBeanProperty

res = myc.metaClass.getMetaMethods() //works and returns list of metaMethods

//This is the only method for static's in MOP - this works but you have to know the name of the method in advance
res = myc.metaClass.getStaticMetaMethod("dynStaticMethod", [] as ArrayList)
assert res

boolean hasProp = myc.metaClass.hasProperty (myc, 'dynProp')
assert hasProp


def getStaticProperty (def obj, String pname) {

    Class clazz = obj.getClass()
    MetaClass mc = clazz.metaClass
    ExpandoMetaClass.ExpandoMetaProperty stat = mc.static

    String camelCaseName = pname[0].toUpperCase() + pname.substring (1)

    List props = clazz.getDeclaredFields().findAll {it.name == pname && Modifier.isStatic(it.modifiers) }

    List props2 = clazz.getDeclaredMethods()  //.findAll {it.name == "get$camelCaseName"}

    def props3 = mc.getProperties()
   props
}


def ans = getStaticProperty(myc, 'classDeclaredStatName')
ans


//these fucntions are missing from MOP and would enable you to query for static props/methods
res = myc.metaClass.getStaticMetaMethods()  //this method doesnt exist in MOP api
res = myc.metaClass.getStaticProperties()  //this method doesnt exist in MOP api either

