# Guide to integration with [Wireshark](https://www.wireshark.org)

This library has been designed to be used with [Wireshark extcap interface](https://www.wireshark.org/docs/wsdg_html_chunked/ChCaptureExtcap.html).
You just need to follow a simple procedure to set up Wireshark in a way where you can discover and capture data directly from Silicon Labs WSTK adapters.

## Short instructions
1. First build this library according to instructions. (Basically run `./gradlew all`)
1. Then attempt to execute `./gradlew wireshark`. That should copy a small script into your wireshark extcap directory. (Note: this has mostly been tested on Linux. If you run into trouble here, please refer to more detailed instructions below.)
1. After you do this, execute wireshark. In the list of interfaces you should see all WSTK adapters that you have discovered locally. You can capture from them.

## More detailed instructions

### Silabs-pti extcap script
The key to the integration is a script `silabs-pti` that needs to be put into the wireshark extcap directory. If you are not sure where wireshark extcap directory is, you can determine its location in the following way:
1. Run wireshark.
1. Select "About" from the menu.
1. Choose "Folders" tab. See the "User Extcap directory" entry there.

You need to add into this directory a short script that looks like this (at least on Linux):

```
#!/usr/bin/env bash
export EXTCAP_LOC="<FULL PATH TO THE EXTCAP DIRECTORY WHERE YOU ARE PUTTING THIS SCRIPT>"
java -jar <FULL PATH TO WHERE YOUR PTI IS CLONED TO>/silabs-pti/build/libs/silabs-pti-<VERSION>.jar extcap $@
```

For other platforms (other than Linux), you should create an equivalent script, that simply executes the silabs-pti.jar with the first argument being `extcap`, and remaining arguments are as Wireshark passes them in. If the first argument you pass to the `silabs-pti.jar` is `extcap`, then the library will act as an extcap interface to wireshark, and will properly parse the subsequent arguments that wireshark adds in when exercising the interface.

The setting of `EXTCAP_LOC` is important, so that the silabs pti library creates a log file in that directory. If you set this up correctly, you should see a file `silabs-pti.log` in that directory, after you start up Wireshark, showing the activity between Wireshark and the `silabs-pti.jar`. 

### Ethernet Adapter Discovery

By default, adapters on the local IP subnet are discovered. Any WSTK adapter you have connected via the ethernet on your local subnet will show up.
You can test the discovery outside of Wireshark by doing:
  `java -jar silabs-pti-<VERSION>.jar -discover`
This will show you the list of discovered adapters. This same discovery is done by wireshark.

### USB Adapter Discovery

In order to work with USB adapter, you have to use the `silink` utility, which bridges the WSTK adapters from USB into a locally hosted IP mode. After that, you treat them the same way as an Ethernet adapter, just on a localhost.
