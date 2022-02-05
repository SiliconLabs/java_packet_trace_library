// Copyright (c) 2016 Silicon Labs. All rights reserved.

package com.silabs.pti.protocol;

import com.silabs.pti.StackTypeId;
import com.silabs.pti.decode.ISparseFieldEnum;

/**
 * These are protocols as defined by the EFR32 PTI.
 *
 * Created on Dec 12, 2016
 * @author timotej
 */
public enum Protocol implements ISparseFieldEnum {
  UNKNOWN           ("Unknown",         -1,  0,  null),
  CUSTOM            ("Custom",           0,  2,  null),
  EMBER_PHY         ("EFR32 EmberPHY",   1,  2,  null),
  THREAD_ON_RAIL    ("Thread on RAIL",   2,  2,  StackTypeId.IP_ID),
  BLE               ("BLE",              3,  3,  StackTypeId.BLE_ID),
  CONNECT_ON_RAIL   ("Connect on RAIL",  4,  2,  StackTypeId.CONNECT_ID),
  ZIGBEE_ON_RAIL    ("ZigBee on RAIL",   5,  2,  StackTypeId.ZIGBEE_ID),
  ZWAVE_ON_RAIL     ("Z-Wave on RAIL",   6,  -1, StackTypeId.ZWAVE_ID),
  WISUN             ("Wi-SUN",           7,  4,  StackTypeId.WISUN_ID);

  private final int crcLen;
  private final int id;
  private final String title, stackId;


  Protocol(final String title,
           final int id,
           final int crcLen, // -1 means that this is dynamically calculated.
           final String stackId) {
    this.crcLen = crcLen;
    this.id = id;
    this.title = title;
    this.stackId = stackId;
  }

  public int crcLen() { return crcLen; }

  public boolean isCrcLengthDynamicallyCalculated() {
    return crcLen == -1;
  }

  @Override
  public int id() { return id; }

  @Override
  public String title() { return title; }

  public String stackId() { return stackId; }

  public boolean isUndetermined() {
    return this == UNKNOWN || this == CUSTOM;
  }

  public static int crcLen(final Protocol p, final byte[] payload) {
    if (p.isCrcLengthDynamicallyCalculated()) {
      switch (p) {
      case ZWAVE_ON_RAIL:
        return ZWaveRegion.calculateZWaveCrcLength(payload);
      default:
        return p.crcLen();
      }
    } else {
      return p.crcLen();
    }
  }

  /**
   * Resolve method that is faster than a for loop. Maintain it please.
   *
   *
   * @param id The ID of a protocol from the PTI radio info.
   * @return Protocol
   */
  public static Protocol resolve(final int id) {
    switch(id) {
    case 0: return CUSTOM;
    case 1: return EMBER_PHY;
    case 2: return THREAD_ON_RAIL;
    case 3: return BLE;
    case 4: return CONNECT_ON_RAIL;
    case 5: return ZIGBEE_ON_RAIL;
    case 6: return ZWAVE_ON_RAIL;
    case 7: return WISUN;
    default: return UNKNOWN;
    }
  }
}
