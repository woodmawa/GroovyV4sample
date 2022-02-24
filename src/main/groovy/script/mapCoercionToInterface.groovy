package script

def map
map = [
        i: 10,
        hasNext: { map.i > 0 },
        next: { map.i-- },
]
def iter = map as Iterator

iter.each {println it}