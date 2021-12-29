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

/**
 * Raw binary dump of data.
 * 
 * @author timotej
 *
 */
public class DumpFileFormat implements IPtiFileFormat {

  @Override
  public String header() {
    return null;
  }

  @Override
  public String description() {
    return "Binary dump of raw bytes, no deframing.";
  }

  @Override
  public boolean isUsingRawBytes() {
    return true;
  }
  
  @Override
  public boolean isUsingDebugMessages() {
    return false;
  }
  
  @Override
  public String formatDebugMessage(String originator, DebugMessage dm, EventType type) {
    return null;
  }
  
  @Override
  public String formatRawBytes(byte[] rawBytes, int offset, int length) {
    return null;
  }
}
