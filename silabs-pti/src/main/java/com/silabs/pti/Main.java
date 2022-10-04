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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.silabs.pti.adapter.Adapter;
import com.silabs.pti.adapter.AdapterPort;
import com.silabs.pti.adapter.AdapterSocketConnector;
import com.silabs.pti.adapter.AsciiFramer;
import com.silabs.pti.adapter.CharacterCollector;
import com.silabs.pti.adapter.DebugChannelFramer;
import com.silabs.pti.adapter.IConnection;
import com.silabs.pti.adapter.IFramer;
import com.silabs.pti.adapter.TimeSync;
import com.silabs.pti.adapter.TimeSynchronizer;
import com.silabs.pti.adapter.UnframedConnectionListener;
import com.silabs.pti.debugchannel.DebugMessageConnectionListener;
import com.silabs.pti.debugchannel.TextConnectionListener;
import com.silabs.pti.discovery.DiscoveryUtil;
import com.silabs.pti.discovery.PrintingDiscoveryListener;
import com.silabs.pti.extcap.Extcap;
import com.silabs.pti.format.IDebugChannelExportFormat;
import com.silabs.pti.format.IDebugChannelExportOutput;
import com.silabs.pti.log.PtiLog;
import com.silabs.pti.util.LineTerminator;

/**
 * Main entry point to the standalone PTI functionality.
 *
 * Created on Feb 9, 2017
 *
 * @author timotej
 */
public class Main {

  private static final int ADMIN_PORT_OFFSET = 2;
  private static final int DEBUG_PORT_OFFSET = 5;
  public static final String PROPERTIES = "-properties=";

  private final AdapterSocketConnector adapterConnector;
  private final TimeSynchronizer timeSync;

  private final CommandLine cli;

  public static void main(final String[] args) {
    if (args.length > 0 && Extcap.EXTCAP.equals(args[0])) {
      final int errorCode = Extcap.run(args);
      System.exit(errorCode);
    } else {
      final Main m = new Main(args);
      if (m.cli.shouldExit())
        System.exit(m.cli().exitCode());

      final int code = m.run();
      m.closeConnections();
      System.exit(code);
    }
  }

  public Main(final String[] args) {
    int propertiesIndex = getPropertiesArgIndex(args);
    if (propertiesIndex >= 0) {
        String[] argsFromProps = convertPropsToArgs(args, propertiesIndex);
        cli = new CommandLine(argsFromProps);
    } else {
      cli = new CommandLine(args);
    }
    timeSync = new TimeSynchronizer(TimeSynchronizer.DEFAULT_PC_TIME_SUPPLIER,
                                    cli.driftCorrection(),
                                    cli.driftCorrectionThreshold(),
                                    cli.zeroTimeThreshold());
    adapterConnector = new AdapterSocketConnector();
  }

  /**
   * Provide index of properties argument in args array
   * @param args array of arguments
   * @return index of properties argument; -1 if not found or null/empty input
   */
  private int getPropertiesArgIndex(final String[] args) {
    if (args != null && args.length > 0) {
      for (int index = 0; index < args.length; index++) {
        if (args[index].startsWith(PROPERTIES)) {
          return index;
        }
      }
    }
    return -1;
  }

  public CommandLine cli() {
    return cli;
  }

  public int run() {
    try {
      if (cli.isInteractive()) {
        return Interactive.runInteractive(cli, timeSync);
      } else if (cli.isDiscovery()) {
        return DiscoveryUtil.runDiscovery(new PrintingDiscoveryListener());
      } else {
        switch (cli.port()) {
        case TEST:
        case DEBUG:
          return runCapture(cli.fileFormat().format(), timeSync);
        case ADMIN:
        case SERIAL0:
        case SERIAL1:
          return runCommandSequence();

        default:
          PtiLog.error("Unknown port: " + cli.port());
          return 1;
        }
      }
    } catch (final IOException ioe) {
      PtiLog.error("Failed to communicate to adapters: " + String.join(", ", cli.hostnames()), ioe);
      return 1;
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private <T> int runCapture(final IDebugChannelExportFormat<T> format,
                             final TimeSynchronizer timeSynchronizer) throws IOException {
    // inits
    final String outputFilename = cli.output();
    UnframedConnectionListener<T> dl = null;
    final HashMap<String, List<IConnection>> connections = new HashMap<>();

    OutputMap<T> output = null;

    // connections / attaching listeners
    if (!cli.fileFormat().format().isUsingDebugMessages()) {
      if (outputFilename == null) {
        throw new IOException("Need to specify a file with DUMP file format.");
      }

      dl = new UnframedConnectionListener(new File(outputFilename), cli.fileFormat().format());

      for (final String host : cli.hostnames()) {
        final IConnection c = adapterConnector.createConnection(host, AdapterPort.DEBUG.defaultPort(), cli);
        c.addCharacterListener(dl);
        c.connect();
      }
    } else {
      output = configOutputFiles(format, outputFilename);

      String timeServer = null;
      List<IConnection> adminConnections = new ArrayList<>();

      if (cli.testMode()) {
        for (final Integer port : cli.testPort()) {
          final String originator = "localhost:" + port;
          final List<IConnection> debugConnections = new ArrayList<>();
          connections.put(originator, debugConnections);

          // Debug connection
          final IConnection testPortConnection = adapterConnector.createConnection("localhost", port, cli);
          final IFramer asciiFramer = new AsciiFramer();
          testPortConnection.connect();
          testPortConnection.setFramers(asciiFramer, asciiFramer);
          testPortConnection.addConnectionListener(new TextConnectionListener(originator, output));
          debugConnections.add(testPortConnection);
        }
      } else {
        for (int i = 0; i < cli.hostnames().length; i++) {
          final String ip = cli.hostnames()[i];
          final List<IConnection> debugConnections = new ArrayList<>();
          connections.put(ip, debugConnections);

          // Debug connection
          AddressAndPort addrPort = parseIpAddrAndPort(ip, AdapterPort.DEBUG.defaultPort(), DEBUG_PORT_OFFSET);
          final IConnection debug = adapterConnector.createConnection(addrPort.getAddress(), addrPort.getPort(), cli);
          final IFramer debugChannelFramer = new DebugChannelFramer(true);
          debug.setFramers(debugChannelFramer, debugChannelFramer);
          debug.addConnectionListener(new DebugMessageConnectionListener(cli.fileFormat().format(),
                                                                         ip,
                                                                         output,
                                                                         timeSynchronizer));
          debug.connect();
          debugConnections.add(debug);

          // Admin connection / configure Time Server
          if (!cli.testMode() && cli.discreteNodeCapture() == false && cli.hostnames().length > 1) {
            addrPort = parseIpAddrAndPort(ip,AdapterPort.ADMIN.defaultPort(), ADMIN_PORT_OFFSET);
            final IConnection admin = adapterConnector.createConnection(addrPort.getAddress(), addrPort.getPort(), cli);
            final IFramer asciiFramer = new AsciiFramer();
            admin.setFramers(asciiFramer, asciiFramer);
            admin.connect();
            TimeSync.synchronizeTime(admin,
                                     debug.isConnected(),
                                     ip,
                                     ".*switched to mode: server.*",
                                     true,
                                     timeServer != null,
                                     timeServer);
            adminConnections.add(admin);
          }

          if (i == 0) {
            timeServer = cli.hostnames()[0];
          }
        }
      }

      // do not maintain open ADMIN port connections.
      for (final IConnection c : adminConnections) {
        c.close();
      }
      adminConnections.clear();
      adminConnections = null;
    }

    // sleep
    try {
      if (cli.hasTimeLimit()) {
        Thread.sleep(cli.timeLimitMs());
      } else {
        // Sit for a year
        Thread.sleep(1000 * 60 * 60 * 24 * 365);
      }
    } catch (final Exception e) {
    }

    // close handles
    if (dl != null) {
      dl.close();
    }

    if (output != null) {
      output.closeAndClear();
    }
    closeConnections(connections);
    return 0;
  }

  /**
   * Parse IP to extract base port, if included.
   * @param ip ip address to parse
   * @param defaultPort the default port to use
   * @param basePortOffset when IP includes base port, add offset to get real port
   * @return when IP has format address:port, return object <address,port>. when IP
   * does not have port, return object <address,port> with provided ip and defaultPort.
   */
  private AddressAndPort parseIpAddrAndPort(final String ip, final int defaultPort, final int basePortOffset) {
    AddressAndPort addrAndPort = new AddressAndPort(ip,defaultPort);

    if (ip != null && !ip.isBlank() && ip.indexOf(":") > 0) {
      int index = ip.indexOf(":");
      String hostName = ip.substring(0, index);
      Integer basePort = Integer.valueOf(ip.substring(index+1));
      addrAndPort = new AddressAndPort(hostName, basePort+basePortOffset);
      }

    return addrAndPort;
  }
  /**
   * Setup output stream to either write to a single, multiple files, or stdOut
   *
   * @param cli
   * @param outFilename
   * @param output
   * @throws FileNotFoundException
   */
  @SuppressWarnings("resource")
  private <T> OutputMap<T> configOutputFiles(final IDebugChannelExportFormat<T> format,
                                             final String outFilename) throws IOException {
    final OutputMap<T> output = new OutputMap<>();
    if (outFilename != null && !outFilename.isEmpty()) {
      // We have a file specified.
      if (cli.testMode()) {
        for (final Integer port : cli.testPort()) {
          final String f = makeCaptureFilenames(outFilename, port.toString());
          output.put("localhost:" + port, format.createOutput(new File(f), false));
        }
      } else if (cli.discreteNodeCapture()) {
        for (final String ip : cli.hostnames()) {
          final String f = makeCaptureFilenames(outFilename, ip.toString());
          output.put(ip, format.createOutput(new File(f), false));
        }
      } else { // capture all node traffic into 1 file.
        final IDebugChannelExportOutput<T> printStream = format.createOutput(new File(cli.output()), false);
        for (final String ip : cli.hostnames()) {
          output.put(ip, printStream);
        }
      }
    } else {
      // No file specified, we print to stdout.
      for (final String name : cli.hostnames()) {
        output.put(name, format.createStdoutOutput());
      }
    }
    return output;
  }

  public String makeCaptureFilenames(final String filename, final String filename_suffix) {
    String out = filename.substring(0, filename.lastIndexOf("."));
    out += "_";
    out += filename_suffix.replace(':', '_');
    out += filename.substring(filename.lastIndexOf("."));
    return out;
  }

  public void closeConnections() {
    closeConnections(null);
  }

  public void closeConnections(final HashMap<String, List<IConnection>> connections) {
    if (connections != null) {
      for (final String ip : connections.keySet()) {
        final List<IConnection> list = connections.get(ip);
        for (final IConnection c : list) {
          c.close();
        }
        list.clear();
      }
    }
    if (adapterConnector != null) {
      adapterConnector.dispose();
    }
  }

  private int runCommandSequence() throws IOException {
    final String hostname = cli.hostnames()[0];
    final IConnection c = Adapter.createConnection(hostname, cli.port().defaultPort(), cli);
    c.connect();

    final CharacterCollector cc = new CharacterCollector();
    c.addCharacterListener(cc);

    for (final String cmd : cli.commands()) {
      c.send(cmd + LineTerminator.CRLF);
      System.out.println(cmd);
      try {
        Thread.sleep(cli.delayMs());
      } catch (final Exception e) {
      }
      System.out.println(cc.textAndClean());
    }

    c.close();
    return 0;
  }

  /**
   * Convert arguments in properties file to CLI arguments format.
   * <br/><br/>
   * NB:
   * 1) expected format is <code>-properties=path_to_properties_file</code>,
   * where <code>path_to_properties_file</code> may be surrounded in double
   * quotes in case whitespace characters exists in path. ~ at start of path is
   * converted to user home directory.
   * 2) keys in properties file must be the same as CLI arguments, meaning they
   * start with hyphen (e.g. <code>-ip</code> or <code>-delay</code>)
   * 3) any additional arguments beside <code>-properties</code> arg will be
   * appended as-is to end of arguments list.
   * 4) in properties file, arguments without value will not have value
   * after '=' delimiter (e.g. <code>-discover=</code>).
   * <br/><br/>
   * Input Example (properties file content):
   * <code><br/>
   *  -zeroTimeThreshold=1000000<br/>
      -format=text<br/>
      -ip=1.2.3.4,9.8.7.6<br/>
      -serial0=<br/>
      -discreteNodeCapture=<br/>
      -discover=<br/>
   * </code>
   *
   * Output Example:
   *  <code>-zeroTimeThreshold=1000000 -format=text -ip=1.2.3.4,9.8.7.6 -serial0 -discreteNodeCapture -discover</code>
   *  <br/>
   * @param progArgs array of args provided to silabs-pti.jar; <code>progArgs[0]</code> is
   * argument <code>-properties=path_to_properties_file</code>
   * @param propertiesIndex index of <code>-properties</code> argument in array of args
   * @return array of strings with arguments in CLI format; empty array of strings
   * if invalid input or error processing properties file; never null
   * @throws IOException error while reading/processing properties file
   */
  private String[] convertPropsToArgs(final String[] progArgs, final int propertiesIndex) {
    List<String> args = new ArrayList<>();

    if (progArgs.length > 0 && propertiesIndex >= 0) {

      String propsFile =  progArgs[propertiesIndex].substring(PROPERTIES.length()).trim();
      //remove any whitespace and escape chars surrounding file path
      if (propsFile.startsWith("\"")) {
        propsFile = propsFile.substring(1);
      }
      if (propsFile.endsWith("\"")) {
        propsFile = propsFile.substring(0, propsFile.length()-1);
      }
      //update ~ with user home directory
      propsFile = propsFile.replaceFirst("^~", System.getProperty("user.home"));

      Properties props = new Properties();
      try (BufferedReader propsReader = new BufferedReader(new FileReader(new File(propsFile)))) {
        props.load(propsReader);

        props.forEach((k,v) -> {
          if (v instanceof String && !((String)v).isBlank()) {
            args.add(k.toString().trim()+"="+v.toString().trim());
          } else {
            args.add(k.toString());
          }
        });
      } catch (IOException e) {
        e.printStackTrace(System.out);
      }
    }
    //append any additional arguments provided to list *after* arguments from
    //properties file. Duplicate arguments later in list will override earlier
    //arguments when CommandLine processes them.
    if (progArgs.length > 0) {
      Arrays.stream(progArgs).forEach(e -> {
        if (e.startsWith(PROPERTIES)) {
          return;
        }
        args.add(e);

      });
    }

    return args.toArray(new String[0]);
  }

  /**
   * Stores IP address and its port
   * @author daperez
   *
   */
  private class AddressAndPort {
    private final String addr;
    private final int port;

    public AddressAndPort(final String addr, final int port) {
      this.addr = addr;
      this.port = port;
    }

    public String getAddress() {
      return addr;
    }

    public int getPort() {
      return port;
    }

  }
}
