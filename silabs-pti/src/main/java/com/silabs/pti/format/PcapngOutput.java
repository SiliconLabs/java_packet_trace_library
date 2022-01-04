/*******************************************************************************
 * # License
 * Copyright 2022 Silicon Laboratories Inc. www.silabs.com
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

import com.silabs.na.pcap.IPcapOutput;
import com.silabs.na.pcap.Pcap;

/**
 * Output implementation using a PCAP NG format from the pcap library.
 * 
 * @author timotej
 *
 */
public class PcapngOutput implements IDebugChannelExportOutput<IPcapOutput> {

  private final IPcapOutput pcapOutput;

  PcapngOutput(final File f) throws IOException {
    pcapOutput = Pcap.openForWriting(f);
  }

  @Override
  public void close() throws IOException {
    pcapOutput.close();
  }

  @Override
  public IPcapOutput writer() {
    return pcapOutput;
  }
}
