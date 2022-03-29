package script.databaseApproach

GormDomainProxy proxyClass = new GormDomainProxy(DomainClass)

GormDomainProxy proxyInst = proxyClass.newInstance()

println proxyInst.propx
proxyInst.hello()   //hello is proxied to gorm instance
