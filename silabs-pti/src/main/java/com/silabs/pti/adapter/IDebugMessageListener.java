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
