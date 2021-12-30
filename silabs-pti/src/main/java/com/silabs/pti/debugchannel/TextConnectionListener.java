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
package com.silabs.pti.debugchannel;

import java.io.IOException;
import java.util.Map;

import com.silabs.pti.adapter.IConnectionListener;
import com.silabs.pti.format.IDebugChannelExportOutput;
import com.silabs.pti.log.PtiLog;

/**
 * Connection listener, responsible for forwarding the data into the appropriate
 * formater.
 * 
 * @author timotej
 *
 */
public class TextConnectionListener implements IConnectionListener {
  private final String originator;
  private volatile int nReceived = 0;
  private final Map<String, IDebugChannelExportOutput> output;

  public TextConnectionListener(final String originator, final Map<String, IDebugChannelExportOutput> output) {
    this.originator = originator;
    this.output = output;
  }

  @Override
  public int count() {
    return nReceived;
  }

  @Override
  public void messageReceived(final byte[] message, final long pcTime) {
    try {
      final IDebugChannelExportOutput out = output.get(originator);
      out.println(new String(message));
    } catch (final IOException ioe) {
      PtiLog.error("Could not output a message.", ioe);
    }
  }

  @Override
  public void connectionStateChanged(final boolean isConnected) {
  }

}