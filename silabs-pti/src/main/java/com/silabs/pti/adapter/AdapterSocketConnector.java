/*******************************************************************************
 * # License
 * Copyright 2022 Silicon Laboratories Inc. www.silabs.com
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

import java.nio.charset.Charset;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * A wrapper class that wraps an apache mina IO connector into
 * 
 * @author timotej
 *
 */
public class AdapterSocketConnector {

  /**
   * The <code>timeout</code> field is used for timing out socket connection
   * requests and waiting for message responses. Units are milliseconds. Default
   * is 2000.
   */
  private static final int framingTimeout = 2000;

  private final IoConnector connector;

  public AdapterSocketConnector() {
    this.connector = new NioSocketConnector();
    connector.setConnectTimeoutMillis(framingTimeout);
    connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new PtiCodecFactory(Charset.forName("UTF-8"))));
    connector.setHandler(new ConnectionSessionHandler());
  }

  public IoConnector ioConnector() {
    return connector;
  }

  public void dispose() {
    connector.dispose();
  }

  /**
   * Creates an adapter IConnection out of the Apache Mina based io connection.
   * 
   * @param host
   * @param port
   * @param logger
   * @return
   */
  public IConnection createConnection(final String host, final int port, final IConnectivityLogger logger) {
    return new Connection(this, host, port, logger);
  }
}
