// Copyright (c) 2004 Ember Corporation. All rights reserved.

package com.silabs.pti.adapter;


/**
 * Interface for consumers of messages from a Connection
 * used for asynchronous processing of serial port data from nodes.
 * Example usages include custom logging of data, or taking action
 * based on a particular messages coming up from the node.
 *
 * @author  Matteo Neale Paris (matteo@ember.com)
 */
public interface IConnectionListener {

  /**
   * s<code>Connection</code> objects which have been set
   * to use this <code>IConnectionListener</code> call this method after stripping
   * the framing from the incoming message.
   *
   * @param message  the incoming message, stripped of its framing.
   * @param pcTime TODO
   */
  public void messageReceived(byte[] message, long pcTime);

  /**
   * It is possible for a connection to close when the peer closes the
   * <code>Socket</code>. When this happens, the <code>connectionState</code>
   * method is called on the <code>IConnectionListener</code> so that it
   * can respond accordingly.
   *
   * @param isConnected The connection state. <code>true</code> if the
   *                    connection is connected, <code>false</code> otherwise.
   *
   */
  public void connectionStateChanged(boolean isConnected);

}
