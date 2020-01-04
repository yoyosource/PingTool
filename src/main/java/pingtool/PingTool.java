package pingtool;

import pingtool.utils.ArgumentMapper;
import pingtool.utils.ArgumentMapping;
import pingtool.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class PingTool {

    private static ArgumentMapping[] argumentMappings = new ArgumentMapping[]{
            new ArgumentMapping("-ip", "-IP", "-I"),
            new ArgumentMapping("-file", "-f"),
            new ArgumentMapping("-interval", "-i"),

            new ArgumentMapping("-help", true, "-h"),

            new ArgumentMapping("-default", true, "-d"),
            new ArgumentMapping("-ping", true, "-p"),
            new ArgumentMapping("-averagePings", true, "-a"),
            new ArgumentMapping("-AveragePings", true, "-A"),
            new ArgumentMapping("-error", true, "-e"),
            new ArgumentMapping("-Error", true, "-E")};
    private static int padding = 24;

    public static void main(String[] args) {
        ArgumentMapper argumentMapper = new ArgumentMapper(argumentMappings);
        argumentMapper.map(args);

        if (argumentMapper.hasError()) {
            while (argumentMapper.hasError()) {
                System.out.println(argumentMapper.getError());
            }
            return;
        }

        if (!argumentMapper.hasArgument() || argumentMapper.hasArgument("-help")) {
            System.out.println("Help Ping Tool");
            showHelp();
            return;
        }

        if (!argumentMapper.hasArgument("-ip")) {
            System.out.println("Please specify an IP address with '-ip <IP>'");
            showHelp();
            return;
        }
        String ip = argumentMapper.getArgument("-ip").getValue();
        if (!ip.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")) {
            System.out.println("Invalid IP address");
            System.out.println("Format: n[nn].n[nn].n[nn].n[nn]");
            System.out.println("Regex: \"[0-9]{1,3}(\\.[0-9]{1,3}){3}\"");
            return;
        }

        String file = "";
        if (argumentMapper.hasArgument("-file")) {
            file = argumentMapper.getArgument("-file").getValue();
        }

        String interval = "100";
        if (argumentMapper.hasArgument("-interval")) {
            interval = argumentMapper.getArgument("-interval").getValue();
            if (!interval.matches("\\d+")) {
                System.out.println("Invalid interval");
                System.out.println("Format: n");
                System.out.println("Regex: \"\\\\d+\"");
                return;
            }
        }

        boolean graph_Ping = true;
        boolean graph_averagePings = true;
        boolean graph_AveragePings = true;
        boolean graph_Error = false;

        if (argumentMapper.hasArgument("-ping") || argumentMapper.hasArgument("-averagePings") || argumentMapper.hasArgument("-AveragePings") || argumentMapper.hasArgument("-error")) {
            graph_Ping = false;
            graph_averagePings = false;
            graph_AveragePings = false;
            graph_Error = false;
        }

        if (argumentMapper.hasArgument("-default")) {
            graph_Ping = true;
            graph_averagePings = true;
            graph_AveragePings = true;
        }
        if (argumentMapper.hasArgument("-ping")) {
            graph_Ping = true;
        }
        if (argumentMapper.hasArgument("-averagePings")) {
            graph_averagePings = true;
        }
        if (argumentMapper.hasArgument("-AveragePings")) {
            graph_AveragePings = true;
        }
        if (argumentMapper.hasArgument("-error")) {
            graph_Error = true;
        }

        Ping ping = new Ping(ip, file, interval, graph_Ping, graph_averagePings, graph_AveragePings, graph_Error);
        Thread pingThread = new Thread(ping);
        pingThread.setName("PingThread");
        pingThread.start();
    }

    public static void showHelp() {
        System.out.println("SETTINGS");
        System.out.println(StringUtils.padding("-h[elp]", padding) + "to see this message.");
        System.out.println(StringUtils.padding("-ip <IP>", padding) + "to start the graph with specified IP address.");
        System.out.println(StringUtils.padding("-I[P] <IP>", padding) + "refers to '-ip <IP>'.");
        System.out.println(StringUtils.padding("-f[ile] <FILE>", padding) + "to start the graph with specified IP address and Output Dump File.");
        System.out.println(StringUtils.padding("", padding) + "<FILE> will automatically be in your user.home directory and will get the file suffix .pings");
        System.out.println(StringUtils.padding("-i[nterval] <TIME>", padding) + "to specify the interval time in milliseconds to ping the specified IP address");
        System.out.println();
        System.out.println("FLAGS");
        System.out.println(StringUtils.padding("-p[ing]", padding) + "to show the ping graph.");
        System.out.println(StringUtils.padding("-a[veragePings]", padding) + "to show the average over the last 20 pings.");
        System.out.println(StringUtils.padding("-A[veragePings]", padding) + "to show the average over the last 1000 pings.");
        System.out.println(StringUtils.padding("-d[efault]", padding) + StringUtils.padding("refers to:", 13) + StringUtils.padding("-p -a -A", 15) + "remove '-d' if '-e' is not specified.");
        System.out.println(StringUtils.padding("", padding + 13 + 15) + "remove '-d' if '-E' is specified as '-E' refers to '-d -e'.");
        System.out.println(StringUtils.padding("", padding + 13 + 15) + "remove '-d' if '-p', '-a' or '-A' is specified as '-d' overwrites those flags.");
        System.out.println(StringUtils.padding("-e[rror]", padding) + "to show the error graph");
        System.out.println(StringUtils.padding("-E[rror]", padding) + StringUtils.padding("refers to:", 13) + StringUtils.padding("-p -a -A -e", 15) + "short form for '-d -e'.");
        System.out.println();
        System.out.println(StringUtils.padding("", padding) + "FLAGS will specify the different graphs shown. If any flag is specified it overrides the -d flag with your specifications.");
    }

}
