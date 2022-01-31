package closure;

import groovy.lang.*;

/*
 * define a Java closure that can be called by groovy
 *
 */
public class SampleJavaClosure {
    private  Reference count = new Reference(0);

    public Reference getCountRef() {
        return count;

    }

    public void setCountRef(Object scopedObject) {
        count = new Reference(scopedObject) ;

    }

    //get reference object from closure birthday context, and increment it by one
    Closure sampleClosure = new Closure(this) {

        public Object doCall() {
            count.set((Integer)count.get() + 1);
            return count.get();
        }
    };

    public Closure getSampleClosure () {
        return sampleClosure;
    }
}
