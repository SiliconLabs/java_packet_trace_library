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
import com.silabs.pti.protocol.RadioInfoEfr32;

/**
 * Format for PCAPNG writer.
 *
 * @author timotej
 *
 */
public class PcapngFormat implements IDebugChannelExportFormat<IPcapOutput> {

  public static enum Mode {
    DCH(LinkType.USER12,
        "PCAPNG format: capturing whole debug channel as custom linktype 'user12'. All channel data is captured.",
        true),
    ZIGBEE(LinkType.IEEE802_15_4_NOFCS,
          "PCAPNG format for Zigbee: using 802.15.4 no-FCS link type. Only 15.4 packets are captured.",
          false),
    MATTER(LinkType.IEEE802_15_4_NOFCS,
          "PCAPNG format for Matter: using 802.15.4 no-FCS link type. Only 15.4 packets are captured.",
          false),
    BLUETOOTH(LinkType.BLUETOOTH_LE_LL,
           "PCAPNG format for Bluetooth: using Bluetooth LE_LL link type. Only Bluetooth packets are captured.",
           false),
    WISUN(LinkType.IEEE802_15_4_NOFCS,
          "PCAPNG format for Wi-SUN: using 802.15.4 no-FCS link type. Only 15.4 packets are captured.",
          false);

    private LinkType linkType;
    private String description;
    private boolean usesRawBytes;
    Mode(final LinkType linkType, final String description, final boolean usesRawBytes) {
      this.linkType = linkType;
      this.description = description;
      this.usesRawBytes = usesRawBytes;
    }

    public LinkType linkType() { return linkType; }
    public String description() { return description; }
    public boolean usesRawBytes() { return usesRawBytes; }
  }

  private final Mode mode;

  public PcapngFormat(final Mode mode) {
    this.mode = mode;
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
    return mode.description();
  }

  @Override
  public void writeHeader(final IDebugChannelExportOutput<IPcapOutput> out) throws IOException {
    out.writer().writeInterfaceDescriptionBlock(mode.linkType(), Pcap.RESOLUTION_MICROSECONDS);
  }

  @Override
  public boolean formatDebugMessage(final IDebugChannelExportOutput<IPcapOutput> out,
                                    final String originator,
                                    final DebugMessage dm,
                                    final EventType type) throws IOException {
    // We end here in the case where mode is not using raw bytes.
    byte[] content;
    long time;
    switch(mode) {
    case WISUN:
    case BLUETOOTH:
    case MATTER:
    case ZIGBEE:
      // For WISUN mode, we ignore non-packets.
      if ( !type.isPacket() ) return false;
      byte[] buff = dm.contents();
      int startOffset = 0;
      int endOffset = buff.length - 2;
      if ( type.isFromEfr() ) {
        // For Efr, we know how to extract the payload.

        // Adjust start offset.
        if (buff[startOffset] == (byte) 0xF8 || buff[startOffset] == (byte) 0xFC) {
          // omit leading encapsulation byte
          startOffset++;
        }

        // Adjust endOffset
        endOffset -= RadioInfoEfr32.determineRadioInfoLength(type, buff, mode == Mode.BLUETOOTH);

        int len = endOffset - startOffset;
        if ( len < 0 ) return false;

        content = new byte[len];
        System.arraycopy(buff, startOffset, content, 0, len);
      } else {
        content = buff;
      }
      time = dm.networkTime();
      break;
    default:
      content = dm.contents();
      time = dm.networkTime();
      break;
    }
    out.writer().writeEnhancedPacketBlock(0, time, content);
    return true;
  }

  @Override
  public boolean formatRawBytes(final IDebugChannelExportOutput<IPcapOutput> out,
                                final long pcTimeMs,
                                final byte[] rawBytes,
                                final int offset,
                                final int length) throws IOException {
    // We end here in the raw bytes case (mode == DCH)
    out.writer().writeEnhancedPacketBlock(0, pcTimeMs, rawBytes);
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
    return mode.usesRawBytes();
  }
}
