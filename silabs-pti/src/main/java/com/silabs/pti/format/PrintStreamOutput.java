/*******************************************************************************
 * # License
 * Copyright 2021 Silicon Laboratories Inc. www.silabs.com
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.silabs.na.pcap.util.ByteArrayUtil;

/**
 * Implementation of the export output that simply wraps print stream.
 * 
 * @author timotej
 *
 */
public class PrintStreamOutput implements IDebugChannelExportOutput {

  private final PrintStream printStream;

  public PrintStreamOutput(final File f) throws IOException {
    this.printStream = new PrintStream(new FileOutputStream(f));
  }

  public PrintStreamOutput(final PrintStream printStream) {
    this.printStream = printStream;
  }

  @Override
  public void println(final String x) {
    printStream.println(x);
  }

  @Override
  public void write(final byte[] bytes, final int offset, final int length) {
    printStream.println(ByteArrayUtil.formatByteArray(bytes, offset, length, true, true));
  }

  @Override
  public void close() {
    printStream.close();
  }

}
