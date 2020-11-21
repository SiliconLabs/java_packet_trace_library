// Copyright (c) 2003 Ember Corporation. All rights reserved.

package com.silabs.pti.adapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;


/**
 * The connection that wraps a basic socket connection, using a
 * standard blocking IO.
 *
 * This class was written originally in 2003 and then
 * completely turned inside out several times.
 *
 * @author  Matteo Neale Paris (matteo@ember.com)
 * @author  Timotej Ecimovic
 */
public class Connection extends BaseConnection {

  // keys for attaching context obj to IoSession.
  private static final String CONNECTION = "connection";
  private static final String CHARSET = "charset";

  /**
   * The <code>timeout</code> field is used for timing out socket connection
   * requests and waiting for message responses.  Units are milliseconds.
   * Default is 2000.
   */
  private final int framingTimeout = 2000;

  // MINA
  private final Charset defaultCharset = Charset.forName("UTF-8");
  private IoConnector connector;
  private IoSession session;
  private IoHandler handler;

  /**
   * Constructs a Connection object that will use a socket.
   * Does not open the socket.
   *
   * @param host  the host to connect to.
   * @param port  the port to connect to.
   */
  Connection(final IoConnector connector,
             final String host,
             final int port,
             final IConnectivityLogger logger) {
    super(host, port, logger);
    this.connector = connector;
    this.handler = connector.getHandler();
  }


  Connection(final String host,
             final int port,
             final IConnectivityLogger logger) {
    super(host, port, logger);
  }

  /**
   * Opens a socket to this Connection's host and port.  Starts a thread
   * to listen to the inbound messages.
   *
   * @return   true if and only if the socket was successfully opened.
   */
  @Override
  public void connect() throws IOException {
    // We don't create a new socket if we already have one.
    if (isConnected()) {
      return;
    }

    if (connectionEnabler != null) {
      connectionEnabler.prepareConnection(host + ":" + port);
    }

    if (connector == null) {
      connector = new NioSocketConnector();
      connector.setConnectTimeoutMillis(framingTimeout);
      connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new PtiCodecFactory(defaultCharset)));
      // connector.getFilterChain().addLast("logger", new LoggingFilter());
      if (handler == null) {
        handler = new ConnectionSessionHandler();
      }
      connector.setHandler(handler);
    }

    ConnectFuture future = connector.connect(new InetSocketAddress(this.host, this.port));
    future.awaitUninterruptibly();
    session = future.getSession();
    session.setAttribute(CONNECTION, this);
    session.setAttribute(CHARSET, defaultCharset);

    logInfo("Connect.");
    initFramers();
    informListenersOfState(true);
  }

  /**
   * Closes the socket and stops the listen thread.
   */
  @Override
  public void close() {
    if (isConnected()) {
      try {
        logInfo("Disconnect.");
        session.closeNow();
      } catch (Exception e) {
        reportProblem("Close socket.", e);
        logError("Disconnect error.", e);
      }
    }

    // clean up
    session = null;

    if ( connectionEnabler != null )
      connectionEnabler.releaseConnection(host + ":" + port);
    informListenersOfState(false);
  }

  /**
   * Sends a message to the device.  Uses the {@link IFramer#frame(byte[])}
   * method of the current <code>IFramer</code> to add framing, unless it
   * has been turned off with {@link #setOutgoingFramingEnabled(boolean)}.  If a
   * log has been started, prints the unframed message to the log
   * after formatting it as a String using {@link IFramer#toString(byte[])}.
   *
   * @param message  the message to send, as an array of ints.
   * @see            #send(String)
   */
  @Override
  public void send(final byte[] message) throws IOException {
    IoSession out = getOutputSession();
    Charset charset = (Charset) out.getAttribute(CHARSET);

    if (out == null || message == null || charset == null)
      return;

    byte[] outgoing = frameOutgoing ? outgoingFramer.frame(message) : message;
    if (outgoing == null) {
      return;
    }

    IoBuffer buf = IoBuffer.allocate(outgoing.length).setAutoExpand(true);
    buf.put(outgoing);
    buf.flip();
    out.write(buf);
    logInfo("Write " + outgoing.length + " bytes.");
  }

  /**
   * Checks the socket to see if it is connected.
   *
   * @return  true  if and only if the socket is connected and not closed.
   */
  @Override
  public boolean isConnected() {
    return session != null && session.isConnected() && !session.isClosing();
  }

  /**
   * Returns the socket's input stream, or null if there is a problem.
   *
   * @return the socket's input stream, or null if there is a problem.
   */
  public IoSession getInputSession() {
    return isConnected() ? session : null;
  }

  /**
   * Returns the socket's output stream, or null if there is a problem.
   *
   * @return the socket's output stream, or null if there is a problem.
   */
  public IoSession getOutputSession() {
    return isConnected() ? session : null;
  }

  public IoSession getIoSession() {
    return isConnected() ? session : null;
  }

  /**
   * Start the listen thread, which processes incoming messages.
   * Called automatically by the {@link #connect} method.
   */
  public void initFramers() {
    if (incomingFramer == null) {
      incomingFramer = new AsciiFramer();  // default framing is Ascii
    }
    if (outgoingFramer == null) {
      outgoingFramer = new AsciiFramer();  // default framing is Ascii
    }
  }

  @Override
  public void repair() throws IOException {
    if ( connectionEnabler != null ) {
      connectionEnabler.repairConnection(host + ":" + port);
    }
  }
}

