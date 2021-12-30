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

import java.io.Closeable;
import java.io.IOException;

/**
 * Defines the output operations for IO for a given export output.
 * 
 * @author timotej
 *
 */
public interface IDebugChannelExportOutput extends Closeable {

  /**
   * Output a string.
   * 
   * @param x
   */
  void println(String x) throws IOException;

  /**
   * Output raw bytes.
   * 
   * @param bytes
   * @param offset
   * @param length
   */
  void write(byte[] bytes, int offset, int length) throws IOException;

  /**
   * Closes the output for writing.
   */
  @Override
  void close() throws IOException;
}
