/*******************************************************************************
 * # License
 * Copyright 2020 Silicon Laboratories Inc. www.silabs.com
 *******************************************************************************
 *
 * The licensor of this software is Silicon Laboratories Inc. Your use of this
 * software is governed by the terms of Silicon Labs Master Software License
 * Agreement (MSLA) available at
 * www.silabs.com/about-us/legal/master-software-license-agreement. This
 * software is distributed to you in Source Code format and is governed by the
 * sections of the MSLA applicable to Source Code.
 *
 ******************************************************************************/

package com.silabs.pti;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.silabs.pti.adapter.AdapterPort;
import com.silabs.pti.adapter.IConnectivityLogger;
import com.silabs.pti.format.FileFormat;
import com.silabs.pti.log.PtiSeverity;
import com.silabs.pti.util.MiscUtil;

/**
 * Command line parsing for the standalone PTI.
 *
 * Created on Feb 11, 2017
 *
 * @author timotej
 */
public class CommandLine implements IConnectivityLogger {

  private static final String DISCOVER = "-discover";
  private static final String IP = "-ip=";
  private static final String OUT = "-out=";
  private static final String TIME_LIMIT = "-time=";
  private static final String ADMIN = "-admin";
  private static final String SERIAL0 = "-serial0";
  private static final String SERIAL1 = "-serial1";
  private static final String FORMAT = "-format=";
  private static final String DELAY = "-delay=";
  private static final String INTERACTIVE = "-i";
  private static final String VERSION = "-v";
  private static final String SN = "-sn=";
  private static final String DRIFT_CORRECTION = "-driftCorrection=";
  private static final String DRIFT_CORRECTION_THRESHOLD = "-driftCorrectionThreshold=";
  private static final String ZERO_TIME_THRESHOLD = "-zeroTimeThreshold=";
  private static final String DISCRETE_NODE_CAPTURE = "-discreteNodeCapture";
  private static final String TEST_PORT = "-testPort=";

  private List<String> hostnames = new ArrayList<>();
  private String output = null;

  private FileFormat fileFormat = FileFormat.RAW;

  private AdapterPort port = AdapterPort.DEBUG;
  private final List<String> commands = new ArrayList<>();

  private int timeLimitMs = Integer.MIN_VALUE;
  private int delayMs = 2000;
  private boolean interactive = false;
  private boolean discovery = false;
  protected String params[] = new String[5];
  private boolean driftCorrection = true;
  private int driftCorrectionThreshold = 5000000; // micro-second
  private int zeroTimeThreshold = 2000000; // micro-second
  private boolean discreteNodeCapture = false;
  private List<Integer> testPort = new ArrayList<>();
  private boolean testMode = false;

  private boolean shouldExit = false;
  private int exitCode = -1;

  public CommandLine(final String[] args) {
    for (final String arg : args) {
      if ("-?".equals(arg) || "--?".equals(arg) || "-help".equals(arg) || "--help".equals(arg)) {
        usage(0);
        return;
      }

      if (arg.startsWith(IP)) {
        final String ipField = arg.substring(IP.length());
        final List<String> ipAddresses = getIpAddresses(ipField);

        if (ipAddresses.size() > 0) {
          hostnames = ipAddresses;
        } else {
          hostnames = new ArrayList<>(Arrays.asList(ipField.replaceAll(" ", "").split(",")));
        }
      } else if (arg.startsWith(SN)) {
        hostnames.add("127.0.0.1");
      } else if (arg.startsWith(OUT)) {
        output = arg.substring(OUT.length());
      } else if (arg.startsWith(TIME_LIMIT)) {
        try {
          timeLimitMs = MiscUtil.parseInt(arg.substring(TIME_LIMIT.length()));
        } catch (final NumberFormatException pe) {
          usage(1);
          return;
        }
      } else if (arg.startsWith(DELAY)) {
        try {
          delayMs = MiscUtil.parseInt(arg.substring(DELAY.length()));
        } catch (final NumberFormatException pe) {
          usage(1);
          return;
        }
      } else if (arg.startsWith(ADMIN)) {
        port = AdapterPort.ADMIN;
      } else if (arg.startsWith(SERIAL0)) {
        port = AdapterPort.SERIAL0;
      } else if (arg.startsWith(SERIAL1)) {
        port = AdapterPort.SERIAL1;
      } else if (arg.startsWith(FORMAT)) {
        final String fmt = arg.substring(FORMAT.length());
        try {
          fileFormat = FileFormat.valueOf(fmt.toUpperCase());
          if (fileFormat == null)
            throw new Exception();
        } catch (final Exception e) {
          System.err.println("Invalid file format: " + fmt);
          usage(1);
          return;
        }
      } else if (arg.equals(INTERACTIVE)) {
        interactive = true;
      } else if (arg.equals(VERSION)) {
        printVersionAndExit();
      } else if (arg.equals(DISCOVER)) {
        discovery = true;
      } else if (arg.startsWith(DRIFT_CORRECTION)) {
        driftCorrection = arg.substring(DRIFT_CORRECTION.length()).toLowerCase().indexOf("enable") >= 0;
      } else if (arg.startsWith(DRIFT_CORRECTION_THRESHOLD)) {
        try {
          driftCorrectionThreshold = MiscUtil.parseInt(arg.substring(DRIFT_CORRECTION_THRESHOLD.length()));
        } catch (final NumberFormatException pe) {
          usage(1);
          return;
        }
      } else if (arg.startsWith(ZERO_TIME_THRESHOLD)) {
        try {
          zeroTimeThreshold = MiscUtil.parseInt(arg.substring(ZERO_TIME_THRESHOLD.length()));
        } catch (final NumberFormatException pe) {
          usage(1);
          return;
        }
      } else if (arg.startsWith(DISCRETE_NODE_CAPTURE)) {
        discreteNodeCapture = true;
      } else if (arg.startsWith(TEST_PORT)) {
        testMode = true;
        port = AdapterPort.TEST;
        try {
          final String argStr = arg.substring(TEST_PORT.length());
          final String[] ports = argStr.replaceAll(" ", "").split(",");
          testPort = Arrays.asList(ports).stream().map(x -> Integer.parseInt(x)).collect(Collectors.toList());
        } catch (final Exception e) {
          usage(1);
        }
      } else {
        if (port != AdapterPort.DEBUG) {
          commands.add(arg);
        }
      }
    }

    if (!interactive && !discovery) {
      if ((!testMode && testPort.size() >= 0) && hostnames.size() == 0) {
        usage(1);
      }
    }
  }

  @Override
  public void log(final PtiSeverity severity, final String message, final Throwable throwable) {
    System.out.println(severity.name() + ": " + message);
    if (throwable != null)
      throwable.printStackTrace(System.out);
  }

  public boolean shouldExit() {
    return shouldExit;
  }

  public int exitCode() {
    return exitCode;
  }

  @Override
  public int bpsRecordPeriodMs() {
    return 1000;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  private List<String> getIpAddresses(final String path) {
    final List<String> ips = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
      String line = reader.readLine();
      while (line != null) {
        line = line.trim().replace("\n", "").replace("\r", "");
        if (line.startsWith("#") || line.isEmpty()) {
          line = reader.readLine();
          continue;
        }
        ips.add(line.trim());
        // read next line
        line = reader.readLine();
      }
      reader.close();
    } catch (final IOException e) {
    }
    return ips;
  }

  private void printVersionAndExit() {
    String date = "unknown";
    String hash = "unknown";
    String version = "unknown";

    final URL u = getClass().getClassLoader().getResource("build_pti.stamp");
    if (u != null) {
      final Properties p = new Properties();
      try {
        try (InputStream is = u.openStream()) {
          p.load(is);
        }
        date = p.getProperty("date");
        hash = p.getProperty("hash");
        version = p.getProperty("version");
      } catch (final Exception e) {
        System.err.println("Error reading build information.");
      }
    }
    System.out.println("Library information:");
    System.out.println("  - version: " + version);
    System.out.println("  - date: " + date);
    System.out.println("  - hash: " + hash);
    System.exit(0);
  }

  /*
   * name of the current running jar file.
   */
  private String filename() {
    try {
      final URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
      String file = location.getFile();
      file = file.substring(0, file.lastIndexOf('!'));
      final String[] split = file.split("[//\\\\]");
      return split[split.length - 1];
    } catch (final Exception e) {
      return "";
    }
  }

  /**
   * Prints usage and exits with a given exit code.
   */
  public void usage(final int returnedExitCode) {
    final String filename = filename();
    System.out.println("Usage: java -jar " + filename + " [ARGUMENTS] [COMMANDS]");
    System.out.println("\nMandatory arguments:\n");
    System.out.println("  " + IP
        + "<HOSTNAMES> - specify adapter names or IP addresses to connect to (may be ommited in case of -discover).");
    System.out.println("\nOptional arguments:\n");
    System.out.println("  " + INTERACTIVE
        + " - drop into interactive mode after connecting to adapter. Type 'help' once in interactive mode.");
    System.out.println("  " + TIME_LIMIT
        + "<TIME_IN_MS> - how long to capture, before connection is closed and program shuts down. Default is 1 year.");
    System.out.println("  " + DELAY
        + "<TIME_IN_MS> - how much delay is put after each command when running commands over admin port. Default is 2 seconds.");
    System.out.println("  " + OUT + "<FILENAME> - specify filename where to capture to.");
    System.out.println("  " + ADMIN + " - connect to admin port and execute COMMANDS one after another");
    System.out.println("  " + SERIAL0 + " - connect to serial0 port and execute COMMANDS one after another");
    System.out.println("  " + SERIAL1 + " - connect to serial1 port and execute COMMANDS one after another");
    System.out.println("  " + FORMAT + "[" + FileFormat.displayOptionsAsString() + "] - specify a format for output.");
    System.out.println("  " + VERSION + " - print version and exit.");
    System.out.println("  " + DISCOVER + " - run UDP discovery and print results.");
    System.out.println("  " + DRIFT_CORRECTION
        + "[enable, disable] - perform drift time correction for incoming packets. Default is enabled.");
    System.out.println("  " + DRIFT_CORRECTION_THRESHOLD + " - drift time correction threshold (micro-sec).");
    System.out.println("  " + ZERO_TIME_THRESHOLD + " - zero time threshold (micro-sec).");
    System.out.println("  " + DISCRETE_NODE_CAPTURE
        + " - each node stream gets its own log file. Each filename is \"-out\" option combined with \"_$ip\" suffix. Time Sync is disabled. ");
    System.out.println("  " + Main.PROPERTIES + "<FILE> - specify path to file, where each line in file has a single entry in format of argument=value. "
        + "On Windows, path separators need to be escaped.");
    System.out.println("\nFile formats:\n");
    for (final FileFormat ff : FileFormat.values()) {
      System.out.println("  " + ff.name().toLowerCase() + " - " + ff.format().description());
    }
    System.out.println("\nExamples:\n");
    System.out.println("  'java -jar " + filename
        + " -ip=10.4.186.138'                                                     => capture from given device and print raw events to stdout.");
    System.out.println("  'java -jar " + filename
        + " -ip=10.4.186.138,10.4.186.139'                                        => capture from given devices and print raw events to stdout.");
    System.out.println("  'java -jar " + filename
        + " -ip=10.4.186.138,10.4.186.139 -discreteNodeCapture -out=capture.log'  => capture from given devices and stream events are captured in");
    System.out.println("                                                                                                     capture_10.4.186.138.log, capture_10.4.186.139.log.");
    System.out.println("  'java -jar " + filename
        + " -ip=10.4.186.138 -admin discovery'                                    => connect to admin port and print discovery information.");
    System.out.println("  'java -jar " + filename
        + " -ip=10.4.186.138 -format=log -time=5000 -out=capture.log'             => capture for 5 seconds into capture.log, using network analyzer format.");
    this.shouldExit = true;
    this.exitCode = returnedExitCode;
  }

  public boolean hasTimeLimit() {
    return timeLimitMs != Integer.MIN_VALUE;
  }

  public int timeLimitMs() {
    return timeLimitMs;
  }

  public int delayMs() {
    return delayMs;
  }

  public String[] hostnames() {
    return hostnames.toArray(new String[0]);
  }

  public String output() {
    return output;
  }

  public AdapterPort port() {
    return port;
  }

  public List<String> commands() {
    return commands;
  }

  public FileFormat fileFormat() {
    return fileFormat;
  }

  public boolean isInteractive() {
    return interactive;
  }

  public boolean isDiscovery() {
    return discovery;
  }

  public boolean driftCorrection() {
    return driftCorrection;
  }

  public int driftCorrectionThreshold() {
    return driftCorrectionThreshold;
  }

  public int zeroTimeThreshold() {
    return zeroTimeThreshold;
  }

  public boolean discreteNodeCapture() {
    return discreteNodeCapture;
  }

  public List<Integer> testPort() {
    return testPort;
  }

  public boolean testMode() {
    return testMode;
  }

}
