// Copyright 2016 Silicon Laboratories, Inc.

package com.silabs.pti.log;

/**
 * Class that can log messages to error log.
 *
 * Created on Feb 13, 2017
 * @author timotej
 * @since 4.6
 */
public interface IPtiLogger {
  /**
   * Logs a message with given severity and optional throwable.
   */
  public void log(final PtiSeverity severity, final String message, final Throwable throwable);

}
