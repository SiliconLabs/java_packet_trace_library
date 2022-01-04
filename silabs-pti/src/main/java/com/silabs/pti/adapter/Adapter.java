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

package com.silabs.pti.adapter;

/**
 * Static utilities.
 *
 * @author Timotej Created on Mar 20, 2018
 */
public class Adapter {

  private Adapter() {
  }

  public static IConnection createConnection(final String host, final int port, final IConnectivityLogger logger) {
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
    switch (type) {
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
    return new Backchannel(originatorId, host, portmapper, enabler, logger, debugConnectionType);
  }

}
