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
import com.silabs.pti.debugchannel.PtiUtilities;
import com.silabs.pti.debugchannel.RadioConfiguration;
import com.silabs.pti.util.MiscUtil;

/**
 * Output format in Silicon Labs Network analyzer text log format.
 * 
 * @author timotej
 *
 */
public class LogFileFormat implements IPtiFileFormat {

  @Override
  public String header() {
    return PtiUtilities.ISD_LOG_HEADER;
  }

  @Override
  public String description() {
    return "Parsed debug messages, written into a file that Network Analyzer can import.";
  }

  @Override
  public boolean isUsingRawBytes() {
    return false;
  }

  @Override
  public String formatDebugMessage(String originator, DebugMessage dm, EventType type) {
    byte[] contents = dm.contents();
    return "[" 
           + dm.networkTime() 
           + " " 
           + RadioConfiguration.FIFTEENFOUR.microsecondDuration(contents.length) 
           + " "
           + type.value() 
           + " " 
           + type.name() 
           + "] [" 
           + originator 
           + "] [" 
           + MiscUtil.formatByteArray(dm.contents()) 
           + "]";

  }

  @Override
  public String formatRawBytes(byte[] rawBytes, int offset, int length) {
    return null;
  }
}
