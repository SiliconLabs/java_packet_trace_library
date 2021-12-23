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

package com.silabs.pti.adapter;

import com.silabs.pti.log.PtiSeverity;

/**
 * Simple class that can calculate statistics for connectivity.
 *
 * @author Timotej Created on Mar 28, 2018
 */
public class ConnectivityStats {

  private long lastPrintTime = 0;
  private long startTime = Long.MIN_VALUE;
  private long totalBytes = 0;
  private long totalCountedBytes = 0;
  private int bitPerSecLifetime;

  private final IConnectivityLogger logger;
  private final String prefix;

  /**
   * Creates stats. If logger is non-null, it will print out the stats.
   *
   * @param logger
   */
  public ConnectivityStats(final IConnectivityLogger logger, final String prefix) {
    this.logger = logger;
    this.prefix = prefix;
  }

  /**
   * Records the data and prints out the data.
   * 
   * @param t
   * @param count
   */
  public void recordData(final long t, final int count) {
    totalBytes += count;
    if (startTime == Long.MIN_VALUE) {
      startTime = t;
    } else {
      totalCountedBytes += count;
      int msElapsed = (int) (t - startTime);
      if (logger != null && logger.bpsRecordPeriodMs() > 0 && t - lastPrintTime > logger.bpsRecordPeriodMs()
          && msElapsed > 0) {
        bitPerSecLifetime = (int) ((8000 * totalCountedBytes) / msElapsed);
        logger.log(PtiSeverity.INFO,
                   String.format("%s => %12d bytes / %8d ms = %10d kbit/s",
                                 prefix,
                                 totalBytes,
                                 msElapsed,
                                 bitPerSecLifetime / 1000),
                   null);
        lastPrintTime = t;
      }
    }
  }
}
