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

/**
 * Export output based on FileOutputStream
 * 
 * @author timotej
 *
 */
public class FileOutput implements IDebugChannelExportOutput {

  private final FileOutputStream fos;

  public FileOutput(final File f) throws IOException {
    this.fos = new FileOutputStream(f);
  }

  public FileOutput(final File f, final boolean append) throws IOException {
    this.fos = new FileOutputStream(f, append);
  }

  @Override
  public void println(final String x) throws IOException {
    fos.write(x.getBytes());
  }

  @Override
  public void write(final byte[] bytes, final int offset, final int length) throws IOException {
    fos.write(bytes, offset, length);
  }

  @Override
  public void close() throws IOException {
    fos.close();
  }

}
