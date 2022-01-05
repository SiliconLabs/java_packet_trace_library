[![Java CI with Gradle](https://github.com/SiliconLabs/java_packet_trace_library/actions/workflows/gradle.yml/badge.svg)](https://github.com/SiliconLabs/java_packet_trace_library/actions/workflows/gradle.yml)

# What is this?

Silabs-pti is a java based client side library, used to communicate with Silicon Labs ISA3 or WSTK adapters and stream debug channel messages to a file.
This includes energy consumption data, network packet data or any other data available over the debug channel.
Data can be saved in a PCAPNG format to be consumed by wireshark, Silicon Labs Network Analyzer, or as plain text or binary files.

It is also suitable for direct integration with the Wireshark extcap interface, so you can capture directly from Wireshark.


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

# License

This library was developed by Silicon Labs and is covered by a [standard Silicon Labs MSLA](https://www.silabs.com/about-us/legal/master-software-license-agreement).

# Other documents

- [User guide](doc/user-guide.md)
- [Wireshark integration](doc/../doc/wireshark.md)
- [Debug channel format](doc/../doc/debug-channel.md)


