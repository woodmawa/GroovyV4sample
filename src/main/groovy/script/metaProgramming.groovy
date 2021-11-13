package script

class MyClass {

    String getStaticName () {
        this.metaClass.getProperty(this, "statName")
    }
}

//metaclass behaves like an exapndo - add variable and give it a value
MyClass.metaClass.will = "hello"
MyClass.metaClass.static.statName = "static hello"

MyClass num1 = new MyClass()

println num1.statName

//println num1.metaClass.static.statName