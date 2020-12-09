# What is this?

Silabs-pti is a java based client side library, to communicate with Silicon Labs ISA3 or WSTK adapters and stream debug channel messages to a file.
That includes energy consumption data, network packet data or any other data available over the debug channel.

Data can be saved in a format to be consumed by wireshark, Silicon Labs Network Analyzer, or as plain text or binary files.

# How do I build this library?

You should build it using gradle via:
```
./gradlew all
```
This will result in following files:
```
build/libs/silabs-pti-lib-X.Y.Z-sources.jar
build/libs/silabs-pti-X.Y.Z.jar
build/libs/silabs-pti-lib-X.Y.Z.jar
build/libs/silabs-pti-lib-X.Y.Z-javadoc.jar
```
where X.Y.Z is the version of the library.

# How do I use this library?

If you wish to use this library in your own java project, then you should use `silabs-pti-lib-X.Y.Z.jar` file on your classpath, but you will have to satisfy a dependency on [Apache Mina](https://mina.apache.org/) in your own project.

If you wish to use this library as a program from the command line, then simply run a on-jar archive:
`java -jar build/libs/silabs-pti-X.Y.Z.jar`
and it will execute and print the command line usage help:
```
$ java -jar build/libs/silabs-pti-X.Y.Z.jar 

Usage: java -jar silabs-pti-X.Y.Z.jar [ARGUMENTS] [COMMANDS]

Mandatory arguments:

  -ip=<HOSTNAMES> - specify adapter names or IP addresses to connect to (may be ommited in case of -discover).

Optional arguments:

  -i - drop into interactive mode after connecting to adapter. Type 'help' once in interactive mode.
  -time=<TIME_IN_MS> - how long to capture, before connection is closed and program shuts down. Default is 1 year.
  -delay=<TIME_IN_MS> - how much delay is put after each command when running commands over admin port. Default is 2 seconds.
  -out=<FILENAME> - specify filename where to capture to.
  -admin - connect to admin port and execute COMMANDS one after another
  -serial0 - connect to serial0 port and execute COMMANDS one after another
  -serial1 - connect to serial1 port and execute COMMANDS one after another
  -format=[dump|raw|log|text] - specify a format for output.
  -v - print version and exit.
  -discover - run UDP discovery and print results.
  -driftCorrection=[enable, disable] - perform drift time correction for incoming packets. Default is enabled.
  -driftCorrectionThreshold= - drift time correction threshold (micro-sec).
  -zeroTimeThreshold= - zero time threshold (micro-sec).
  -discreteNodeCapture - each node stream gets its own log file. Each filename is "-out" option combined with "_$ip" suffix. Time Sync is disabled. 

File formats:

  dump - Binary dump of raw bytes, no deframing.
  raw - Raw bytes of deframed debug messages, one message per line.
  log - Parsed debug messages, written into a file that Network Analyzer can import.
  text - Text file format that can be used with wireshark by running through 'text2pcap -q -t %H:%M:%S. <FILENAME>'

Examples:

  'java -jar silabs-pti-1.0.1.jar -ip=10.4.186.138'                                                     => capture from given device and print raw events to stdout.
  'java -jar silabs-pti-1.0.1.jar -ip=10.4.186.138,10.4.186.139'                                        => capture from given devices and print raw events to stdout.
  'java -jar silabs-pti-1.0.1.jar -ip=10.4.186.138,10.4.186.139 -discreteNodeCapture -out=capture.log'  => capture from given devices and stream events are captured in
                                                                                                     capture_10.4.186.138.log, capture_10.4.186.139.log.
  'java -jar silabs-pti-1.0.1.jar -ip=10.4.186.138 -admin discovery'                                    => connect to admin port and print discovery information.
  'java -jar silabs-pti-1.0.1.jar -ip=10.4.186.138 -format=log -time=5000 -out=capture.log'             => capture for 5 seconds into capture.log, using network analyzer format.
```

# License

This library was developed by Silicon Labs and is covered by a [standard Silicon Labs MSLA](https://www.silabs.com/about-us/legal/master-software-license-agreement).
