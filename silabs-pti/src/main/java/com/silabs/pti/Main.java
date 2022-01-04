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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.silabs.pti.adapter.Adapter;
import com.silabs.pti.adapter.AdapterPort;
import com.silabs.pti.adapter.AsciiFramer;
import com.silabs.pti.adapter.CharacterCollector;
import com.silabs.pti.adapter.DebugChannelFramer;
import com.silabs.pti.adapter.IConnection;
import com.silabs.pti.adapter.IFramer;
import com.silabs.pti.adapter.AdapterSocketConnector;
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

  private final AdapterSocketConnector adapterConnector;
  private final TimeSynchronizer timeSync;

  private final CommandLine cli;

  public static void main(final String[] args) {
    if (args.length > 0 && "extcap".equals(args[0])) {
      final int errorCode = Extcap.run(args);
      System.exit(errorCode);
    } else {
      final Main m = new Main(args);
      if (m.cli.shouldExit())
        System.exit(m.cli.exitCode());

      final int code = m.run(m.cli);
      m.closeConnections();
      System.exit(code);
    }
  }

  public Main(final String[] args) {
    cli = new CommandLine(args);
    timeSync = new TimeSynchronizer(TimeSynchronizer.DEFAULT_PC_TIME_SUPPLIER,
                                    cli.driftCorrection(),
                                    cli.driftCorrectionThreshold(),
                                    cli.zeroTimeThreshold());
    adapterConnector = new AdapterSocketConnector();
  }

  public CommandLine cli() {
    return cli;
  }

  public int run(final CommandLine cli) {
    try {
      if (cli.isInteractive()) {
        return Interactive.runInteractive(cli, timeSync);
      } else if (cli.isDiscovery()) {
        return DiscoveryUtil.runDiscovery(new PrintingDiscoveryListener());
      } else {
        switch (cli.port()) {
        case TEST:
        case DEBUG:
          return runCapture(cli.fileFormat().format(), cli, timeSync);
        case ADMIN:
        case SERIAL0:
        case SERIAL1:
          return runCommandSequence(cli);

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

  private <T> int runCapture(final IDebugChannelExportFormat<T> format,
                             final CommandLine cli,
                             final TimeSynchronizer timeSynchronizer) throws IOException {
    // inits
    final String outputFilename = cli.output();
    UnframedConnectionListener dl = null;
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
      output = configOutputFiles(cli, format, outputFilename);

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
          final IConnection debug = adapterConnector.createConnection(ip, AdapterPort.DEBUG.defaultPort(), cli);
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
            final IConnection admin = adapterConnector.createConnection(ip, AdapterPort.ADMIN.defaultPort(), cli);
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
   * Setup output stream to either write to a single, multiple files, or stdOut
   * 
   * @param cli
   * @param outFilename
   * @param output
   * @throws FileNotFoundException
   */
  private <T> OutputMap<T> configOutputFiles(final CommandLine cli,
                                             final IDebugChannelExportFormat<T> format,
                                             final String outFilename) throws IOException {
    final OutputMap<T> output = new OutputMap<T>();
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
    out += filename_suffix;
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

  private int runCommandSequence(final CommandLine cli) throws IOException {
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
}
