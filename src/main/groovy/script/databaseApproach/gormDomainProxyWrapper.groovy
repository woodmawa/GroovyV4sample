package script.databaseApproach

GormDomainProxy proxyClass = new GormDomainProxy(DomainClass)

GormDomainProxy proxyInst = proxyClass.newInstance()

println proxyInst.propx
//with's delegate is the DomainClass, so you can access its props/methods directly in context 
proxyInst.with {
    println "inside with(), propx is $propx "
}//hello is proxied to gorm instance
