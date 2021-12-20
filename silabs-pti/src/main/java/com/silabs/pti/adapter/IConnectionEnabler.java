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

import java.io.IOException;

/**
 * Objects of this type can intercept the connection making process.
 *
 * This is used in case of silink socket replicator. Before a socket is
 * connected the executable has to be launched.
 *
 * Created on Nov 30, 2014
 * @author timotej
 */
public interface IConnectionEnabler {

  /**
   * Before Connection class will physically connect the socket,
   * this method will be called.
   *
   * If it returns, connection will proceed, otherwise exception
   * will be propagated.
   *
   *
   * @param connectionName
   */
  public void prepareConnection(String connectionName) throws IOException;

  /**
   * When underlying connection object is ready to release a given connection
   * it will call this just after the connection is physically closed.
   *
   *
   * @param connectionName
   */
  public void releaseConnection(String connectionName);

  /**
   * If something is terribly wrong with this connection, attempt to repair it.
   */
  public void repairConnection(String connectionName) throws IOException;
}
