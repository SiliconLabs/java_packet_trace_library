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

/**
 * Implementation of the export output that simply wraps print stream.
 * 
 * @author timotej
 *
 */
class PrintStreamOutput implements IDebugChannelExportOutput<PrintStream> {

  private final PrintStream printStream;

  @Override
  public PrintStream writer() {
    return printStream;
  }

  public PrintStreamOutput(final File f, final boolean append) throws IOException {
    this.printStream = new PrintStream(new FileOutputStream(f, append));
  }

  public PrintStreamOutput(final PrintStream printStream) {
    this.printStream = printStream;
  }

  @Override
  public void close() {
    printStream.close();
  }

}
