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

package com.silabs.pti.debugchannel;

import com.silabs.pti.util.MiscUtil;

/**
 * A parsed Ember node debug message.  Contains fields for the name
 * of the host that generated the message, the type of the debug
 * message, the timestamp of the message, and the actual contents
 * of the message, if any, as a byte array.
 *
 * @author  Matteo Neale Paris (matteo@ember.com)
 */
public class DebugMessage {

  /** Enum for debug versioning */
  public static enum Version {
    UNKNOWN(-1,-1, -1),
    V1(1, 1, 1),
    V2(2, 1, 2),
    V3(3, 2, 2);

    private final int id, sequenceLength, sizeLength;
    Version(final int id, final int sequenceLengthInBytes, final int sizeLength) {
      this.id = id;
      this.sequenceLength = sequenceLengthInBytes;
      this.sizeLength = sizeLength;
    }

    public int id() { return id; }
    public int sequenceLength() { return sequenceLength; }
    public int sizeLength() { return sizeLength; }

    public static Version resolve(final int id) {
      switch(id) {
      case 1: return V1;
      case 2: return V2;
      case 3: return V3;
      default: return UNKNOWN;
      }
    }
  }

  private String originatorId;
  private long pcTime;       // timestamp when received by the PC
  private Version version;
  private long networkTime;  // backchannel generated network timestamp
  private int debugType;
  private int seqNumber = -1; // if -1, then V1 with no sequence numbers.
  private int flags = 0;
  private byte[] contents;

  private DebugMessage() {
  }

  public DebugMessage(final long networkTime,
                      final int debugType,
                      final byte[] contents) {
    this.networkTime = networkTime;
    this.debugType = debugType;
    this.contents = contents;
  }

  /**
   * This is a convenience method to return a human readable value for
   * the various debug types.
   * @return a human readable String representing the debug type
   */
  public static String getTypeName(final int typeValue) {
    return DebugMessageType.get(typeValue).description();
  }

  /** Returns protocol version of this debug message */
  public Version version() { return version; }

  /** Returns the sequence number */
  public int seqNumber() { return seqNumber; }

  /** Returns the host. */
  public String originatorId() { return  originatorId; }

  /** Returns the bytearray contents. */
  public byte[] contents() { return contents; }

  /** Returns the length of the contents. */
  public int contentLength() { return contents.length; }

  /** Returns the debug type */
  public int debugType() { return debugType; }

  /** Returns network time */
  public long networkTime() { return networkTime; }

  /** Returns PC time when this event arrived to the pc */
  public long pcTime() { return pcTime; }

  /**
   * Returns the i-th byte
   *
   * @return byte
   */
  public byte contents(final int i) { return contents[i]; }

  /** Return debug message flags */
  public int flags() { return flags; }

  /**
   * Sets the network time for the debug message, after the time correction
   * was done.
   *
   * @param t
   */
  public void setNetworkTime(final long t) {
    this.networkTime = t;
  }

  //----------------------------------------------------------------------------

  /**
   * Constructs a DebugMessage object from the raw message bytes. Assumes that
   * the message has been processed by the backchannel board (so that it is in
   * the ethernet format rather than the serial format) and that the framing and
   * length bytes have been removed.
   *
   * Version 1.0 Message:
   *  0 timestamp low byte  \ LSB of timestamp = 1/1024 of a second
   *  1 timestamp med byte  |
   *  2 timestamp med byte  |
   *  3 timestamp high byte /
   *  4 Debug Message Type
   *  5 Debug Message Subtype (EM_DEBUG_MAC_RX)
   *  6 Link Quality
   *  ... Data dependant on the Message Type
   *
   * @param originatorId   the hostname of the node that generated the message.
   * @param raw    the raw bytes of the message.
   * @param pcTime the PC system time (in milliseconds) when the message was
   *               received.
   */
  public static DebugMessage makeVersion1(final String originatorId,
                                          final byte[] raw,
                                          final long pcTime){
    if (raw.length < 5)
      return null;

    DebugMessage debug = new DebugMessage();
    debug.originatorId = originatorId;
    debug.pcTime  = pcTime;
    debug.networkTime = bytesToLong(raw, 0, 4);
    debug.debugType = (raw[4] & 0xFF);
    debug.contents = new byte[raw.length - 5];
    if (debug.contents.length > 0)
      System.arraycopy(raw, 5, debug.contents, 0, debug.contents.length);

    return debug;
  }

  /**
   * Constructs a DebugMessage object from the raw message bytes. Assumes that
   * the message has been processed by the backchannel board (so that it is in
   * the ethernet format rather than the serial format) and that the framing and
   * length bytes have been removed.
   *
   * Version 2.0 Message:
   *  0 version number  \ LSB,  direction (0 = node to pc, 1 = pc to node)
   *  1 version number  / MSB
   *  2 timestamp byte 0  \ LSB, microsecond tics
   *  3 timestamp byte 1  |
   *  4 timestamp byte 2  |
   *  5 timestamp byte 3  |
   *  6 timestamp byte 4  |
   *  7 timestamp byte 5  / MSB
   *  8 Debug Message Type \ LSB (coresponds to single byte message type.
   *  9 Debug Message Type / MSB
   *  10 sequence number
   *  ... Data dependant on the Message Type
   *
   *
   *   * Version 3.0 Message:
   *  0 version number  \ LSB,  direction (0 = node to pc, 1 = pc to node)
   *  1 version number  / MSB
   *  2 timestamp byte 0  \ LSB, nanosecond tics
   *  3 timestamp byte 1  |
   *  4 timestamp byte 2  |
   *  5 timestamp byte 3  |
   *  6 timestamp byte 4  |
   *  7 timestamp byte 5  |
   *  8 timestamp byte 6  |
   *  9 timestamp byte 7  / MSB
   *  10 Debug Message Type \ LSB (corresponds to double byte message type.
   *  11 Debug Message Type / MSB
   *  12 Flags \ LSB
   *  13 Flags |
   *  14 Flags |
   *  15 Flags / MSB
   *  16 sequence number
   *  17 sequence number
   *  ... Data dependent on the Message Type
   *
   * @param originatorId   the hostname of the node that generated the message.
   * @param raw    the raw bytes of the message.
   * @param pcTime the PC system time (in milliseconds) when the message was
   *               received.
   */
  public static DebugMessage make(final String originatorId,
                                  final byte[] raw,
                                  final long pcTime){
    if (raw.length < 2)
      return null;

    DebugMessage debug = new DebugMessage();
    debug.originatorId = originatorId;
    debug.pcTime       = pcTime;
    debug.version      = Version.resolve((int)bytesToLong(raw, 0, 2));
    if ( debug.version == Version.V3 ) {
      if ( raw.length < 18 ) return null;
      debug.networkTime  = bytesToLong(raw, 2, 8)/1000;
      debug.debugType    = (int)bytesToLong(raw, 10, 2);
      debug.flags        = (int)bytesToLong(raw, 12, 4);
      debug.seqNumber    = (int)bytesToLong(raw, 16, 2);
      debug.contents     = new byte[raw.length - 18];
      if (debug.contents.length > 0)
        System.arraycopy(raw, 18, debug.contents, 0, debug.contents.length);
    } else {
      // We're assuming version 2, since in the code we occasionally parsed
      // random junk as version 2. Needs to be cleaned up.
      if ( raw.length < 11 ) return null;
      debug.networkTime  = bytesToLong(raw, 2, 6);
      debug.debugType    = (int)bytesToLong(raw, 8, 2);
      debug.seqNumber    = (raw[10] & 0xFF);
      debug.contents     = new byte[raw.length - 11];
      if (debug.contents.length > 0)
        System.arraycopy(raw, 11, debug.contents, 0, debug.contents.length);
    }

    return debug;
  }


  /**
   * Converts an array of bytes to a long. The LSB is raw[startIndex] and the
   * MSB is raw[startIndex + length - 1].
   */
  private static long bytesToLong(final byte[] raw, final int startIndex, final int length) {
    return MiscUtil.byteArrayToLong(raw, startIndex, length, false);
  }

  @Override
  public String toString() {
    return "[" + originatorId
           + " " + networkTime + " " + getTypeName(debugType)
           + "] [" + MiscUtil.formatByteArray(contents, true) + "]";
  }

}
