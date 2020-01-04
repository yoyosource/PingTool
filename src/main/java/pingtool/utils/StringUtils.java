package pingtool.utils;

public class StringUtils {

    public static String leading(String s, int length) {
        if (length - s.length() > 0) {
            return " ".repeat(length - s.length()) + s;
        }
        return "" + s;
    }

    public static String padding(String s, int length) {
        if (length - s.length() > 0) {
            return s + " ".repeat(length - s.length());
        }
        return s + "";
    }

}
