package muhzi.parser;

class Token {
    private String value;
    private String type;

    Token() {
        value = "";
        type = "";
    }

    String getValue() {
        return value;
    }

    String getType() {
        return type;
    }

    void setValue(String value) {
        this.value = value;
    }

    void setType(String type) {
        this.type = type;
    }
}
