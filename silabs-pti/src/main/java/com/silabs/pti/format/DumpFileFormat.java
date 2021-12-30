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
package com.silabs.pti.format;

import java.io.IOException;

import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.EventType;

/**
 * Raw binary dump of data.
 * 
 * @author timotej
 *
 */
public class DumpFileFormat implements IDebugChannelExportFormat {

  @Override
  public void writeHeader(final IDebugChannelExportOutput out) {
  }

  @Override
  public String description() {
    return "Binary dump of raw bytes, no deframing.";
  }

  @Override
  public boolean isUsingRawBytes() {
    return true;
  }

  @Override
  public boolean isUsingDebugMessages() {
    return false;
  }

  @Override
  public void writeRawUnframedData(final IDebugChannelExportOutput out,
                                   final byte[] rawBytes,
                                   final int offset,
                                   final int length) throws IOException {
    out.write(rawBytes, offset, length);
  }

  @Override
  public boolean formatDebugMessage(final IDebugChannelExportOutput out,
                                    final String originator,
                                    final DebugMessage dm,
                                    final EventType type) {
    return false;
  }

  @Override
  public final boolean formatRawBytes(final IDebugChannelExportOutput out,
                                      final byte[] rawBytes,
                                      final int offset,
                                      final int length) {
    return false;
  }
}
