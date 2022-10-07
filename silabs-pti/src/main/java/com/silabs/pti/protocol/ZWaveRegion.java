// Copyright 2016 Silicon Laboratories, Inc.

package com.silabs.pti.protocol;

import com.silabs.pti.decode.ISparseFieldEnum;

/**
 * ZWave regions.
 *
 * Created on Jun 10, 2020
 * 
 * @author timotej
 */
public enum ZWaveRegion implements ISparseFieldEnum {
  INVALID(0x00, "Default Invalid", new int[] { 100000, 40000, 9600 }),
  EU(0x01, "European Union", new int[] { 100000, 40000, 9600 }),
  US(0x02, "United States", new int[] { 100000, 40000, 9600 }),
  ANZ(0x03, "Australia/New Zealand", new int[] { 100000, 40000, 9600 }),
  HK(0x04, "Hong Kong", new int[] { 100000, 40000, 9600 }),
  MY(0x05, "Malaysia", new int[] { 100000, 40000, 9600 }),
  IN(0x06, "India", new int[] { 100000, 40000, 9600 }),
  JP(0x07, "Japan", new int[] { 100000, 100000, 100000 }),
  RU(0x08, "Russia", new int[] { 100000, 40000, 9600 }),
  IL(0x09, "Israel", new int[] { 100000, 40000, 9600 }),
  KR(0x0A, "Korea", new int[] { 100000, 100000, 100000 }),
  CN(0x0B, "China", new int[] { 100000, 40000, 9600 }),
  US_LR1(0x0C, "United States, first long range PHY", new int[] { 100000, 40000, 9600, 100000 }),
  US_LR2(0x0D, "United States, second long range PHY", new int[] { 100000, 40000, 9600, 100000 }),
  US_LR_END_DEVICE(0x0E, "United States, long range end device PHY", new int[] { 100000, 100000 });

  private int id;
  private String title;
  private int[] channelToBaudRate;

  private ZWaveRegion(final int id, final String title, final int[] channelToBaudRate) {
    this.id = id;
    this.title = title;
    this.channelToBaudRate = channelToBaudRate;
  }

  @Override
  public int id() {
    return id;
  }

  @Override
  public String title() {
    return title;
  }

  // Takes a channel, gets the baud rate from it and then
  // 9.6kbps or 40kbps is a 1 byte CRC while any 100kbps channel is 2 bytes
  public int crcLengthFromChannel(final int channel) {
    if (channel >= 0 && channel < channelToBaudRate.length) {
      int baudRate = channelToBaudRate[channel];
      switch (baudRate) {
      case 100000:
        return 2;
      default:
        return 1;
      }
    } else {
      return 1;
    }
  }

  public static int calculateZWaveCrcLength(final byte[] payload) {
    if (payload.length < 4)
      return 1; // Something is wrong, let's not bomb out....
    int region = (payload[payload.length - 4] & 0x0F);
    int channel = (payload[payload.length - 3] & 0x3F);
    return crcLengthFromRegionNumberAndChannelNumber(region, channel);
  }

  /**
   * This method figures out the length of the CRC. It needs 2 informations for
   * this: a.) The region. b.) The channel.
   *
   *
   * @param region  Region for ZWave
   * @param channel Channel for ZWave
   * @return int
   */
  public static int crcLengthFromRegionNumberAndChannelNumber(final int region, final int channel) {
    for (ZWaveRegion r : ZWaveRegion.values()) {
      if (r.id == region)
        return r.crcLengthFromChannel(channel);
    }
    return 1;
  }
}
