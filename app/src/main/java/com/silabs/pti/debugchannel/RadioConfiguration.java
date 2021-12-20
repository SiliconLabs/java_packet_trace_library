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

/**
 * Radio configuration is used in determining event start/end times.
 *
 * Created on Dec 12, 2017
 * @author Timotej Ecimovic
 */
public enum RadioConfiguration {

  FIFTEENFOUR(32, 8, 250, "IEEE 802.15.4"),
  BLE1(8, 32, 1000, "Bluetooth Low Energy 1"),
  BLE2(8, 32, 1000, "Bluetooth Low Energy 2");

  private final int preambleBits;
  private final int syncWordBits;
  private final int dataRateInKiloBitsPerSecond;
  private final String description;

  private RadioConfiguration(final int preambleBits,
                             final int syncWordBits,
                             final int dataRateInKiloBitsPerSecond,
                             final String description) {
    this.preambleBits = preambleBits;
    this.syncWordBits = syncWordBits;
    this.dataRateInKiloBitsPerSecond = dataRateInKiloBitsPerSecond;
    this.description = description;
  }

  /**
   * Returns description of the radio configuration.
   */
  public String description() { return description; }

  /** How many bits does the preamble have? */
  private int preambleBits() { return preambleBits; }

  /** How many bits does the sync word have? */
  private int syncWordBits() { return syncWordBits; }

  /** What is the data rate (in kiloBitsPerSecond) */
  private int dataRateInKiloBitsPerSecond() { return dataRateInKiloBitsPerSecond; }

  /**
   * With a given length of bytes, calculate microsecond duration for this
   * packet.
   *
   * @param lengthInBytes
   * @return number of microseconds that is a duration of this event.
   */
  public int microsecondDuration(final int lengthInBytes) {
    return (1000 * (lengthInBytes * 8 + syncWordBits() + preambleBits())) / dataRateInKiloBitsPerSecond();
  }

  /**
   * Calculate microsecond duration of sync and preamble packet.
   *
   * @return number of microseconds that is a duration.
   */
  public int syncAndPreableDuration() {
    return microsecondDuration(0);
  }

  /**
   * With a given length of bytes, calculate microsecond duration for this
   * packet without taking sync and preamble into consideration.
   *
   * @param lengthInBytes
   * @return number of microseconds that is a duration of this event.
   */
  public int microsecondDurationWithNoSyncAndPreamble(final int lengthInBytes) {
    return (1000 * (lengthInBytes * 8 )) / dataRateInKiloBitsPerSecond();
  }

  /**
   * Returns a preferential radio config, or default if invalid.
   *
   * @param preferenceValue
   * @return
   */
  public static RadioConfiguration valueFromPreference(final String preferenceValue) {
    for ( RadioConfiguration rc: values() ) {
      if ( rc.name().equals(preferenceValue) )
        return rc;
    }
    return FIFTEENFOUR;
  }

}
