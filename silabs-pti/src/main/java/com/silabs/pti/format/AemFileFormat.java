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

import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.EventType;
import com.silabs.pti.decode.AemDecoder;
import com.silabs.pti.decode.AemSample;

/**
 * AEM file format: triplet of numbers, time/current/voltage.
 * 
 * @author timotej
 */
public class AemFileFormat implements IPtiFileFormat {

  @Override
  public String header() {
    return "#     Time    Voltage    Current";
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
  public String formatDebugMessage(String originator, DebugMessage dm, EventType type) {
    if (!type.isAem())
      return null;

    long microSecondTime = dm.networkTime();
    byte[] contents = dm.contents();
    AemDecoder ad = new AemDecoder(microSecondTime, contents);
    AemSample as;
    StringBuilder sb = new StringBuilder();
    String sep = "";
    while ((as = ad.nextSample()) != null) {
      sb.append(String.format("%s%10d %10f %10f", sep, as.timestamp(), as.voltage(), as.current()));
      sep = "\n";
    }
    return sb.toString();
  }
  
  @Override
  public String formatRawBytes(byte[] rawBytes, int offset, int length) {
    return null;
  }
}
