// Copyright (c) 2018 Silicon Labs. All rights reserved.

package com.silabs.pti.debugchannel;

import com.silabs.pti.adapter.IConnectionListener;
import com.silabs.pti.adapter.IDebugMessageListener;
import com.silabs.pti.log.PtiLog;

/**
 * A connection listener that extract byte[] messages and creates
 * DebugMessage objects out of them, feeding them upwards to a listener.
 *
 * @author Timotej
 * Created on Mar 27, 2018
 */
public class DebugMessageCollector implements IConnectionListener {

  private IDebugMessageListener listener = null;
  private final String originatorId;

  public DebugMessageCollector(final String originatorId) {
    this.originatorId = originatorId;
  }

  @Override
  public void messageReceived(final byte[] message,
                              final long pcTime) {
    DebugMessage debugMessage = DebugMessage.make(originatorId, message, pcTime);
    if (debugMessage != null && listener != null) {
      try {
        listener.processMessage(debugMessage);
      } catch (Exception e) {
        PtiLog.warning("Connection listener error", e);
      }
    }

  }

  @Override
  public void connectionStateChanged(final boolean isConnected) {
  }

  public void setDebugMessageListener(final IDebugMessageListener l) {
    this.listener = l;
  }
}
