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
  public String header() {
    return null;
  }

  @Override
  public String description() {
    // TODO Auto-generated method stub
    return "Text file format that can be used with wireshark by running through 'text2pcap -q -t %H:%M:%S. <FILENAME>'";
  }
  
  @Override
  public boolean isUsingRawBytes() {
    return false;
  }
  
  @Override
  public String formatDebugMessage(String originator, DebugMessage dm, EventType type) {
    if (!type.isPacket())
      return null;

    // Text2pcap
    long timeMs = dm.networkTime();
    byte[] contents = dm.contents();
    int[] drops = WiresharkUtil.dropBytesFromBeginningEnd(type);
    if (drops[0] != 0 || drops[1] != 0) {
      if (drops[0] + drops[1] >= contents.length)
        return null; // Nothing we can do. There is no data left.
      contents = Arrays.copyOfRange(contents, drops[0], contents.length - drops[1]);
    }
    return WiresharkUtil.printText2Pcap(timeMs, contents);
  }
  
  @Override
  public String formatRawBytes(byte[] rawBytes, int offset, int length) {
    return null;
  }
}
