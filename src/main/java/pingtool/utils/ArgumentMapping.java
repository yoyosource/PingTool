package pingtool.utils;

public class ArgumentMapping {

    private String key;
    private String[] aliases = new String[0];

    private boolean flag = false;

    public ArgumentMapping(String key) {
        this.key = key;
    }

    public ArgumentMapping(String key, String... aliases) {
        this.key = key;
        this.aliases = aliases;
    }

    public ArgumentMapping(String key, boolean flag,  String... aliases) {
        this.key = key;
        this.aliases = aliases;
        this.flag = flag;
    }

    public String getKey() {
        return key;
    }

    public boolean isFlag() {
        return flag;
    }

    public boolean isArgument(String key) {
        for (String s : aliases) {
            if (s.equals(key)) {
                return true;
            }
        }
        return key.equals(this.key);
    }
}
