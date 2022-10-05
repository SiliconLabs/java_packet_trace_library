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
package com.silabs.pti.adapter;

import java.io.File;
import java.io.IOException;

import com.silabs.pti.format.IDebugChannelExportFormat;
import com.silabs.pti.format.IDebugChannelExportOutput;
import com.silabs.pti.log.PtiLog;
import com.silabs.pti.util.ICharacterListener;

/**
 * Simple character listener that simply writes out the bytes as they are
 * received.
 *
 * @author timotej
 *
 */
public class UnframedConnectionListener<T> implements ICharacterListener {
  private final IDebugChannelExportOutput<T> out;
  private final IDebugChannelExportFormat<T> fileFormat;

  public UnframedConnectionListener(final File f, final IDebugChannelExportFormat<T> format) throws IOException {
    this.out = format.createOutput(f, false);
    this.fileFormat = format;
  }

  @Override
  public void received(final byte[] ch, final int offset, final int len) {
    try {
      fileFormat.writeRawUnframedData(out.writer(), ch, offset, len);
    } catch (final IOException ioe) {
      PtiLog.error("Could not write data.", ioe);
    }
  }

  public void close() throws IOException {
    out.close();
  }
}