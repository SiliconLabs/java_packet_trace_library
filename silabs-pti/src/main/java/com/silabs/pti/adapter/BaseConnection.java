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
import java.util.ArrayList;
import java.util.List;

import com.silabs.pti.log.PtiLog;
import com.silabs.pti.log.PtiSeverity;
import com.silabs.pti.util.ICharacterListener;

/**
 * Underlying commonality of all connections.
 *
 * This class is currently abstract, which makes it by definition evil. It's
 * following the squeeze-from-the-sides refactoring pattern, so it will
 * dissapear ultimately.
 *
 * @author Timotej Created on Mar 27, 2018
 */
abstract class BaseConnection implements IDebugConnection {

  protected final String host;
  protected final int port;
  protected final IConnectivityLogger logger;

  protected final List<ICharacterListener> characterListeners = new ArrayList<>();
  protected final List<IConnectionListener> connectionListeners = new ArrayList<>();

  protected IFramer incomingFramer = new AsciiFramer();
  protected IFramer outgoingFramer = new AsciiFramer();
  protected boolean frameOutgoing = true;

  protected IConnectionProblemListener problemListener;
  protected IConnectionEnabler connectionEnabler;

  private long resumeTime = -1;

  private final ConnectivityStats stats;

  protected BaseConnection(final String host, final int port, final IConnectivityLogger logger) {
    this.host = host;
    this.port = port;
    this.logger = logger;
    this.stats = new ConnectivityStats(logger, host + ":" + port);
  }

  @Override
  public final void addCharacterListener(final ICharacterListener listener) {
    synchronized (characterListeners) {
      if (!characterListeners.contains(listener))
        characterListeners.add(listener);
    }
  }

  @Override
  public final void removeCharacterListener(final ICharacterListener listener) {
    synchronized (characterListeners) {
      characterListeners.remove(listener);
    }
  }

  @Override
  public final void addConnectionListener(final IConnectionListener listener) {
    synchronized (connectionListeners) {
      if (!connectionListeners.contains(listener))
        connectionListeners.add(listener);
    }
  }

  @Override
  public final void removeConnectionListener(final IConnectionListener listener) {
    synchronized (connectionListeners) {
      connectionListeners.remove(listener);
    }
  }

  protected final void informListenersOfState(final boolean state) {
    synchronized (connectionListeners) {
      for (IConnectionListener l : connectionListeners) {
        try {
          l.connectionStateChanged(state);
        } catch (Exception e) {
          PtiLog.warning("Connection listener error", e);
        }
      }
    }
  }

  /**
   * Turns on and off framing of outgoing messages. On by default.
   *
   * @param on true for on, false for off.
   */
  @Override
  public final void setOutgoingFramingEnabled(final boolean on) {
    this.frameOutgoing = on;
  }

  @Override
  public final void setConnectionProblemListener(final IConnectionProblemListener l) {
    this.problemListener = l;
  }

  @Override
  public final IFramer incomingFramer() {
    return incomingFramer;
  }

  @Override
  public final IFramer outgoingFramer() {
    return outgoingFramer;
  }

  @Override
  public final void setConnectionEnabler(final IConnectionEnabler enabler) {
    this.connectionEnabler = enabler;
  }

  @Override
  public final void pauseFor(final int milliseconds) {
    resumeTime = System.currentTimeMillis() + milliseconds;
  }

  @Override
  public final void setFramers(final IFramer incomingFramer, final IFramer outgoingFramer) {
    this.incomingFramer = incomingFramer;
    this.outgoingFramer = outgoingFramer;
  }

  protected final void logInfo(final String message) {
    logger.log(PtiSeverity.INFO, host + ":" + port + " =>> " + message, null);
  }

  protected final void logError(final String message, final Throwable t) {
    logger.log(PtiSeverity.ERROR, host + ":" + port + " =>> " + message, t);
  }

  protected final void processMessage(final long pcTime, final byte[] messageBytes) {
    if (messageBytes == null) {
      return;
    }

    if (resumeTime != -1) {
      if (pcTime > resumeTime) {
        resumeTime = -1;
      } else {
        return;
      }
    }

    synchronized (connectionListeners) {
      for (IConnectionListener l : connectionListeners) {
        try {
          l.messageReceived(messageBytes, pcTime);
        } catch (Exception e) {
          PtiLog.warning("Connection listener error", e);
        }
      }
    }
  }

  protected final void reportProblem(final String msg, final Exception e) {
    if (problemListener != null) {
      problemListener.reportProblem(msg, e);
    }
  }

  protected final void processIncomingData(final long readTime, final int readCount, final byte[] readBytes) {

    stats.recordData(readTime, readCount);
    synchronized (characterListeners) {
      for (ICharacterListener l : characterListeners) {
        l.received(readBytes, 0, readCount);
      }
    }
    try {
      for (int i = 0; i < readCount; i++) {
        byte[] messageBytes = incomingFramer.assembleMessage(readBytes[i]);
        if (messageBytes != null)
          processMessage(readTime, messageBytes);
      }
    } catch (Exception e) {
      logError("Framing error.", e);
      reportProblem("Error assembling data.", e);
    }
  }

  /**
   * Sends a message to the device. Uses the {@link IFramer#toBytes(String)}
   * method of the current <code>IFramer</code> to convert the String argument to
   * bytes. Adds framing bytes unless framing has been turned off using
   * {@link #setOutgoingFramingEnabled(boolean)}.
   *
   * @param message the message to send, formatted as a String.
   * @see #send(byte[])
   */
  @Override
  public final void send(final String message) throws IOException {
    if (!isConnected())
      return;
    if (message == null)
      return;
    logInfo("Sending '" + message + "'");
    send(outgoingFramer.toBytes(message));
  }

  @Override
  public void repair() throws IOException {
  }

}
