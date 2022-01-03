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
import java.io.IOException;

import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.EventType;

/**
 * Each supported file format implements this interface.
 * 
 * @author timotej
 *
 */
public interface IDebugChannelExportFormat<T> {

  public IDebugChannelExportOutput<T> createStdoutOutput() throws IOException;

  public IDebugChannelExportOutput<T> createOutput(File f, boolean append) throws IOException;

  /**
   * Returns human readable description of this file format.
   * 
   * @return
   */
  public String description();

  /**
   * Returns the header that is put on the top of the text file.
   */
  public void writeHeader(IDebugChannelExportOutput<T> out) throws IOException;

  /**
   * Formatter returns true if the raw bytes are used, or false if the debug
   * message is used after time synchronization.
   * 
   * Note that deframing still happens. Deframing is controlled via
   * isUsingDebugMessages().
   * 
   * @return true or false.
   */
  public boolean isUsingRawBytes();

  /**
   * If this is set to false, then no deframing happens on the input data, and
   * they are just passed to the formater as they come, simply a sequence of
   * bytes.
   * 
   * @return true or false.
   */
  public boolean isUsingDebugMessages();

  /**
   * If isUsingRawBytes() return false, then this method is called to get the
   * formatting for a debug message.
   * 
   * @return true if message was written out, false if it was filtered.
   */
  public boolean formatDebugMessage(IDebugChannelExportOutput<T> out,
                                    String originator,
                                    DebugMessage dm,
                                    EventType type) throws IOException;

  /**
   * If isUsingRawBytes() return true, then this method is called to get the
   * formatting for the raw bytes.
   * 
   * @return true if message was written out, false if it was filtered.
   */
  public boolean
         formatRawBytes(IDebugChannelExportOutput<T> out, byte[] rawBytes, int offset, int length) throws IOException;

  /**
   * If this format is NOT using debug messages, then this method is called for
   * each block of raw bytes received.
   * 
   * @param out
   * @param rawBytes
   * @param offset
   * @param length
   * @throws IOException
   */
  public void writeRawUnframedData(IDebugChannelExportOutput<T> out,
                                   byte[] rawBytes,
                                   int offset,
                                   int length) throws IOException;
}
