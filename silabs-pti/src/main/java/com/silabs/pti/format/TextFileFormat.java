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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.EventType;
import com.silabs.pti.util.WiresharkUtil;

/**
 * Wireshark text2pcap format.
 * 
 * @author timotej
 *
 */
public class TextFileFormat implements IPtiFileFormat {

  @Override
  public void writeHeader(final PrintStream printStream) {
  }

  @Override
  public String description() {
    return "Text file format that can be used with wireshark by running through 'text2pcap -q -t %H:%M:%S. <FILENAME>'";
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
  public boolean formatDebugMessage(final PrintStream printStream,
                                    final String originator,
                                    final DebugMessage dm,
                                    final EventType type) {
    if (!type.isPacket())
      return false;

    // Text2pcap
    final long timeMs = dm.networkTime();
    byte[] contents = dm.contents();
    final int[] drops = WiresharkUtil.dropBytesFromBeginningEnd(type);
    if (drops[0] != 0 || drops[1] != 0) {
      if (drops[0] + drops[1] >= contents.length)
        return false; // Nothing we can do. There is no data left.
      contents = Arrays.copyOfRange(contents, drops[0], contents.length - drops[1]);
    }
    final String x = WiresharkUtil.printText2Pcap(timeMs, contents);
    printStream.println(x);
    return true;
  }

  @Override
  public boolean
         formatRawBytes(final PrintStream printStream, final byte[] rawBytes, final int offset, final int length) {
    return false;
  }

  @Override
  public void writeRawUnframedData(final OutputStream out,
                                   final byte[] rawBytes,
                                   final int offset,
                                   final int length) throws IOException {
  }

}
