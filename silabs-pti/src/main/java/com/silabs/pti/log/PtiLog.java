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
 * This class provides logging through eclipse loggers. All workbench code
 * should use it to log stuff.
 *
 * Created on Jul 26, 2005
 *
 * @author Timotej (timotej@ember.com)
 * @since 4.6
 */

public class PtiLog {

  private static IPtiLogger logger = new DefaultLogger();

  // Don't instantiate
  private PtiLog() {
  }

  public static void setLogger(final IPtiLogger loggerInstance) {
    logger = loggerInstance;
  }

  /** Log simple informational message. */
  public static void info(final String message) {
    info(message, null);
  }

  /** Log simple informational message with exception */
  public static void info(final String message, final Throwable throwable) {
    logger.log(PtiSeverity.INFO, message, throwable);
  }

  /** Log error */
  public static void error(final String message) {
    error(message, null);
  }

  /**
   * Log error
   *
   * @since 5.0
   */
  public static void error(final Throwable throwable) {
    error(null, throwable);
  }

  /** Log error with exception */
  public static void error(final String message, final Throwable throwable) {
    logger.log(PtiSeverity.ERROR, message, throwable);
  }

  /** Log warning */
  public static void warning(final String message) {
    warning(message, null);
  }

  /** Log warning with exception */
  public static void warning(final String message, final Throwable throwable) {
    logger.log(PtiSeverity.WARNING, message, throwable);
  }

  /**
   * Log message with full information. Calling this is identical to calling
   * separate info(), error() and warning() methods.
   */
  public static void message(final PtiSeverity severity, final String message, final Throwable t) {
    switch (severity) {
    case ERROR:
    case INFO:
    case WARNING:
      logger.log(severity, message, t);
      break;
    case NONE:
    default:
      break;
    }
  }

}
