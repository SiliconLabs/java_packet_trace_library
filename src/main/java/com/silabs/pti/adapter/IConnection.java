// Copyright (c) 2004 Ember Corporation. All rights reserved.

package com.silabs.pti.adapter;

import java.io.IOException;

import com.silabs.pti.util.ICharacterListener;



/**
 * Interface describing the adapter connection.
 *
 * @author  Matteo Neale Paris (matteo@ember.com)
 */
public interface IConnection {

  /** Sets incoming and outgoing framer */
  public void setFramers(IFramer incomingFramer, IFramer outgoingFramer);

  /** Returns incoming framer */
  public IFramer incomingFramer();

  /** Returns outgoing framer */
  public IFramer outgoingFramer();

  /** Enables or disables outgoing framing */
  public void setOutgoingFramingEnabled(boolean on);

  /**
   * Adds a character listener, that is triggered on each character,
   * regardless of framing
   */
  public void addCharacterListener(ICharacterListener listener);

  /** Removes a character listener */
  public void removeCharacterListener(ICharacterListener listener);

  /** Adds a connection listener that considers framing and report connection status */
  public void addConnectionListener(IConnectionListener listener);

  /** Removes a connection listener */
  public void removeConnectionListener(IConnectionListener listener);

  /**
   * Connection enabler is a class that can be invoked just before and just after
   * the connection is physically opened or closed.
   */
  public void setConnectionEnabler(final IConnectionEnabler enabler);

  /**
   * Ensures this connection is connected, reconnecting the underlying
   * IP socket if needed. If socket is already connected, the method may do
   * nothing.
   *
   * If method does not throe IOException, then socket
   * is connected.
   */
  public void connect() throws IOException;

  /** Closes this connection */
  public void close();

  /**
   * If this connection knows how to repair itself, this will attempt to repair it.
   * Might do nothing.
   *
   * @throws IOException
   */
  public void repair() throws IOException;

  /** Send data */
  public void send(byte[] message) throws IOException;

  /** Send a string */
  public void send(String message) throws IOException;

  /** Returns true if this connection is connected */
  public boolean isConnected();


}
