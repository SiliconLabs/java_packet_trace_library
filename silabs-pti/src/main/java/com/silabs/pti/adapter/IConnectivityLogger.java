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

import com.silabs.pti.log.IPtiLogger;

/**
 * Interface that describes a logger for connectivity.
 *
 * @author Timotej Created on Mar 28, 2018
 */
public interface IConnectivityLogger extends IPtiLogger {

  /**
   * What is the minimum period in milliseconds between the consecutive prints of
   * the data rata. 0 disables it.
   *
   * @return milliseconds
   */
  public int bpsRecordPeriodMs();

  /**
   * Returns true if this logger is enabled. This is a speeed enhancement.
   * 
   * @return
   */
  public boolean isEnabled();
}
