package lamda

class ExampleBeanClass {
    private String value = "hello from getter"
    private static String staticValue = "static string value"

    ExampleBeanClass() {}  //constructor

    String getValue () {return value}
    void setValue (String val) {value = val}

    boolean test(expression) {expression}  //use groovy truth on expression

    static String getStaticValue () {return staticValue}
    static String setStaticValue (String staticVal) {staticValue = staticVal}
}