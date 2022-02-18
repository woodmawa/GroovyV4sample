package script

Reference ref = new Reference()

assert ref.get() == null

ref.set({"closure return"})

Closure clos = ref.get()

assert clos
assert clos() == "closure return"

//clever,  recognises contained object and delegates the invokeMethod to that !
ref.invokeMethod('call', []) == "closure return"

ref.set ("lower")

assert ref.invokeMethod ("toUpperCase", []) == "LOWER"

println ref.invokeMethod ("toUpperCase", [])