package script

import extensible.DynamicExtendableClass


DynamicExtendableClass testInstance = new DynamicExtendableClass()

assert DynamicExtendableClass.getDeclaredMethodStaticString() == "static method returning string"

assert DynamicExtendableClass.declaredStaticString == "declared static string"

assert testInstance.addedProperty == "added property to class metaClass"