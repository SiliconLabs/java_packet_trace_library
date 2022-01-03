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
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.silabs.pti.adapter.Adapter;
import com.silabs.pti.adapter.AdapterPort;
import com.silabs.pti.adapter.DebugChannelFramer;
import com.silabs.pti.adapter.IConnection;
import com.silabs.pti.adapter.IConnectionListener;
import com.silabs.pti.adapter.IConnectivityLogger;
import com.silabs.pti.adapter.IFramer;
import com.silabs.pti.adapter.TimeSynchronizer;
import com.silabs.pti.debugchannel.DebugMessageConnectionListener;
import com.silabs.pti.debugchannel.TextConnectionListener;
import com.silabs.pti.format.FileFormat;
import com.silabs.pti.format.IDebugChannelExportOutput;
import com.silabs.pti.util.LineTerminator;
import com.silabs.pti.util.MiscUtil;

/**
 * Class that drives the command line.
 *
 * @author tecimovic
 */
public class Interactive {

  private String prompt = "$";
  private FileFormat formatType = FileFormat.LOG;
  private File out = new File("packet-trace.log");
  private IConnection debugConnection = null, cliConnection = null;
  private String host = null;
  private OutputMap<?> cliOutputMap = null;
  private OutputMap<?> captureOutputMap = null;
  private IConnectionListener connectionListener = null;
  private IConnectionListener cliConnectionListener = null;

  private String setChannelCommand = "set_channel";
  private String radioOnCommand = "set_mac_idle_mode";

  private int cliPort = AdapterPort.SERIAL0.defaultPort();

  private final IConnectivityLogger logger;
  private final TimeSynchronizer timeSync;

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Cli {
    String help() default "";

    String args() default "";
  }

  private Interactive(final CommandLine cli, final TimeSynchronizer timeSync) {
    if (cli.hostnames().length > 0)
      host = cli.hostnames()[0];
    this.logger = cli;
    this.timeSync = timeSync;
    this.cliOutputMap = new OutputMap<Object>();
    this.captureOutputMap = new OutputMap<Object>();
  }

  /**
   * Main entrypoint.
   */
  public static int runInteractive(final CommandLine cli, final TimeSynchronizer timeSync) {
    @SuppressWarnings("resource")
    final Scanner scanner = new Scanner(new InputStreamReader(System.in));
    System.out.println("Entering interactive mode. Use 'help' to get help.");
    final Interactive in = new Interactive(cli, timeSync);
    while (true) {
      in.printPompt();
      String cmd;
      try {
        cmd = scanner.nextLine();
      } catch (final Exception e) {
        cmd = null;
      }
      if (cmd == null)
        break;
      if (cmd.trim().length() == 0)
        continue;
      if (in.runCommand(cmd))
        break;
    }

    return 0;
  }

  private List<Method> getCliMethods() {
    final List<Method> l = new ArrayList<>();
    for (final Method m : getClass().getMethods()) {
      if (m.getAnnotation(Cli.class) != null)
        l.add(m);
    }
    Collections.sort(l, (o1, o2) -> o1.getName().compareTo(o2.getName()));
    return l;
  }

  private void printPompt() {
    if (connectionListener != null) {
      System.out.print("[" + connectionListener.count() + " events from " + host + "]");
    }
    System.out.print(prompt + " ");
  }

  // Returns true if quit
  private boolean runCommand(final String cmd) {
    final String[] cmdLine = cmd.split("\\s++");
    if (cmdLine.length == 0)
      return false;

    for (final Method m : getCliMethods()) {
      if (cmdLine[0].equalsIgnoreCase(m.getName())) {
        Object[] args = null;
        if (cmdLine.length > 1) {
          args = new Object[] { Arrays.copyOfRange(cmdLine, 1, cmdLine.length) };
        }
        try {
          if (m.isVarArgs() && args == null) {
            args = new Object[] { new String[0] };
          }
          final Object ret = m.invoke(this, args);
          if (ret instanceof Boolean) {
            return ((Boolean) ret).booleanValue();
          } else {
            return false;
          }
        } catch (final Exception e) {
          System.out.println("Error executing: " + cmd);
          e.printStackTrace();
          return false;
        }
      }
    }
    System.out.println("Invalid command: " + cmd);
    return false;
  }

  @Cli(help = "Quits the interactive application")
  public boolean quit() {
    return true;
  }

  @Cli(help = "Prints out the available commands")
  public void help() {
    System.out.println("Valid commands:");
    final Map<String, String> cmdsHelp = new LinkedHashMap<>();
    for (final Method m : getCliMethods()) {
      final Cli c = m.getAnnotation(Cli.class);
      final StringBuilder help = new StringBuilder();
      help.append("  ");
      help.append(m.getName().toLowerCase());
      if (c.args() != null && c.args().length() > 0) {
        help.append(" ").append(c.args());
      }
      cmdsHelp.put(help.toString(), c.help());
    }

    int n = 0;
    for (final String key : cmdsHelp.keySet()) {
      if (key.length() > n)
        n = key.length();
    }
    for (final String key : cmdsHelp.keySet()) {
      System.out.print("  ");
      System.out.print(key);
      final String help = cmdsHelp.get(key);
      if (help != null && help.length() > 0) {
        for (int i = 0; i < n - key.length(); i++)
          System.out.print(" ");
        System.out.print(" - ");
        System.out.println(help);
      }
    }
  }

  private void cli(final String s) throws IOException {
    if (cliConnection == null || !cliConnection.isConnected()) {
      try {
        cliOutputMap.put(host, (IDebugChannelExportOutput) FileFormat.TEXT.format().createStdoutOutput());
      } catch (final Exception e) {
        System.err.println("Could not open file: " + out.getAbsolutePath());
        e.printStackTrace();
        return;
      }
      cliConnectionListener = new TextConnectionListener(host, cliOutputMap);
      cliConnection = Adapter.createConnection(host, cliPort, logger);
      cliConnection.connect();
      cliConnection.addConnectionListener(cliConnectionListener);
    }
    cliConnection.send(s + LineTerminator.CRLF);
  }

  @Cli(help = "Turns the radio on or off", args = "{ on | off }")
  public void radio(final String... ch) {
    if (ch.length > 0) {
      final boolean on = ch[0].equalsIgnoreCase("on") || ch[0].equalsIgnoreCase("1");
      String cmd;
      if (on) {
        cmd = (radioOnCommand + " 1");
      } else {
        cmd = (radioOnCommand + " 0");
      }
      try {
        cli(cmd);
      } catch (final Exception e) {
        System.out.println("ERROR: could not toggle radio: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Cli(help = "Sets the channel", args = "CHANNEL")
  public void channel(final String... ch) {
    if (ch.length > 0) {
      try {
        cli(setChannelCommand + " " + ch[0]);
      } catch (final Exception e) {
        System.out.println("ERROR: could not set channel: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Cli(help = "Sets the TCP/IP port to use for the CLI interaction", args = "PORT")
  public void cli_port(final String... port) {
    if (port.length > 0) {
      try {
        cliPort = MiscUtil.parseInt(port[0]);
        if (cliConnection != null) {
          cliConnection.close();
          cliConnection = null;
        }
      } catch (final NumberFormatException nfe) {
        System.out.println("Invalid format: " + port[0]);
      }
    }
    System.out.println("Cli port is: " + cliPort);
  }

  @Cli(help = "Sets the 'set channel' command", args = "SETCHANNELCMD")
  public void channel_command(final String... chcmd) {
    if (chcmd.length > 0) {
      setChannelCommand = chcmd[0];
    }
    System.out.println("Channel command is: '" + setChannelCommand + "'");
  }

  @Cli(help = "Sets the enable radio command", args = "SETRADIOCMD")
  public void radio_command(final String... chcmd) {
    if (chcmd.length > 0) {
      radioOnCommand = chcmd[0];
    }
    System.out.println("Radio command is: '" + radioOnCommand + "'");
  }

  @Cli(help = "Sets the prompt", args = "PROMPT")
  public void prompt(final String... s) {
    if (s.length > 0)
      this.prompt = s[0];
  }

  @Cli(help = "Shows or sets the output file for captured data", args = "[FILENAME]")
  public void file(final String... s) {
    if (s.length > 0) {
      out = new File(s[0]);
    }
    System.out.println("Output file: " + out.getAbsolutePath());
  }

  @Cli(help = "Sends a command", args = "COMMAND")
  public void send(final String... ch) {
    if (ch.length > 0) {
      String command = "";
      for (final String a : ch) {
        command = command + a + " ";
      }
      try {
        cli(command);
      } catch (final Exception e) {
        System.out.println("ERROR: could not send command: " + command);
        e.printStackTrace();
      }
    }
  }

  @Cli(help = "Reconnects to the same host as previous connect command.")
  public void reconnect() {
    if (debugConnection != null) {
      debugConnection.close();
      debugConnection = null;
    }
    if (host == null) {
      System.err.println("You should first use 'connect' command at least once.");
      return;
    }
    debugConnection = Adapter.createConnection(host, AdapterPort.DEBUG.defaultPort(), logger);
    try {
      debugConnection.connect();
    } catch (final Exception e) {
      System.out.println("ERROR: could not connect: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Cli(help = "Connects to a specified hostname or IP address.", args = "HOSTNAME")
  public void connect(final String... s) {
    if (debugConnection != null) {
      debugConnection.close();
      debugConnection = null;
    }

    if (s.length < 1) {
      System.err.println("Missing argument: hostname.");
      return;
    }

    host = s[0];
    debugConnection = Adapter.createConnection(host, AdapterPort.DEBUG.defaultPort(), logger);
    try {
      debugConnection.connect();
    } catch (final Exception e) {
      System.out.println("ERROR: could not connect: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Cli(help = "Closes the connection.")
  public void close() {
    if (debugConnection != null) {
      if (connectionListener != null) {
        debugConnection.removeConnectionListener(connectionListener);
        connectionListener = null;
      }
      if (captureOutputMap != null) {
        captureOutputMap.closeAndClear();
        captureOutputMap = null;
      }
      debugConnection.close();
      debugConnection = null;
    }

    if (cliConnection != null) {
      cliConnection.close();
      cliConnection = null;
    }

  }

  @Cli(help = "Starts or stops capture", args = "start|stop")
  public void capture(final String... s) {
    if (s.length == 0) {
      System.err.println("Expecting 'start' or 'stop'.");
      return;
    }
    boolean isStart;
    if ("start".equalsIgnoreCase(s[0])) {
      isStart = true;
    } else if ("stop".equalsIgnoreCase(s[0])) {
      isStart = false;

    } else {
      System.err.println("Expecting 'start' or 'stop'.");
      return;
    }

    if (isStart && debugConnection == null) {
      System.err.println("You must connect first.");
      return;
    }

    if (isStart) {
      if (captureOutputMap != null) {
        System.err.println("Already capturing.");
        return;
      }
      final IFramer f = new DebugChannelFramer(true);
      debugConnection.setFramers(f, f);
      try {
        captureOutputMap.put(host, (IDebugChannelExportOutput) formatType.format().createOutput(out, true));
      } catch (final Exception e) {
        System.err.println("Could not open file: " + out.getAbsolutePath());
        e.printStackTrace();
        return;
      }
      connectionListener = new DebugMessageConnectionListener(formatType.format(), host, captureOutputMap, timeSync);
      debugConnection.addConnectionListener(connectionListener);
    } else {
      try {
        captureOutputMap.closeAndClear();
        captureOutputMap = null;
        debugConnection.removeConnectionListener(connectionListener);
        connectionListener = null;
      } catch (final NullPointerException e) {
        System.err.println("Stream already closed");
      }
    }
  }

  @Cli(help = "Sets the format of the capture file", args = "[raw|log|text]")
  public void format(final String... s) {
    if (s.length > 0) {
      try {
        formatType = FileFormat.valueOf(s[0].toUpperCase());
        if (formatType == null)
          throw new Exception();
      } catch (final Exception e) {
        System.err.println("Invalid file format: " + s[0]);
      }
    }
    System.out.println("Current format: " + formatType.name());
  }

}
