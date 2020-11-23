// Copyright (c) 2012 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.silabs.pti.splitter.ISplitter;
import com.silabs.pti.util.ICharacterListener;

/**
 * This class takes one connection and splits it into multiple connections
 * based on a splitter implementation.
 *
 * Created on Mar 5, 2013
 * @author timotej
 */
public class ConnectionSplit {

  private final ISplitter splitter;
  private final SplitConnection[] connections;
  private final IConnection connection;

  public ConnectionSplit(final IConnection connection, final ISplitter splitter) {
    if ( splitter == null )
      throw new IllegalArgumentException("Null not allowed");
    this.splitter = splitter;
    this.connection = connection;
    connections = new SplitConnection[splitter.bucketCount()];
    for ( int i=0; i<splitter.bucketCount(); i++ ) {
      connections[i] = new SplitConnection();
      splitter.setCharacterListener(i, connections[i]);
    }

    connection.addCharacterListener(splitter);
  }

  public void flush() {
    splitter.flush();
  }

  public IConnection connection(final int n) {
    if ( n < 0 || n >= connections.length ) {
      throw new IllegalArgumentException("Connection count out of bounds: " + n);
    }
    return connections[n];
  }

  private class SplitConnection implements IConnection, ICharacterListener {

    private final List<ICharacterListener> charListeners
      = new ArrayList<>();

    @Override
    public void received(final byte[] ch, final int offset, final int len) {
      for ( ICharacterListener l: charListeners ) {
        l.received(ch, offset, len);
      }
    }

    @Override
    public void addCharacterListener(final ICharacterListener listener) {
      if ( !charListeners.contains(listener) )
        charListeners.add(listener);
    }

    @Override
    public void addConnectionListener(final IConnectionListener listener) {
      connection.addConnectionListener(listener);
    }

    @Override
    public void setConnectionEnabler(final IConnectionEnabler enabler) {
      connection.setConnectionEnabler(enabler);
    }

    @Override
    public void close() {
      connection.close();
    }

    @Override
    public void connect() throws IOException {
      connection.connect();
    }

    @Override
    public void repair() throws IOException {
    }

    @Override
    public IFramer incomingFramer() {
      return connection.incomingFramer();
    }

    @Override
    public boolean isConnected() {
      return connection.isConnected();
    }

    @Override
    public void removeCharacterListener(final ICharacterListener listener) {
      charListeners.remove(listener);
    }

    @Override
    public void removeConnectionListener(final IConnectionListener listener) {
      connection.removeConnectionListener(listener);
    }

    @Override
    public void send(final byte[] message) throws IOException {
      connection.send(message);
    }

    @Override
    public void send(final String message) throws IOException {
      connection.send(message);
    }

    @Override
    public void setFramers(final IFramer incomingFramer, final IFramer outgoingFramer) {
      connection.setFramers(incomingFramer, outgoingFramer);
    }

    @Override
    public void setOutgoingFramingEnabled(final boolean on) {
      connection.setOutgoingFramingEnabled(on);
    }

    @Override
    public IFramer outgoingFramer() {
      return connection.outgoingFramer();
    }
  }

}

