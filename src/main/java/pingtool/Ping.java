package pingtool;

import pingtool.utils.Average;
import pingtool.utils.StringUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

public class Ping implements Runnable {

    private long connects = 0;
    private long errors = 0;
    private int factor = 20;
    private boolean lineSwitch = false;
    private boolean works = true;

    private boolean graph_Ping;
    private boolean graph_averagePings;
    private boolean graph_AveragePings;
    private boolean graph_Error;

    private int interval = 100;

    private File file;
    private BufferedWriter bufferedWriter;

    private Average averageSmall1 = new Average(20);
    private Average averageLarge1 = new Average(1000);

    private Average averageSmall2 = new Average(20);
    private Average averageLarge2 = new Average(1000);

    private Average errorAverage = new Average(100);

    private List<String> command = new ArrayList<>();

    private boolean working = true;
    private boolean pause = false;

    public Ping(String ip, String fileName, String interval, boolean graph_Ping, boolean graph_averagePings, boolean graph_AveragePings, boolean graph_Error) {
        String message = StringUtils.leading("Tries", 19) + "   " + StringUtils.leading("Latency", 13) + "   " + StringUtils.leading("Timestamp", 30) + "   Graph";
        System.out.println(message);
        assembleCommand(ip);

        this.graph_Ping = graph_Ping;
        this.graph_averagePings = graph_averagePings;
        this.graph_AveragePings = graph_AveragePings;
        this.graph_Error = graph_Error;

        this.interval = Integer.parseInt(interval);
        if (fileName.isEmpty()) {
            return;
        }
        try {
            String userHome = System.getProperty("user.home");
            if (fileName.startsWith(userHome)) {
                fileName = fileName.substring(userHome.length());
            }
            if (fileName.endsWith(".pings")) {
                fileName = fileName.substring(0, fileName.length() - 6);
            }
            file = new File(userHome + "/" + fileName + ".pings");
            bufferedWriter = new BufferedWriter(new FileWriter(file, true));

            if (file.length() != 0) {
                bufferedWriter.newLine();
                bufferedWriter.newLine();
            }
            bufferedWriter.write(message);
            bufferedWriter.flush();
        } catch (IOException e) {

        }
    }

    private void assembleCommand(String ip) {
        if (!ip.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")) {
            System.out.println(ip);
            throw new IllegalArgumentException("IP address format exception");
        }
        command.clear();

        command.add("ping");
        // Count
        command.add("-c");
        command.add("1");
        // Wait between packets
        //command.add("-i");
        //command.add("0.1");
        // waittime (ms) reply
        command.add("-W");
        command.add("1000");
        // timeout (s)
        //command.add("-t");
        //command.add("1");
        command.add(ip);
    }

    @Override
    public void run() {
        while (working) {
            if (!pause) {
                long time = System.currentTimeMillis();
                TemporalAccessor temporalAccessor = LocalDateTime.now();

                try {
                    String commandOutput = runCommand();
                    processDataAndOutput(commandOutput, time, temporalAccessor);
                } catch (IOException e) {
                    continue;
                }
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String runCommand() throws IOException {
        String s = null;

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        List<String> output = new ArrayList<>();
        while ((s = stdInput.readLine()) != null) {
            output.add(s);
        }
        List<String> error = new ArrayList<>();
        while ((s = stdError.readLine()) != null) {
            error.add(s);
        }
        return output.get(1);
    }

    private void processDataAndOutput(String s, long time, TemporalAccessor temporalAccessor) throws IOException {
        int d = 0;
        boolean range = false;

        if (s.isEmpty() || s.contains("Destination Net Unreachable")) {
            s = "------";
            errors++;
            if (works) {
                errorAverage.add(1);
                works = false;
                s = "ERROR";
            } else {
                errorAverage.add(0.5);
            }
        } else {
            works = true;
            int i = s.indexOf("time=");
            if (i != -1) {
                s = s.substring(i + 5, s.length() - 3);
            }
            try {
                double t = Double.parseDouble(s);
                errorAverage.add(0);
                if (t < 5.0) {
                    range = true;
                    d = (int) (t * factor);
                } else {
                    d = (int) (t);
                }
                averageSmall1.add(t);
                averageLarge1.add(t);
                averageSmall2.add(t * factor);
                averageLarge2.add(t * factor);
            } catch (NumberFormatException e) {

            }
        }

        String graph;
        double error = errorAverage.average() * 100;
        if (range) {
            if (averageSmall2.average() >= 100.0) {
                graph = graph(d, s, 0, 0, error, '•');
            } else {
                graph = graph(d, s, averageSmall2.average(), averageLarge2.average(), error, '•');
            }
        } else {
            graph = graph(d, s, averageSmall1.average(), averageLarge1.average(), error, '*');
        }
        String outputToFile = StringUtils.leading(++connects + "", 19) + "   " + StringUtils.leading(s, 10) + " " + (s.equals("ERROR") || s.equals("------") ? "  " : "ms") + "   " + StringUtils.leading(time + "", 19) + "   " + DateTimeFormatter.ofPattern("HH:mm:ss").format(temporalAccessor) + "   " + graph;
        while (outputToFile.endsWith(" ")) {
            outputToFile = outputToFile.substring(0, outputToFile.length() - 1);
        }
        if (bufferedWriter != null) {
            bufferedWriter.newLine();
            bufferedWriter.write(outputToFile);
            bufferedWriter.flush();
        }
        System.out.println(outputToFile);

        //|                 56       25.044 ms         1578228526246   13:48:46   |
        // 71 chars prefix
        // 100 chars Graph
    }

    private String graph(int i, String s, double averageSmall, double averageLarge, double error, char c) {
        StringBuilder st = new StringBuilder();
        double max = Math.max(averageLarge, averageSmall);
        max = Math.max(max, error);

        if (max < 1) {
            max = 1;
        }
        if (s.contains("-") && max > 0) {
            st.append(" ".repeat((int)max - 1));
        } else if (i > 0 && graph_Ping) {
            st.append((c + "").repeat(i - 1));
        } else {
            st.append(" ".repeat((int)max - 1));
        }
        if (max - i > 0) {
            st.append(" ".repeat((int)max - i));
        }

        if ((int)error > 0 && graph_Error) {
            st.replace((int)error - 1, (int)error, "×");
        }
        if ((int)averageSmall <= 0 || (int)averageLarge <= 0) {
            return st.toString();
        }

        if ((int)averageSmall == (int)averageLarge && graph_averagePings && graph_AveragePings) {
            lineSwitch = !lineSwitch;
            if (lineSwitch) {
                st.replace((int) averageSmall - 1, (int) averageSmall, "|");
            } else if (averageLarge - 1 > 0) {
                st.replace((int) averageLarge - 1, (int) averageLarge, ":");
            }
        } else {
            if (graph_averagePings) {
                st.replace((int) averageSmall - 1, (int) averageSmall, "|");
            }
            if (graph_AveragePings) {
                st.replace((int) averageLarge - 1, (int) averageLarge, ":");
            }
        }

        return st.toString();
    }

    public void pause() {
        pause = true;
    }

    public void resume() {
        pause = false;
    }

    public void stop() {
        working = false;
    }
}
