package pingtool.utils;

public class Argument {

    private String key;
    private String value;

    public Argument(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Argument{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
