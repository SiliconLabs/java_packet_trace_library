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
import java.io.OutputStream;

import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.EventType;

/**
 * Raw binary dump of data.
 * 
 * @author timotej
 *
 */
public class DumpFileFormat implements IDebugChannelExportFormat<OutputStream> {

  @Override
  public OutputStreamOutput createOutput(final File f, final boolean append) throws IOException {
    return new OutputStreamOutput(f, append);
  }

  @Override
  public OutputStreamOutput createStdoutOutput() {
    return new OutputStreamOutput();
  }

  @Override
  public void writeHeader(final IDebugChannelExportOutput<OutputStream> out) {
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
  public void writeRawUnframedData(final IDebugChannelExportOutput<OutputStream> out,
                                   final byte[] rawBytes,
                                   final int offset,
                                   final int length) throws IOException {
    out.writer().write(rawBytes, offset, length);
  }

  @Override
  public boolean formatDebugMessage(final IDebugChannelExportOutput<OutputStream> out,
                                    final String originator,
                                    final DebugMessage dm,
                                    final EventType type) {
    return false;
  }

  @Override
  public final boolean formatRawBytes(final IDebugChannelExportOutput<OutputStream> out,
                                      final byte[] rawBytes,
                                      final int offset,
                                      final int length) {
    return false;
  }
}
