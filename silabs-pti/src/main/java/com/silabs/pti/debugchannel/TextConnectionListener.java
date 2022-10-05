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

import java.io.PrintStream;

import com.silabs.pti.OutputMap;
import com.silabs.pti.adapter.IConnectionListener;
import com.silabs.pti.format.IDebugChannelExportOutput;

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
  private final OutputMap<?> output;

  public TextConnectionListener(final String originator, final OutputMap<?> output) {
    this.originator = originator;
    this.output = output;
  }

  @Override
  public int count() {
    return nReceived;
  }

  @Override
  public void messageReceived(final byte[] message, final long pcTime) {
    @SuppressWarnings("resource")
    final IDebugChannelExportOutput<?> out = output.output(originator);
    if (out.writer() instanceof PrintStream)
      ((PrintStream) out.writer()).println(new String(message));
  }

  @Override
  public void connectionStateChanged(final boolean isConnected) {
  }

}