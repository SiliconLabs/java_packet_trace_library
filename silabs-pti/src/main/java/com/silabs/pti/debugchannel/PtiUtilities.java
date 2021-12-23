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
 * Random static utilities used for PTI.
 *
 * Created on Feb 15, 2017
 * 
 * @author timotej
 */
public class PtiUtilities {

  /**
   * Header for ISD text log files.
   */
  public final static String ISD_LOG_HEADER = "# (c) Ember - InSight Desktop";

  /**
   * This method returns the start and end times of the event as it should be used
   * in event creation.
   *
   * Note: We're doing an ugly thing here. WSTK timestamps at the beginning of the
   * packet, but ISA3 timestamps at the end of the packet. Given that EFR is only
   * supported on WSTK at the time of this writing (May 16, 2017), we are making
   * bold assumption that EFR packets have reversed endTime/startTime logic than
   * EMxxx ones. One day, this has to correctly be fixed PER ADAPTER TYPE, not per
   * event type.
   */
  public static long[]
         startEndTime(final DebugMessageType dmt, final DebugMessage debugMessage, final RadioConfiguration config) {
    long[] times = new long[2];
    long t = debugMessage.networkTime();
    switch (dmt) {
    case PACKET_TRACE:
    case PACKET_TRACE_EM2420_RX:
    case PACKET_TRACE_EM2420_TX:
    case PACKET_TRACE_EM2XX_RX:
    case PACKET_TRACE_EM2XX_TX:
    case PACKET_TRACE_EM3XX_RX:
    case PACKET_TRACE_EM3XX_TX:
      // This is the ISA3 logic
      times[0] = t - config.microsecondDuration(debugMessage.contentLength());
      times[1] = t;
      break;
    case PACKET_TRACE_EFR_RX:
      times[0] = t - config.microsecondDuration(0);
      times[1] = t + config.microsecondDurationWithNoSyncAndPreamble(debugMessage.contentLength());
      break;
    case PACKET_TRACE_EFR_TX:
      times[0] = t;
      times[1] = t + config.microsecondDuration(debugMessage.contentLength());
      break;
    default:
      times[0] = times[1] = t;
      break;
    }
    return times;
  }

  /**
   * 15.4 packets use this CRC scheme to stamp their packets.
   *
   * The bytes sent should NOT include the length byte, and should contain
   * everything UP TO, but not including the CRC bytes in the payload.
   *
   * @return int
   */
  public static int fifteenFourCrc(final byte[] bytes) {
    int crc = 0x0000; // initial value
    int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)

    for (byte b : bytes) {
      b = MiscUtil.reverseBits(b);
      for (int i = 0; i < 8; i++) {
        boolean bit = ((b >> (7 - i) & 1) == 1);
        boolean c15 = ((crc >> 15 & 1) == 1);
        crc <<= 1;
        if (c15 ^ bit)
          crc ^= polynomial;
      }
    }

    crc = (Integer.reverse(crc) >> 16) & 0xffff;
    return crc;
  }

}
