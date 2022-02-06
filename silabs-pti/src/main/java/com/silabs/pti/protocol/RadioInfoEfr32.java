// Copyright (c) 2022 Silicon Labs. All rights reserved.

package com.silabs.pti.protocol;

import com.silabs.pti.debugchannel.EventType;
import com.silabs.pti.util.MiscUtil;

/**
 * Various utilities for the radio info protocol of efr32 parts.
 *
 * @author timotej
 * Created on Feb 5, 2022
 */
public class RadioInfoEfr32 {

  private RadioInfoEfr32() {
  }

  /**
   * Determines the length of the radio info block.
   *
   * @param type Event type.
   * @param payload Payload
   * @param hintBluetooth If you externally know this event is a BLE event, set this to true to deal with "UNKNOWN" protocol cases
   * @return length
   */
  public static int determineRadioInfoLength(final EventType type, final byte[] payload, final boolean hintBluetooth) {
    if (payload == null || payload.length < 2)
      return 0;
    byte endByte = payload[payload.length - 1];
    byte penultimateByte = payload[payload.length - 2];
    int len = lengthFromLastByte(endByte);
    boolean isMissingPtiProtocol = isMissingPtiProtocol(endByte,
                                                        penultimateByte);

    if (isMissingPtiProtocol) {
      int length;
      if ( hintBluetooth ) {
        length = Protocol.crcLen(Protocol.BLE, payload) + 1;
      } else {
        length = 3;
      }
      if (type.isRx())
        length++; // RSSI
      return length;
    } else {
      int junkBytesLength = 0;
      Protocol p = protocolFromPenultimateByte(penultimateByte);
      if (p.isUndetermined() && hintBluetooth) {
        p = Protocol.BLE;
      }
      if (p == Protocol.BLE && type.isRx() && doesRxHaveRadioCfg(len)) {
        junkBytesLength = junkBytesLength(payload);
      }
      return Protocol.crcLen(p, payload) + 4 + junkBytesLength + len;
    }
  }

  /**
   * Given the length of the radio info block, decide if it contains radio
   * config byte.
   *
   * @param len
   * @return true or false.
   */
  public static boolean doesRxHaveRadioCfg(final int len) {
    switch (len) {
    case 1:
    case 5:
      return false;
    case 2:
    case 6:
      return true;
    default:
      return false; // Really an error case.
    }
  }

  /**
   * Given the payload of the data, this method retrieves the protocol.
   *
   * @param payload
   * @return protocol or null.
   */
  public static Protocol determineProtocol(final byte[] payload) {
    if (payload == null || payload.length < 2)
      return null;

    byte penultimate = payload[payload.length - 2];
    if (!isMissingPtiProtocol(payload[payload.length - 1], penultimate)) {
      return protocolFromPenultimateByte(penultimate);
    }
    return null;
  }

  /**
   * In case of BLE, the protocol contains few junk bytes. These are determining
   * the length of those.
   *
   * @param payload
   * @return length of junk bytes
   */
  public static int junkBytesLength(final byte[] payload) {
    int index = payload.length - 4;
    if (index >= 0) {
      byte radioConfig = payload[index];
      return bleLengthFromRadioConfigByte(radioConfig);
    } else {
      return 0;
    }
  }

  /**
   * In case of BLE protocol, this extracts the length.
   *
   * @param value
   * @return length
   */
  public static int bleLengthFromRadioConfigByte(final byte value) {
    return (MiscUtil.unsignedByteToInt(value) & 0x000000F8) >> 3;
  }

  /**
   * Extracts the version from the last byte of the radio info payload.
   *
   * @param value
   * @return version
   */
  public static int versionFromLastByte(final byte value) {
    return (MiscUtil.unsignedByteToInt(value) & 0x00000007);
  }

  /**
   * Extracts the length from the last byte of the radio info payload.
   *
   * @param value
   * @return length
   */
  public static int lengthFromLastByte(final byte value) {
    return (MiscUtil.unsignedByteToInt(value) & 0x00000038) >> 3;
  }

  /**
   * If this is true, then radio info protocol could not be properly detected.
   * This apparently happens if something goes wrong with sequencer.
   *
   * Physically, this detects if one of the two last bytes has an F as a first
   * nibble.
   */
  public static boolean isMissingPtiProtocol(final byte lastByte,
                                       final byte penultimateByte) {
    int nibble1 = (MiscUtil.unsignedByteToInt(lastByte) >> 4);
    int nibble2 = (MiscUtil.unsignedByteToInt(penultimateByte) >> 4);

    return (nibble1 == 0x0000000F || nibble2 == 0x0000000F);
  }

  public static Protocol protocolFromPenultimateByte(final byte value) {
    int n = (MiscUtil.unsignedByteToInt(value) & 0x0000000F);
    return Protocol.resolve(n);
  }

  /**
   * Returns channel from the payload.
   * @param payload
   * @return channel or -1 if error.
   */
  public static int channel(final byte[] payload) {
    if (payload.length < 2)
      return -1;
    if (RadioInfoEfr32.isMissingPtiProtocol(payload[payload.length - 1],
                             payload[payload.length - 2])) {
      return -1;
    } else {
      return payload[payload.length - 3] & 0x0000003F;
    }
  }

}
