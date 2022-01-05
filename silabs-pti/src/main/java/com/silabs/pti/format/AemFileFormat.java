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

import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.EventType;
import com.silabs.pti.decode.AemDecoder;
import com.silabs.pti.decode.AemSample;

/**
 * AEM file format: triplet of numbers, time/current/voltage.
 * 
 * @author timotej
 */
public class AemFileFormat implements IDebugChannelExportFormat<PrintStream> {

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
    out.writer().println("#     Time    Voltage    Current");
  }

  @Override
  public String description() {
    return "All packets but AEM data are ignored, and AEM data is written as data file, with time, voltage and current in each line.";
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
    if (!type.isAem())
      return false;

    final long microSecondTime = dm.networkTime();
    final byte[] contents = dm.contents();
    final AemDecoder ad = new AemDecoder(microSecondTime, contents);
    AemSample as;
    final StringBuilder sb = new StringBuilder();
    String sep = "";
    while ((as = ad.nextSample()) != null) {
      sb.append(String.format("%s%10d %10f %10f", sep, as.timestamp(), as.voltage(), as.current()));
      sep = "\n";
    }
    out.writer().println(sb.toString());
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
