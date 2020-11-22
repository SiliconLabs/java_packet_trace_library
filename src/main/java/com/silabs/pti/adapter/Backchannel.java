// Copyright (c) 2004 Ember Corporation. All rights reserved.

package com.silabs.pti.adapter;

import java.io.IOException;

import com.silabs.pti.debugchannel.DebugMessageCollector;
import com.silabs.pti.util.ILogger;
import com.silabs.pti.util.Severity;

/**
 * Provides utilities for interacting with an Ember backchannel board and its
 * attached node.
 * <p>
 * The Ember backchannel board exposes four sockets: one for each of the two
 * serial ports (numbered 0 and 1), an admin port for sending commands to the
 * backchannel software (numbered port 2 for the purposes of this API), and a
 * port for rebooting the backchannel board if it is stuck. BackChannel manages
 * {@link IConnection} objects for these sockets, and provides wrappers for the
 * backchanel command set available over the administrative port. This includes:
 * <ul>
 * <li>Bootloading the node.
 * <li>Powering the node on and off.
 * <li>Resetting the node.
 * <li>Configuring the serial ports.
 * <li>Reading the backchannel board config and version.
 * <li>Uploading ebing files to nvram.
 * <li>Enabling Ember node debug messages.
 * </ul>
 *
 * @author Matteo Neale Paris (matteo@ember.com)
 */
public class Backchannel implements IBackchannel {


  private final IConnection[] connections = new IConnection[AdapterPort.values().length];
  private final IBackchannelPortMapper portMapper;
  private final IConnectionEnabler enabler;

  private final ILogger logger;

  private final DebugMessageCollector debugMessageCollector;
  /**
   * Constructs a BackChannel object, but does not attempt to connect the
   * sockets. A portmapper is passed as an argumnet
   *
   * @param host
   *          the host name of the backchannel board to connect to. For example:
   *          <code>"test070", "test070.hq.ember.com", or
   *              "192.168.170.70"</code>.
   */
  Backchannel(final String originatorId,
              final String host,
              final IBackchannelPortMapper portmapper,
              final IConnectionEnabler enabler,
              final IConnectivityLogger logger,
              final ConnectionType debugConnectionType) {
    this.portMapper = portmapper;
    this.enabler = enabler;
    this.logger = logger;
    this.debugMessageCollector = new DebugMessageCollector(originatorId);
    for ( AdapterPort p: AdapterPort.values() ) {
      int port = portMapper.port(p);
      if ( port == -1 ) {
        connections[p.ordinal()] = null;
      } else {
        IConnection con;

        if ( p == AdapterPort.DEBUG ) {
          con = Adapter.createConnection(debugConnectionType, host, port, logger );
        } else {
          con = Adapter.createConnection(host, port, logger );
        }

        connections[p.ordinal()] = con;
        if ( enabler != null )
          connections[p.ordinal()].setConnectionEnabler(enabler);
      }
    }
  }

  /**
   * Returns current portmapper.
   *
   * @return IBackchannelPortMapper
   */
  @Override
  public IBackchannelPortMapper portMapper() {
    return portMapper;
  }

  /**
   * Returns current connection enabler.
   *
   * @return IConnectionEnabler
   */
  @Override
  public IConnectionEnabler connectionEnabler() {
    return enabler;
  }


  /** Closes the sockets to the three backchannel ports. */
  @Override
  public void close() {
    for ( AdapterPort p: AdapterPort.values() ) {
      if ( connections[p.ordinal()] != null )
        connections[p.ordinal()].close();
    }
  }

  // Closes the socket to the specified backchannel port.
  public boolean close(final AdapterPort port) {
    if (getConnection(port) == null) {
      return false;
    } else {
      getConnection(port).close();
      return true;
    }
  }

  @Override
  public IDebugConnection debugConnection() {
    IConnection con = connections[AdapterPort.DEBUG.ordinal()];
    if ( con instanceof IDebugConnection )
      return (IDebugConnection)con;
    else
      return null;
  }

  @Override
  public boolean disableDebugChannelCapture() {
    IDebugConnection debugConn = debugConnection();
    if ( debugConn == null )
      throw new IllegalArgumentException("Debug connection not found.");

    // Turn off debugging
    debugConn.setConnectionProblemListener(null);
    debugMessageCollector.setDebugMessageListener(null);
    debugConn.removeConnectionListener(debugMessageCollector);
    logger.log(Severity.INFO, "Disable capture.", null);
    return true;
  }

  @Override
  public boolean enableDebugChannelCapture(final IDebugMessageListener debugMessageListener,
                                           final IConnectionProblemListener problemListener) {
    IDebugConnection debugConn = debugConnection();
    if ( debugConn == null )
      throw new IllegalArgumentException("Debug connection not found.");

    if (!debugConn.isConnected()) {
      boolean connected = false;
      int connectTrys = 0;
      while (!connected && (connectTrys < 3)) {
        try {
          debugConn.connect();
          connected = true;
        } catch (IOException ioe) {
          connectTrys++;
        }
      }
      if (!connected) {
        return false;
      }
    }

    // wait for half second
    try {
      Thread.sleep(500);
    } catch (Exception e) {
    }

    IFramer f = new DebugChannelFramer(true);
    debugConn.setFramers(f, f);
    if (debugMessageListener != null) {
      debugMessageCollector.setDebugMessageListener(debugMessageListener);
      debugConn.addConnectionListener(debugMessageCollector);
    }
    if ( problemListener != null )
      debugConn.setConnectionProblemListener(problemListener);

    logger.log(Severity.INFO, "Enable capture.", null);

    return true;
  }


  /**
   * Gets the Connection for the specified port.
   *
   * @param port The actul port connecting to.
   * @return the desired Connection, or null if the port was invalid.
   */
  @Override
  public IConnection getConnection(final AdapterPort port) {
    if ( port == null ) return null;
    return connections[port.ordinal()];
  }

}
