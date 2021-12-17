package script

class A {}

assert A.metaClass =~ /MetaClassImpl/

//converts to expandoMetaClass
A.metaClass.dynamicMethod = { -> "add method"}


assert A.metaClass =~ /ExpandoMetaClass/