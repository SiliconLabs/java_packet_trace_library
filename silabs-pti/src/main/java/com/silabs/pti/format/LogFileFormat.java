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

import com.silabs.na.pcap.util.ByteArrayUtil;
import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.EventType;
import com.silabs.pti.debugchannel.PtiUtilities;
import com.silabs.pti.debugchannel.RadioConfiguration;

/**
 * Output format in Silicon Labs Network analyzer text log format.
 * 
 * @author timotej
 *
 */
public class LogFileFormat implements IDebugChannelExportFormat {

  @Override
  public void writeHeader(final IDebugChannelExportOutput out) throws IOException {
    out.println(PtiUtilities.ISD_LOG_HEADER);
  }

  @Override
  public String description() {
    return "Parsed debug messages, written into a file that Network Analyzer can import.";
  }

  @Override
  public boolean isUsingRawBytes() {
    return false;
  }

  @Override
  public boolean isUsingDebugMessages() {
    return true;
  }

  @Override
  public boolean formatDebugMessage(final IDebugChannelExportOutput out,
                                    final String originator,
                                    final DebugMessage dm,
                                    final EventType type) throws IOException {
    final byte[] contents = dm.contents();
    final String x = "[" + dm.networkTime() + " " + RadioConfiguration.FIFTEENFOUR.microsecondDuration(contents.length)
        + " " + type.value() + " " + type.name() + "] [" + originator + "] ["
        + ByteArrayUtil.formatByteArray(dm.contents()) + "]";
    out.println(x);
    return true;
  }

  @Override
  public boolean formatRawBytes(final IDebugChannelExportOutput out,
                                final byte[] rawBytes,
                                final int offset,
                                final int length) {
    return false;
  }

  @Override
  public void writeRawUnframedData(final IDebugChannelExportOutput out,
                                   final byte[] rawBytes,
                                   final int offset,
                                   final int length) throws IOException {
  }

}
