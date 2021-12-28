[![Java CI with Gradle](https://github.com/SiliconLabs/java_packet_trace_library/actions/workflows/gradle.yml/badge.svg)](https://github.com/SiliconLabs/java_packet_trace_library/actions/workflows/gradle.yml)

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
silabs-pti/build/libs/silabs-pti-lib-X.Y.Z-sources.jar
silabs-pti/build/libs/silabs-pti-X.Y.Z.jar
silabs-pti/build/libs/silabs-pti-lib-X.Y.Z.jar
silabs-pti/build/libs/silabs-pti-lib-X.Y.Z-javadoc.jar
```
where X.Y.Z is the version of the library.

# How do I use this library?

If you wish to use this library in your own java project, then you should use `silabs-pti-lib-X.Y.Z.jar` file on your classpath, but you will have to satisfy a dependency on [Apache Mina](https://mina.apache.org/) in your own project.

If you wish to use this library as a program from the command line, then simply run a on-jar archive:
`java -jar silabs-pti/build/libs/silabs-pti-X.Y.Z.jar`
and it will execute and print the command line usage help:
```
$ java -jar silabs-pti/build/libs/silabs-pti-X.Y.Z.jar 

Usage: java -jar silabs-pti-X.Y.Z.jar \[ARGUMENTS\] \[COMMANDS\]

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

# How do I use this library with Wireshark?

This library has been designed to be used with [Wireshark extcap interface](https://www.wireshark.org/docs/wsdg_html_chunked/ChCaptureExtcap.html).
You just need to follow a simple procedure to set up Wireshark in a way where you can discovery and capture data directly from Silicon Labs WSTK adapters.

## Short instructions
1. First build this library according to instructions. (Basically run `./gradlew all`)
1. Then attempt to execute `./gradlew wireshark`. That should copy a small script into your wireshark extcap directory. (Note: this has mostly been tested on Linux.)
1. After you do this, execute wireshark. In the list of interfaces you should see all WSTK adapters that you have discovered locally. You can capture from them.

## More detailed instructions

### Silabs-pti extcap script
The key to the integration is a script `silabs-pti` that needs to be put into the wireshark extcap directory. If you are not sure where wireshark extcap directory is, you can determine it in a following way:
1. Run wireshark.
1. Select "About" from the menu.
1. Choose "Folders" tab. See the "User Extcap directory" entry there.

You need to add into this directory a short script that looks like this:

```
#!/usr/bin/env bash
export EXTCAP_LOC="<FULL PATH TO THE EXTCAP DIRECTORY WHERE YOU ARE PUTTING THIS SCRIPT>"
java -jar <FULL PATH TO WHERE YOUR PTI IS CLONED TO>/silabs-pti/build/libs/silabs-pti-<VERSION>.jar extcap $@
```

If the first argument you pass to the `silabs-pti.jar` is `extcap`, then the library will act as an extcap interface to wireshark, and will properly parse the subsequent arguments that wireshark adds in when exercising the interface.

The setting of `EXTCAP_LOC` is important, so that the silabs pti library creates a log file in that directory. If you set this up correctly, you should see a file `silabs-pti.log` in that directory, after you start up Wireshark, showing the activity between Wireshark and the `silabs-pti.jar`. 

### Ethernet Adapter Discovery

By default, adapters on a local IP subnet are discovered. Any WSTK adapter you connected via the ethernet on your local subnet will show.
You can test the discovery outside of Wireshark by doing:
  `java -jar silabs-pti-<VERSION>.jar -discover`
This will show you the list of discovered adapters. Same discovery is done by wireshark.

### USB Adapter Discovery

In order to work with USB adapter, you have to use the `silink` utility, which bridges the WSTK adapters from USB into a locally hosted IP mode. After that, you treat them the same way as an Ethernet adapter, just on a localhost.

# License

This library was developed by Silicon Labs and is covered by a [standard Silicon Labs MSLA](https://www.silabs.com/about-us/legal/master-software-license-agreement).
