// Copyright (c) 2016 Silicon Labs. All rights reserved.

package com.silabs.pti;

import java.util.Arrays;

import com.silabs.pti.adapter.TimeSynchronizer;
import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.DebugMessageType;
import com.silabs.pti.debugchannel.EventType;
import com.silabs.pti.debugchannel.PtiUtilities;
import com.silabs.pti.debugchannel.RadioConfiguration;
import com.silabs.pti.util.MiscUtil;
import com.silabs.pti.util.WiresharkUtil;

/**
 * Supported file formats.
 *
 * Created on Feb 15, 2017
 * @author timotej
 */
public enum FileFormat {
  DUMP("Binary dump of raw bytes, no deframing."),
  RAW("Raw bytes of deframed debug messages, one message per line."),
  LOG("Parsed debug messages, written into a file that Network Analyzer can import."),
  TEXT("Text file format that can be used with wireshark by running through 'text2pcap -q -t %H:%M:%S. <FILENAME>'");


  public static byte[] PCAP_DATA_PREFIX = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                            (byte)0x80, (byte)0x9A };

  public static String RAW_PREFIX = "[ ";
  public static String RAW_SUFFIX = " ]";

  private String description;

  private FileFormat(final String description) {
    this.description = description;
  }

  /**
   * returns formats as f1|f2|f3 to show in options.
   * @return
   */
  public static String formatsAsString() {
    StringBuilder formats = new StringBuilder();
    String sep = "";
    for ( FileFormat ff: FileFormat.values() ) {
      formats.append(sep).append(ff.name().toLowerCase());
      sep = "|";
    }
    return formats.toString();
  }


  public String description() { return description; }

  /**
   * Returns the header that goes to the beginning of the file.
   * @return
   */
  public String header() {
    switch(this) {
    case LOG:
      return PtiUtilities.ISD_LOG_HEADER;
    default:
      return null;
    }
  }
  /**
   * Takes a raw bytes and formats them.
   *
   * @return String
   */
  public String processDebugMsg(final long timeMs,
                                final String originator,
                                final byte[] bytes,
                                final TimeSynchronizer timeSync) {
    if ( RAW == this )
      return RAW_PREFIX + MiscUtil.formatByteArray(bytes) + RAW_SUFFIX;

    DebugMessage dm = DebugMessage.make("", bytes, timeMs);
    EventType type = EventType.fromDebugMessage(DebugMessageType.get(dm.debugType()));

    // time correction
    timeCorrection(timeSync, dm);
    return format(timeMs, originator, dm, type);
  }

  private void timeCorrection(final TimeSynchronizer timeSync, final DebugMessage message) {
    long actualTime = timeSync.synchronizedTime(message.originatorId(),
                                                message.networkTime());
    message.setNetworkTime(actualTime);

    // TODO: document time correction in trace via Summary event.
  }

  private String format(final long timeMs,
                        final String originator,
                        final DebugMessage dm,
                        final EventType type) {
    byte[] contents = dm.contents();
    if ( this == TEXT ) {
      // Text2pcap
      if ( type.isPacket() ) {
        int[] drops = WiresharkUtil.dropBytesFromBeginningEnd(type);
        if ( drops[0] != 0 || drops[1] != 0) {
          if ( drops[0] + drops[1] >= contents.length )
            return null; // Nothing we can do. There is no data left.
          contents = Arrays.copyOfRange(contents, drops[0], contents.length-drops[1]);
        }
        return WiresharkUtil.printText2Pcap(timeMs, contents);
      } else {
        return null;
      }
    } else {
      // Standard ISD log format
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
  }
}
