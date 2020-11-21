// Copyright (c) 2018 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

import org.apache.mina.core.service.IoConnector;

/**
 * Static utilities.
 *
 * @author Timotej
 * Created on Mar 20, 2018
 */
public class Adapter {

  private Adapter() {}

  /**
   * Creates instance of IConnection object.
   *
   * @param host
   * @param port
   * @param logger
   * @return IConnection
   */
  public static IConnection createConnection(final IoConnector connector,
                                             final String host,
                                             final int port,
                                             final IConnectivityLogger logger) {
    return new Connection(connector, host, port, logger);
  }

  public static IConnection createConnection(final String host,
                                             final int port,
                                             final IConnectivityLogger logger) {
    return createConnection(ConnectionType.CLASSIC, host, port, logger);
  }

  /**
   * Creates instance of IConnection object.
   *
   * @param host
   * @param port
   * @param logger
   * @return IConnection
   */
  public static IConnection createConnection(final ConnectionType type,
                                             final String host,
                                             final int port,
                                             final IConnectivityLogger logger) {
    switch(type) {
    case CLASSIC:
      return new Connection(host, port, logger);
    case BUFFERED_NIO:
      return new BufferedNioConnection(host, port, logger);
    case DUAL_THREAD_BUFFERED:
      return new DualThreadBufferedConnection(host, port, logger);
    }
    throw new IllegalArgumentException("Must provide valid connection type.");
  }

  /**
   * Creates instanceof the IBackchannel.
   *
   * @param originatorId
   * @param host
   * @param portmapper
   * @param enabler
   * @return IBackchannel
   */
  public static IBackchannel createBackchannel(final String originatorId,
                                               final String host,
                                               final IBackchannelPortMapper portmapper,
                                               final IConnectionEnabler enabler,
                                               final IConnectivityLogger logger,
                                               final ConnectionType debugConnectionType) {
    return new Backchannel(originatorId,
                           host,
                           portmapper,
                           enabler,
                           logger,
                           debugConnectionType);
  }

}
