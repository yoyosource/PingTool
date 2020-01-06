package pingtool;

import pingtool.utils.ArgumentMapper;
import pingtool.utils.ArgumentMapping;
import pingtool.utils.StringUtils;

public class PingTool {

    private static ArgumentMapping[] argumentMappings = new ArgumentMapping[]{
            new ArgumentMapping("-ip", "-IP", "-I"),
            new ArgumentMapping("-file", "-f"),
            new ArgumentMapping("-interval", "-i"),

            new ArgumentMapping("-latency", "-l"),
            new ArgumentMapping("-time", "-t"),

            new ArgumentMapping("-help", true, "-h", "-?"),
            new ArgumentMapping("-man", true, "-m"),

            new ArgumentMapping("-background", true, "-b"),
            new ArgumentMapping("-default", true, "-d"),
            new ArgumentMapping("-ping", true, "-p"),
            new ArgumentMapping("-averagePings", true, "-a"),
            new ArgumentMapping("-AveragePings", true, "-A"),
            new ArgumentMapping("-error", true, "-e"),
            new ArgumentMapping("-Error", true, "-E")};

    private static int padding = 24;
    private static int indention = 5;
    private static String commandName = "pingtool";
    private static int compactPadding = 7;

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
            showCompactHelp();
            return;
        }
        if (argumentMapper.hasArgument("-man")) {
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
        if (argumentMapper.hasArgument("-background") && !argumentMapper.hasArgument("-file")) {
            System.out.println("Please specify a File when using '-background' or '-b' with '-f <FILE>'");
            return;
        }
        if (argumentMapper.hasArgument("-background") && !argumentMapper.hasArgument("-time")) {
            System.out.println("Please specify a Time when using '-background' or '-b' with '-t <TIME>'");
            return;
        }

        int interval = 100;
        if (argumentMapper.hasArgument("-interval")) {
            String value = argumentMapper.getArgument("-interval").getValue();
            if (!value.matches("\\d+")) {
                System.out.println("Invalid interval");
                System.out.println("Format: n");
                System.out.println("Regex: \"\\\\d+\"");
                return;
            }
            interval = Integer.parseInt(value);
        }
        double latency = 0;
        if (argumentMapper.hasArgument("-latency")) {
            String value = argumentMapper.getArgument("-latency").getValue();
            if (!value.matches("\\d+")) {
                System.out.println("Invalid latency");
                System.out.println("Format: n");
                System.out.println("Regex: \"\\\\d+\"");
                return;
            }
            latency = Double.parseDouble(value);
        }
        long stopAfter = 0;
        if (argumentMapper.hasArgument("-time")) {
            String value = argumentMapper.getArgument("-time").getValue();
            if (!value.matches("\\d+")) {
                System.out.println("Invalid time");
                System.out.println("Format: n");
                System.out.println("Regex: \"\\\\d+\"");
            }
            stopAfter = Long.parseLong(value) * 60 * 1000;
        }

        boolean graph_Ping = true;
        boolean graph_averagePings = true;
        boolean graph_AveragePings = true;
        boolean graph_Error = false;

        if (argumentMapper.hasArgument("-ping") || argumentMapper.hasArgument("-averagePings") || argumentMapper.hasArgument("-AveragePings") || argumentMapper.hasArgument("-error")) {
            graph_Ping = false;
            graph_averagePings = false;
            graph_AveragePings = false;
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

        Ping ping = new Ping(ip, file, interval, argumentMapper.hasArgument("-background"), graph_Ping, graph_averagePings, graph_AveragePings, graph_Error, latency, stopAfter);
        Thread pingThread = new Thread(ping);
        pingThread.setName("PingThread");
        if (argumentMapper.hasArgument("-background")) {
            pingThread.setDaemon(true);
        }
        pingThread.start();
    }

    public static void showCompactHelp() {
        System.out.println(StringUtils.padding("usage: " + commandName, compactPadding + commandName.length()) + "[-AabdEehmp] [-I ip] [-f file]");
        System.out.println(StringUtils.padding("", compactPadding + commandName.length()) + "[-i interval] [-l latency] [-t time]");
    }

    public static void showHelp() {
        System.out.println("NAME");
        System.out.println(" ".repeat(indention) + commandName + " -- send ICMP ECHO_REQUEST packets to network hosts and visualises them");
        System.out.println();
        System.out.println("SYNOPSIS");
        System.out.println(" ".repeat(indention) + commandName + " [-AabdEehmp] [-I ip] [-f file]");
        System.out.println(" ".repeat(compactPadding + commandName.length() - 1) + "[-i interval] [-l latency] [-t time]");
        System.out.println();
        System.out.println("DESCRIPTION");
        System.out.println(" ".repeat(indention) + "This tool displays ping results graphically. The options are as follows:");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-A[veragePings]");
        System.out.println(" ".repeat(indention + 6) + "shows the average value over the last 1000 pings as `:'");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-a[veragePings]");
        System.out.println(" ".repeat(indention + 6) + "shows the average value over the last 20 pings as `|'");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-b[ackground]");
        System.out.println(" ".repeat(indention + 6) + "run this process as a daemon without console output and the argument `-f <FILE>' and `-t <TIME>' are needed.");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-d[efault]");
        System.out.println(" ".repeat(indention + 6) + "the same as `-p -a -A'");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-E[rror]");
        System.out.println(" ".repeat(indention + 6) + "the same as `-p -a -A -e'. Removes `-d' if this flag is used as it is redundant");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-e[rror]");
        System.out.println(" ".repeat(indention + 6) + "shows error graph in output");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-f[ile] filename");
        System.out.println(" ".repeat(indention + 6) + "prints output into specified file in addition to stdout, creates it if necessary. File will start at `user.home' and gets the suffix `.pings'");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-h[elp]");
        System.out.println(" ".repeat(indention + 6) + "prints this message");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-I[P] ipv4");
        System.out.println(" ".repeat(indention) + "-ip ipv4");
        System.out.println(" ".repeat(indention + 6) + "specifies destination to ping");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-i[nterval] time");
        System.out.println(" ".repeat(indention + 6) + "specifies the interval time to ping the address in milliseconds. If not specified the default value is `100'");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-l[atency] time");
        System.out.println(" ".repeat(indention + 6) + "prints only the pings that are above or equal to the specified latency in milliseconds. If not specified every ping will be printed");
        System.out.println();
        System.out.println(" ".repeat(indention) + "");

        System.out.println("SETTINGS");
        System.out.println(StringUtils.padding("-h[elp]", padding) + "to see this message.");
        System.out.println(StringUtils.padding("-ip <IP>", padding) + "to start the graph with specified IP address.");
        System.out.println(StringUtils.padding("-I[P] <IP>", padding) + "refers to '-ip <IP>'.");
        System.out.println(StringUtils.padding("-f[ile] <FILE>", padding) + "to start the graph with specified IP address and Output Dump File.");
        System.out.println(StringUtils.padding("", padding) + "<FILE> will automatically be in your user.home directory and will get the file suffix .pings");
        System.out.println(StringUtils.padding("-i[nterval] <TIME>", padding) + "to specify the interval time in milliseconds to ping the specified IP address");
        System.out.println();
        System.out.println(StringUtils.padding("-l[atency] <TIME>", padding) + "to specify the latency threshold for logging messages in ms. Errors will still be logged.");
        System.out.println(StringUtils.padding("-t[ime] <MINUTES>", padding) + "to specify the minutes after which the program automatically shuts down. Specify '0' for infinite");
        System.out.println();
        System.out.println("FLAGS");
        System.out.println(StringUtils.padding("-b[ackground]", padding) + "to run this process as a daemon without console output and the argument '-f <FILE>' is needed.");
        System.out.println();
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
