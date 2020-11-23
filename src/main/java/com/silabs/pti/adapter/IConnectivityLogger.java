// Copyright (c) 2018 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

import com.silabs.pti.log.IPtiLogger;

/**
 * Interface that describes a logger for connectivity.
 *
 * @author Timotej
 * Created on Mar 28, 2018
 */
public interface IConnectivityLogger extends IPtiLogger {

  /**
   * What is the minimum period in milliseconds between the consecutive
   * prints of the data rata. 0 disables it.
   *
   * @return milliseconds
   */
  public int bpsRecordPeriodMs();

  /**
   * Returns true if this logger is enabled. This is a speeed enhancement.
   * @return
   */
  public boolean isEnabled();
}
