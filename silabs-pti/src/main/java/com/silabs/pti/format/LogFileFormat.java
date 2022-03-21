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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

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
public class LogFileFormat implements IDebugChannelExportFormat<PrintStream> {

  @Override
  public IDebugChannelExportOutput<PrintStream> createOutput(final File f, final boolean append) throws IOException {
    return new PrintStreamOutput(f, append);
  }

  @Override
  public IDebugChannelExportOutput<PrintStream> createStdoutOutput() {
    return new PrintStreamOutput(System.out);
  }

  @Override
  public void writeHeader(final IDebugChannelExportOutput<PrintStream> out) throws IOException {
    out.writer().println(PtiUtilities.ISD_LOG_HEADER);
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
  public boolean formatDebugMessage(final IDebugChannelExportOutput<PrintStream> out,
                                    final String originator,
                                    final DebugMessage dm,
                                    final EventType type) throws IOException {
    final byte[] contents = dm.eventContents();
    final String x = "[" + dm.networkTime() + " " + RadioConfiguration.FIFTEENFOUR.microsecondDuration(contents.length)
        + " " + type.value() + " " + type.name() + "] [" + originator + "] ["
        + ByteArrayUtil.formatByteArray(contents) + "]";
    out.writer().println(x);
    return true;
  }

  @Override
  public boolean formatRawBytes(final IDebugChannelExportOutput<PrintStream> out,
                                final long pcTimeMs,
                                final byte[] rawBytes,
                                final int offset,
                                final int length) {
    return false;
  }

  @Override
  public void writeRawUnframedData(final IDebugChannelExportOutput<PrintStream> out,
                                   final byte[] rawBytes,
                                   final int offset,
                                   final int length) throws IOException {
  }

}
