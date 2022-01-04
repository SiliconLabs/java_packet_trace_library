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

/**
 * Output a text file with raw debug messages, one per line, in a hex mode.
 *
 * @author timotej
 *
 */
public class RawFileFormat implements IDebugChannelExportFormat<PrintStream> {

  public static String RAW_PREFIX = "[ ";
  public static String RAW_SUFFIX = " ]";

  @Override
  public IDebugChannelExportOutput<PrintStream> createOutput(final File f, final boolean append) throws IOException {
    return new PrintStreamOutput(f, append);
  }

  @Override
  public IDebugChannelExportOutput<PrintStream> createStdoutOutput() {
    return new PrintStreamOutput(System.out);
  }

  @Override
  public void writeHeader(final IDebugChannelExportOutput<PrintStream> out) {
  }

  @Override
  public String description() {
    return "Raw bytes of deframed debug messages, one message per line.";
  }

  @Override
  public boolean isUsingRawBytes() {
    return true;
  }

  @Override
  public boolean isUsingDebugMessages() {
    return true;
  }

  @Override
  public boolean formatDebugMessage(final IDebugChannelExportOutput<PrintStream> out,
                                    final String originator,
                                    final DebugMessage dm,
                                    final EventType type) {
    return false;
  }

  @Override
  public boolean formatRawBytes(final IDebugChannelExportOutput<PrintStream> out,
                                final byte[] rawBytes,
                                final int offset,
                                final int length) throws IOException {
    final String x = RAW_PREFIX + ByteArrayUtil.formatByteArray(rawBytes, offset, length, true, true) + RAW_SUFFIX;
    out.writer().println(x);
    return true;
  }

  @Override
  public void writeRawUnframedData(final IDebugChannelExportOutput<PrintStream> out,
                                   final byte[] rawBytes,
                                   final int offset,
                                   final int length) throws IOException {
  }

}
