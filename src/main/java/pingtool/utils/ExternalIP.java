package pingtool.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ExternalIP {

    public static String get() {
        String[] urls = new String[]{"http://checkip.amazonaws.com", "https://myexternalip.com/raw", "https://ipecho.net/plain", "http://bot.whatismyipaddress.com"};

        List<String> ips = new ArrayList<>();
        for (String url : urls) {
            String ip = get(url);
            if (!ips.contains(ip)) {
                ips.add(ip);
            }
        }

        if (ips.isEmpty()) {
            return "";
        }
        return ips.get(0);
    }

    private static String get(String address) {
        try {
            URL url = new URL(address);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            return bufferedReader.readLine();
        } catch (IOException e) {
            return "";
        }
    }

}
