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

package com.silabs.pti.log;

/**
 * Class that can log messages to error log.
 *
 * Created on Feb 13, 2017
 * 
 * @author timotej
 * @since 4.6
 */
public interface IPtiLogger {
  /**
   * Logs a message with given severity and optional throwable.
   */
  public void log(final PtiSeverity severity, final String message, final Throwable throwable);

}
