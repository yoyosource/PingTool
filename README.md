# PingTool

PingTool is for monitoring the latency or ping of a specific IP address over minutes, hours or even days.
Detect latency problems in your current network and graph the result over time.
The tool presents the tries, latency, timestamp and the time graph in one line.
Log the data in a file and analyse this log conveniently after the capture phase.
Outputs the life data into the console for easy and direct analysis.

## Data

```
              Tries         Latency                        Timestamp   Graph
                  1       24.171 ms         1578234994293   15:36:34   ***********************|
                  2       24.074 ms         1578234994479   15:36:34   ***********************:
                  3        ERROR            1578234994612   15:36:34                          |
                  4       ------            1578234996729   15:36:36                          :
                  5       24.056 ms         1578234996864   15:36:36   ***********************|
                  6       24.173 ms         1578234996995   15:36:36   ***********************:
                  7       24.102 ms         1578234997129   15:36:37   ***********************|
                  8       23.880 ms         1578234997262   15:36:37   ********************** :
                  9       24.349 ms         1578234997393   15:36:37   ***********************|
                 10       26.212 ms         1578234997528   15:36:37   ***********************:*
                 11       24.327 ms         1578234997662   15:36:37   ***********************|
                 12       24.160 ms         1578234997794   15:36:37   ***********************:
                 13       24.226 ms         1578234997926   15:36:37   ***********************|
                 14       24.361 ms         1578234998056   15:36:38   ***********************:
                 15       24.255 ms         1578234998190   15:36:38   ***********************|
                 16       25.140 ms         1578234998324   15:36:38   ***********************:
                 17       24.551 ms         1578234998459   15:36:38   ***********************|
                 18       24.158 ms         1578234998592   15:36:38   ***********************:
                 19       24.192 ms         1578234998729   15:36:38   ***********************|
                 20       24.857 ms         1578234998861   15:36:38   ***********************:
```

Every capture starts with the legend and is followed by the data each in its separate row.
To better read the data, set the console width of at least 171 characters.
The data shown above starts with the try number and is followed by the latency in milliseconds.
If an error occurred you will see the latency message 'ERROR' instead of a number.
Each following row with an error will get the latency message '------'.
Referencing a specific line in your data-set is as easy as specifying the timestamp or just the time. 
You can control what the graph shows.
This can be achieved by specifying different flags at the same time as starting the program.

## Log File

The visualisation of the log file is the same as the console output.
We suggest to open the log file produced by a capture with Sublime Text 3 and download the .sublime-syntax file to colorize the content.
<p align="center">
  <img src="/docs/LogExample.png?raw=true" alt="Log file example" height="400"/>
</p>

## How it works

The program uses the unix ping command to get the latency.
If this might not work for you please contact me via an Issue or via a Pull request.
The exact unix command is: 'ping -c 1 -W 1000 \<IP\>'