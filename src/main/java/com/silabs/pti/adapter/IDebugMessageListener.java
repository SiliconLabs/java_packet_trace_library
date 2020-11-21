// Copyright (c) 2018 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

import com.silabs.pti.debugchannel.DebugMessage;

/**
 * Listener to debug messages. Extracted from earlier IDebugListener.
 *
 * @author Timotej
 * Created on Mar 23, 2018
 */
public interface IDebugMessageListener {
  /**
   * The single method required to implement the <code>IDebugListener</code>
   * interface. <code>DebugCollator</code> objects which have been set
   * to use this <code>IDebugListener</code> call this method after collating
   * the incoming debug messages.
   *
   * @param message the incoming message.
   */
  public void processMessage(DebugMessage message);

}
