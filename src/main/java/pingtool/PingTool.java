package pingtool;

import pingtool.capture.Ping;
import pingtool.utils.ArgumentMapper;
import pingtool.utils.ArgumentMapping;
import pingtool.utils.ExternalIP;
import pingtool.utils.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PingTool {

    private static ArgumentMapping[] argumentMappings = new ArgumentMapping[]{
            new ArgumentMapping("-ip", "-IP", "-I"),
            new ArgumentMapping("-external", true, "-eip", "-X"),
            new ArgumentMapping("-local", true, "lip", "-x"),
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
            new ArgumentMapping("-Error", true, "-E"),

            new ArgumentMapping("-interfaces", true, "-ifs"),
    };

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

        if (!argumentMapper.hasArgument() || argumentMapper.hasArgument("-?")) {
            showCompactHelp();
            return;
        }
        if (!argumentMapper.hasArgument() || argumentMapper.hasArgument("-help")) {
            System.out.println("Help Ping Tool");
            showHelp();
            return;
        }
        if (argumentMapper.hasArgument("-man")) {
            System.out.println("Help Ping Tool");
            showHelp();
            return;
        }

        if (!argumentMapper.hasArgument() || argumentMapper.hasArgument("-interfaces")) {
            showInterfaces();
            return;
        }

        if (!(argumentMapper.hasArgument("-ip") || argumentMapper.hasArgument("-external") || argumentMapper.hasArgument("-local"))) {
            System.out.println("Please specify an IP address with '-ip <IP>'");
            showHelp();
            return;
        }
        String ip = "";
        if (argumentMapper.hasArgument("-ip")) {
            ip = argumentMapper.getArgument("-ip").getValue();
            if (ip.equals("localhost")) {
                ip = "127.0.0.1";
            } else if (ip.equals("external")) {
                ip = ExternalIP.get();
            }
        } else if (argumentMapper.hasArgument("-external")) {
            ip = ExternalIP.get();
        } else if (argumentMapper.hasArgument("-local")) {
            ip = "127.0.0.1";
        }
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
        System.out.println(StringUtils.padding("usage: " + commandName, compactPadding + commandName.length()) + " [-AabdEehmpXx] [-I ip] [-f file]");
        System.out.println(StringUtils.padding("", compactPadding + commandName.length()) + " [-i interval] [-l latency] [-t time]");
    }

    public static void showHelp() {
        System.out.println("NAME");
        System.out.println(" ".repeat(indention) + commandName + " -- send ICMP ECHO_REQUEST packets to network hosts and visualises them");
        System.out.println();
        System.out.println("SYNOPSIS");
        System.out.println(" ".repeat(indention) + commandName + " [-AabdEehmpXx] [-I ip] [-f file]");
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
        System.out.println(" ".repeat(indention + 6) + "specifies destination to ping. `localhost' gets converted to `127.0.0.1'. `external' gets converted to your external ip address.");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-interface");
        System.out.println(" ".repeat(indention + 6) + " ");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-interfaces");
        System.out.println(" ".repeat(indention + 6) + " ");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-i[nterval] time");
        System.out.println(" ".repeat(indention + 6) + "specifies the interval time to ping the address in milliseconds. If not specified the default value is `100'");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-l[atency] time");
        System.out.println(" ".repeat(indention + 6) + "prints only the pings that are above or equal to the specified latency in milliseconds. If not specified every ping will be printed");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-m[an]");
        System.out.println(" ".repeat(indention + 6) + "prints this message");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-p[ing]");
        System.out.println(" ".repeat(indention + 6) + "shows ping graph in output");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-external");
        System.out.println(" ".repeat(indention) + "-eip");
        System.out.println(" ".repeat(indention) + "-X");
        System.out.println(" ".repeat(indention + 6) + "the same as `-ip external'");
        System.out.println();
        System.out.println(" ".repeat(indention) + "-local");
        System.out.println(" ".repeat(indention) + "-lip");
        System.out.println(" ".repeat(indention) + "-x");
        System.out.println(" ".repeat(indention + 6) + "the same as `-ip localhost'");
    }

    private static void showInterfaces() {
        System.out.println("Interfaces");
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                System.out.printf("Display name: %s\n", netint.getDisplayName());
                System.out.printf("Name: %s\n", netint.getName());
                System.out.printf("Interface Options:\n");
                System.out.printf("- Loopback: %s\n", netint.isLoopback());
                System.out.printf("- P2P: %s\n", netint.isPointToPoint());
                System.out.printf("- Virtual: %s\n", netint.isVirtual());
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    System.out.printf("InetAddress: %s\n", inetAddress);
                }
                System.out.printf("\n");
            }
        } catch (IOException e) {

        }
        System.out.println();
        System.out.println("External IP");
        System.out.println(ExternalIP.get());
    }

}
