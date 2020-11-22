// Copyright (c) 2014 Silicon Labs. All rights reserved.

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
