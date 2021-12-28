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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.silabs.pti.adapter.Adapter;
import com.silabs.pti.adapter.AdapterPort;
import com.silabs.pti.adapter.AsciiFramer;
import com.silabs.pti.adapter.CharacterCollector;
import com.silabs.pti.adapter.ConnectionSessionHandler;
import com.silabs.pti.adapter.DebugChannelFramer;
import com.silabs.pti.adapter.IConnection;
import com.silabs.pti.adapter.IConnectionListener;
import com.silabs.pti.adapter.IFramer;
import com.silabs.pti.adapter.PtiCodecFactory;
import com.silabs.pti.adapter.TimeSync;
import com.silabs.pti.adapter.TimeSynchronizer;
import com.silabs.pti.discovery.DiscoveryUtil;
import com.silabs.pti.discovery.PrintingDiscoveryListener;
import com.silabs.pti.extcap.Extcap;
import com.silabs.pti.log.PtiLog;
import com.silabs.pti.util.ICharacterListener;
import com.silabs.pti.util.LineTerminator;

/**
 * Main entry point to the standalone PTI functionality.
 *
 * Created on Feb 9, 2017
 * 
 * @author timotej
 */
public class Main {
  /**
   * The <code>timeout</code> field is used for timing out socket connection
   * requests and waiting for message responses. Units are milliseconds. Default
   * is 2000.
   */
  private static final int framingTimeout = 2000;

  private final IoConnector connector = new NioSocketConnector();
  private final TimeSynchronizer timeSync;

  private final CommandLine cli;

  public static void main(final String[] args) {
    if (args.length > 0 && "extcap".equals(args[0])) {
      int errorCode = Extcap.run(args);
      System.exit(errorCode);
    } else {
      Main m = new Main(args);
      if (m.cli.shouldExit())
        System.exit(m.cli.exitCode());

      int code = m.run(m.cli);
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

    connector.setConnectTimeoutMillis(framingTimeout);
    connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new PtiCodecFactory(Charset.forName("UTF-8"))));
    connector.setHandler(new ConnectionSessionHandler());
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
          return runCapture(cli, timeSync);
        case ADMIN:
        case SERIAL0:
        case SERIAL1:
          return runCommandSequence(cli);

        default:
          PtiLog.error("Unknown port: " + cli.port());
          return 1;
        }
      }
    } catch (IOException ioe) {
      PtiLog.error("Failed to communicate to adapters: " + String.join(", ", cli.hostnames()), ioe);
      return 1;
    }
  }

  private static class DumpListener implements ICharacterListener {
    private final FileOutputStream fos;

    public DumpListener(final File f) throws IOException {
      fos = new FileOutputStream(f);
    }

    @Override
    public void received(final byte[] ch, final int offset, final int len) {
      try {
        fos.write(ch, offset, len);
      } catch (IOException ioe) {
        PtiLog.error("Could not write data.", ioe);
      }
    }

    public void close() throws IOException {
      fos.close();
    }
  }

  private int runCapture(final CommandLine cli, final TimeSynchronizer timeSynchronizer) throws IOException {
    // inits
    final String outputFilename = cli.output();
    DumpListener dl = null;
    HashMap<String, PrintStream> output = new HashMap<>();
    HashMap<String, List<IConnection>> connections = new HashMap<>();

    // connections / attaching listeners
    if (cli.fileFormat() == FileFormat.DUMP) {
      if (outputFilename == null) {
        throw new IOException("Need to specify a file with DUMP file format.");
      }

      dl = new DumpListener(new File(outputFilename));

      for (String host : cli.hostnames()) {
        IConnection c = Adapter.createConnection(connector, host, AdapterPort.DEBUG.defaultPort(), cli);
        c.connect();
        c.addCharacterListener(dl);
      }
    } else {
      configOutputFiles(cli, outputFilename, output);

      String timeServer = null;
      List<IConnection> adminConnections = new ArrayList<>();

      if (cli.testMode()) {
        for (Integer port : cli.testPort()) {
          String originator = "localhost:" + port;
          List<IConnection> debugConnections = new ArrayList<>();
          connections.put(originator, debugConnections);

          // Debug connection
          IConnection testPortConnection = Adapter.createConnection(connector, "localhost", port, cli);
          IFramer asciiFramer = new AsciiFramer();
          testPortConnection.connect();
          testPortConnection.setFramers(asciiFramer, asciiFramer);
          testPortConnection.addConnectionListener(new SimpleConnectionListener(cli.fileFormat(),
                                                                                originator,
                                                                                output,
                                                                                true,
                                                                                timeSynchronizer));
          debugConnections.add(testPortConnection);
        }
      } else {
        for (int i = 0; i < cli.hostnames().length; i++) {
          String ip = cli.hostnames()[i];
          List<IConnection> debugConnections = new ArrayList<>();
          connections.put(ip, debugConnections);

          // Debug connection
          IConnection debug = Adapter.createConnection(connector, ip, AdapterPort.DEBUG.defaultPort(), cli);
          IFramer debugChannelFramer = new DebugChannelFramer(true);
          debug.connect();
          debug.setFramers(debugChannelFramer, debugChannelFramer);
          debug.addConnectionListener(new SimpleConnectionListener(cli.fileFormat(),
                                                                   ip,
                                                                   output,
                                                                   false,
                                                                   timeSynchronizer));
          debugConnections.add(debug);

          // Admin connection / configure Time Server
          if (!cli.testMode() && cli.discreteNodeCapture() == false && cli.hostnames().length > 1) {
            IConnection admin = Adapter.createConnection(connector, ip, AdapterPort.ADMIN.defaultPort(), cli);
            IFramer asciiFramer = new AsciiFramer();
            admin.connect();
            admin.setFramers(asciiFramer, asciiFramer);
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
      for (IConnection c : adminConnections) {
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
    } catch (Exception e) {
    }

    // close handles
    if (dl != null) {
      dl.close();
    }

    if (output != null) {
      output.forEach((k, v) -> v.close());
      output.clear();
      output = null;
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
  private void configOutputFiles(final CommandLine cli,
                                 final String outFilename,
                                 final HashMap<String, PrintStream> output) throws FileNotFoundException {
    if (outFilename != null && !outFilename.isEmpty()) {
      if (cli.testMode()) {
        for (Integer port : cli.testPort()) {
          String f = makeCaptureFilenames(outFilename, port.toString());
          output.put("localhost:" + port, new PrintStream(new FileOutputStream(new File(f))));
        }
      } else if (cli.discreteNodeCapture()) {
        for (String ip : cli.hostnames()) {
          String f = makeCaptureFilenames(outFilename, ip.toString());
          output.put(ip, new PrintStream(new FileOutputStream(new File(f))));
        }
      } else { // capture all node traffic into 1 file.
        PrintStream printStream = new PrintStream(new FileOutputStream(new File(cli.output())));
        for (String ip : cli.hostnames()) {
          output.put(ip, printStream);
        }
      }
    } else {
      for (String name : cli.hostnames()) {
        output.put(name, System.out);
      }
    }
  }

  public String makeCaptureFilenames(String filename, String filename_suffix) {
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
      for (String ip : connections.keySet()) {
        List<IConnection> list = connections.get(ip);
        for (IConnection c : list) {
          c.close();
        }
        list.clear();
      }
    }
    if (connector != null) {
      connector.dispose();
    }
  }

  private int runCommandSequence(final CommandLine cli) throws IOException {
    String hostname = cli.hostnames()[0];
    IConnection c = Adapter.createConnection(hostname, cli.port().defaultPort(), cli);
    c.connect();

    CharacterCollector cc = new CharacterCollector();
    c.addCharacterListener(cc);

    for (String cmd : cli.commands()) {
      c.send(cmd + LineTerminator.CRLF);
      System.out.println(cmd);
      try {
        Thread.sleep(cli.delayMs());
      } catch (Exception e) {
      }
      System.out.println(cc.textAndClean());
    }

    c.close();
    return 0;
  }
}

class SimpleConnectionListener implements IConnectionListener {
  private final FileFormat ff;
  private final String originator;
  private volatile int nReceived = 0;
  private final HashMap<String, PrintStream> output;
  private long t0 = -1;
  private boolean readAsText = true;
  private final TimeSynchronizer timeSync;

  // typically we capture from N devices and write to 1 single file.
  // this ensures us to only write 1 header entry.
  private static HashSet<PrintStream> writtenHeader = new HashSet<>();

  public SimpleConnectionListener(final FileFormat ff,
                                  final String originator,
                                  final HashMap<String, PrintStream> output,
                                  final boolean readText,
                                  final TimeSynchronizer timeSynchronizer) {
    this.ff = ff;
    this.originator = originator;
    this.output = output;
    this.readAsText = readText;
    this.timeSync = timeSynchronizer;

    // write header
    if (!readAsText) {
      output.forEach((k, v) -> {
        if (!writtenHeader.contains(v)) {
          if (ff.header() != null) {
            v.println(ff.header());
          }
          writtenHeader.add(v);
        }
      });
    }
  }

  public int count() {
    return nReceived;
  }

  @Override
  public void messageReceived(final byte[] message, final long pcTime) {
    PrintStream outputStream = output.get(originator);
    if (!readAsText) {
      long t;
      if (t0 == -1) {
        t0 = System.currentTimeMillis();
        t = 0;
      } else {
        t = System.currentTimeMillis() - t0;
      }
      String formatted = ff.processDebugMsg(t, originator, message, timeSync);
      if (formatted != null) {
        outputStream.println(formatted);
        nReceived++;
      }
    } else {
      outputStream.println(new String(message));
    }
  }

  @Override
  public void connectionStateChanged(final boolean isConnected) {
  }
}
