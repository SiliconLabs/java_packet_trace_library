// Copyright (c) 2018 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import com.silabs.pti.util.DataBuffer;
import com.silabs.pti.util.Log;

/**
 * This implementation does not do ANY processing on the reading thread
 * besides the basic read. All deframing is processed on a separate thread.
 *
 * @author Timotej
 * Created on Mar 28, 2018
 */
public class DualThreadBufferedConnection extends BaseConnection {

  /**
   * The <code>timeout</code> field is used for timing out socket connection
   * requests and waiting for message responses.  Units are milliseconds.
   * Default is 2000.
   */
  private final int framingTimeout = 2000;

  private Socket socket;
  private Thread listenThread;

  private volatile boolean threadStopRequest = false,
                           threadRunning = false;

  // A ThreadGroup for all the listen threads.
  private static ThreadGroup listenGroup = new ThreadGroup("Connection Listeners");

  // Size of incoming buffer
  private static final int INCOMING_BUFFER = 1024*128; // 128k buffer

  private static class DataChunk {
    private final long t;
    private final int count;
    private final byte[] data;

    public DataChunk(final long t, final int count, final byte[] data) {
      this.t = t;
      this.count = count;
      this.data = new byte[count];
      System.arraycopy(data, 0, this.data, 0, count);
    }

    public long time() { return t; }
    public int count() { return count; }
    public byte[] data() { return data; }
  }

  private final DataBuffer<DataChunk> buffer
  = new DataBuffer<>(listenGroup,
                     "Deframer",
                     t -> processIncomingData(t.time(), t.count(), t.data()));

  /**
   * Constructs a Connection object that will use a socket.
   * Does not open the socket.
   *
   * @param host  the host to connect to.
   * @param port  the port to connect to.
   */
  DualThreadBufferedConnection(final String host,
             final int port,
             final IConnectivityLogger logger) {
    super(host, port, logger);
  }

  /**
   * Opens a socket to this Connection's host and port.  Starts a thread
   * to listen to the inbound messages.
   */
  @Override
  public void connect() throws IOException {
    // We don't create a new socket if we already have one.
    if ( isConnected() ) return;

    socket = new Socket();
    if ( connectionEnabler != null )
      connectionEnabler.prepareConnection(host + ":" + port);
    socket.connect(new InetSocketAddress(host, port), framingTimeout);
    logInfo("Connect.");
    startListenThread();
    informListenersOfState(true);
  }

  /**
   * Closes the socket and stops the listen thread.
   */
  @Override
  public void close() {
    stopListenThread();
    if (isConnected()) {
      try {
        logInfo("Disconnect.");
        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
      } catch (Exception e) {
        reportProblem("Close socket.", e);
        logError("Disconnect error.", e);
      }
    }
    socket = null;
    if ( connectionEnabler != null )
      connectionEnabler.releaseConnection(host + ":" + port);
    informListenersOfState(false);
  }

  /**
   * Implements Runnable in order to listen to inbound messages.
   * The listen thread is started by the <code>connect</code> method.
   */
  private void run() {
    logInfo("Reading thread start.");
    buffer.startThread();
    InputStream in = null;
    if (socket == null) {
      return;
    }
    try {
      socket.setSoTimeout(10);
      in = socket.getInputStream();
    } catch (SocketException e) {
      logError("Set timeout failed.", e);
      return;
    } catch (IOException e) {
      logError("No input stream.", e);
      return;
    }

    threadRunning = true;
    boolean socketClosedByPeer = false;
    long lastReadTime = System.currentTimeMillis();
    byte[] readBytes = new byte[INCOMING_BUFFER];
    while(!threadStopRequest) {
      int readCount = -1;
      // Read off the InputStream
      try {
        readCount = in.read(readBytes);
      } catch (InterruptedIOException e) {
        if (timedOut(lastReadTime)) {
          incomingFramer.flushMessage();
        }
        continue;
      } catch (IOException e) {
        reportProblem("Error reading data", e);
        logError("Data read error.", e);
      }

      lastReadTime = System.currentTimeMillis();
      if (readCount > -1) {
        buffer.addObject(new DataChunk(lastReadTime, readCount, readBytes));
      } else if (readCount == -1) {
        socketClosedByPeer = true;
        threadStopRequest = true;
      }
    }
    threadRunning = false;
    if (socketClosedByPeer)
      this.close();

    buffer.stopThread();
    logInfo("Reading thread stop.");
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
    OutputStream out = getOutputStream();
    if (out == null || message == null)
      return;

    byte[] outgoing = (frameOutgoing
                       ? outgoingFramer.frame(message)
                       : message);
    if (outgoing == null)
      return;
    logInfo("Write " + outgoing.length + " bytes.");
    out.write(outgoing);
    out.flush();
  }

  /**
   * Checks the socket to see if it is connected.
   *
   * @return  true  if and only if the socket is connected and not closed.
   */
  @Override
  public boolean isConnected() {
    return (socket != null && socket.isConnected() && !socket.isClosed());
  }

  /**
   * Returns the socket's input stream, or null if there is a problem.
   *
   * @return the socket's input stream, or null if there is a problem.
   */
  public InputStream getInputStream() {
    if (isConnected()) {
      try {
        return socket.getInputStream();
      } catch(Exception e) {
        Log.error("Exception getting input stream, closing socket", e);
        close();
      }
    }
    return null;
  }

  /**
   * Returns the socket's output stream, or null if there is a problem.
   *
   * @return the socket's output stream, or null if there is a problem.
   */
  public OutputStream getOutputStream() throws IOException {
    if (isConnected()) {
      try {
        return socket.getOutputStream();
      } catch(Exception e) {
        logError("Exception getting output stream, reconnecting", e);
        connect();
      }
    }
    return null;
  }

  /**
   * Start the listen thread, which processes incoming messages.
   * Called automatically by the {@link #connect} method.
   */
  public void startListenThread() {
    threadStopRequest = false;
    if (incomingFramer == null) {
      incomingFramer = new AsciiFramer();  // default framing is Ascii
    }
    if (outgoingFramer == null) {
      outgoingFramer = new AsciiFramer();  // default framing is Ascii
    }
    listenThread = new Thread(listenGroup, () -> run());
    listenThread.start();
  }

  /**
   * Stops the listen thread.  This is useful when the socket is
   * needed by another process, for example, the bootloader.
   * The thread can be restarted with the {@link #startListenThread()}
   * method.
   */
  public void stopListenThread() {
    threadStopRequest = true;
    while (threadRunning) {
      try { Thread.sleep(10); } catch (Exception e) {}
    }
  }

  private boolean timedOut(final long startTime) {
    return (System.currentTimeMillis() - startTime > framingTimeout);
  }

}

