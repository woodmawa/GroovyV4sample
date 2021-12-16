package lamda

class ExampleBeanClass {
    private String value = "hello from getter"
    private static String staticValue = "static string value"

    ExampleBeanClass() {}  //constructor

    String getValue () {return value}
    void setValue (String val) {value = val}

    static String getStaticValue () {return staticValue}
    static String setStaticValue (String staticVal) {staticValue = staticVal}
}