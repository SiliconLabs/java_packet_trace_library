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
import com.silabs.na.pcap.LinkType;
import com.silabs.na.pcap.Pcap;
import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.EventType;

/**
 * Format for PCAPNG writer.
 * 
 * @author timotej
 *
 */
public class PcapngFormat implements IDebugChannelExportFormat<IPcapOutput> {

  /**
   * Common method that actually writes out the raw unframed bytes into the pcap
   * stream.
   * 
   * @param pcapOut
   * @param interfaceIndex
   * @param pcTime
   * @param rawBytes
   * @throws IOException
   */
  public static final void writeRawUnframedDebugMessage(final IPcapOutput pcapOut,
                                                        final int interfaceIndex,
                                                        final long pcTime,
                                                        final byte[] rawBytes) throws IOException {
    pcapOut.writeEnhancedPacketBlock(interfaceIndex, pcTime, rawBytes);
  }

  /**
   * Common method that writes out an initial interface description block.
   * 
   * @param pcapOut
   * @throws IOException
   */
  public static final void writeInterfaceDescriptionBlock(final IPcapOutput pcapOut) throws IOException {
    pcapOut.writeInterfaceDescriptionBlock(LinkType.USER12, Pcap.RESOLUTION_MICROSECONDS);
  }

  @Override
  public IDebugChannelExportOutput<IPcapOutput> createOutput(final File f, final boolean append) throws IOException {
    if (append)
      throw new IOException("Appending to pcap files not supported.");
    return new PcapngOutput(f);
  }

  @Override
  public IDebugChannelExportOutput<IPcapOutput> createStdoutOutput() throws IOException {
    return null;
  }

  @Override
  public String description() {
    return "PCAPNG forma as used by Wireshark and many other utilities.";
  }

  @Override
  public void writeHeader(final IDebugChannelExportOutput<IPcapOutput> out) throws IOException {
    writeInterfaceDescriptionBlock(out.writer());
  }

  @Override
  public boolean formatDebugMessage(final IDebugChannelExportOutput<IPcapOutput> out,
                                    final String originator,
                                    final DebugMessage dm,
                                    final EventType type) throws IOException {
    out.writer().writeEnhancedPacketBlock(0, dm.networkTime(), dm.contents());
    return true;
  }

  @Override
  public boolean formatRawBytes(final IDebugChannelExportOutput<IPcapOutput> out,
                                final long pcTimeMs,
                                final byte[] rawBytes,
                                final int offset,
                                final int length) throws IOException {
    writeRawUnframedDebugMessage(out.writer(), 0, pcTimeMs, rawBytes);
    return true;
  }

  @Override
  public void writeRawUnframedData(final IDebugChannelExportOutput<IPcapOutput> out,
                                   final byte[] rawBytes,
                                   final int offset,
                                   final int length) throws IOException {
    throw new IOException("PCAP NG does not support unframed writing.");
  }

  @Override
  public boolean isUsingDebugMessages() {
    return true;
  }

  @Override
  public boolean isUsingRawBytes() {
    return true;
  }
}
