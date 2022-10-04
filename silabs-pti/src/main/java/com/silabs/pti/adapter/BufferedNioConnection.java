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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * This connection should be used in case of the debug channel from the adapter.
 *
 * It is intended to be as fast sink as possible, with large internal buffer so
 * that TCP/IP can be serviced as quickly as possible.
 *
 * It also doesn't support sending anything into the debug channel.
 *
 * @author Timotej Created on Mar 27, 2018
 */
public class BufferedNioConnection extends BaseConnection {

  private SocketChannel channel = null;
  private Selector selector;
  private SelectionKey readKey;
  private Thread selectionThread = null;

  public BufferedNioConnection(final String host, final int port, final IConnectivityLogger logger) {
    super(host, port, logger);
  }

  @SuppressWarnings("resource")
  @Override
  public void connect() throws IOException {
    if (isConnected())
      return;
    InetSocketAddress address = new InetSocketAddress(host, port);
    if (connectionEnabler != null)
      connectionEnabler.prepareConnection(host + ":" + port);
    channel = SocketChannel.open(address);
    channel.configureBlocking(false);
    this.selector = Selector.open();
    this.selectionThread = new Thread() {
      @Override
      public void run() {
        logInfo("Reading thread start.");
        ByteBuffer buffer = ByteBuffer.allocate(100000);
        readLoop: while (true) {
          try {
            int ret = selector.select();
            if (ret > 0) {
              if (selector.selectedKeys().contains(readKey)) {
                // We can read:
                buffer.rewind();
                int readCount = channel.read(buffer);
                long readTime = System.currentTimeMillis();
                if (readCount == -1) {
                  // End of stream
                  break readLoop;
                } else if (readCount > 0) {
                  byte[] data = new byte[readCount];
                  buffer.rewind();
                  buffer.get(data);
                  processIncomingData(readTime, readCount, data);
                }
              }
            }
          } catch (ClosedSelectorException cse) {
            break readLoop;
          } catch (IOException ioe) {
            reportProblem("Error reading data", ioe);
            logError("Reading thread error.", ioe);
          }
        }
        logInfo("Reading thread stop.");
      }
    };
    readKey = channel.register(selector, SelectionKey.OP_READ);
    logInfo("Connect.");
    selectionThread.start();
    informListenersOfState(true);
  }

  @Override
  public void close() {
    if (!isConnected())
      return;
    try {
      logInfo("Disconnect.");
      channel.close();
      selector.close();
    } catch (IOException ioe) {
      reportProblem("Close socket.", ioe);
      logError("Disconnect error.", ioe);
    }
    if (connectionEnabler != null)
      connectionEnabler.releaseConnection(host + ":" + port);
    informListenersOfState(false);
  }

  @Override
  public void send(final byte[] message) throws IOException {
    if (!isConnected()) {
      logError("Attempting to write, but socket is not connected.", null);
      return;
    }
    byte[] outgoing = (frameOutgoing ? outgoingFramer.frame(message) : message);

    logInfo("Write " + outgoing.length + " bytes.");
    channel.write(ByteBuffer.wrap(outgoing));
  }

  @Override
  public boolean isConnected() {
    return channel != null;
  }

}
