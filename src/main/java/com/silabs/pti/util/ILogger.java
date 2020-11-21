// Copyright 2016 Silicon Laboratories, Inc.

package com.silabs.pti.util;

/**
 * Class that can log messages to error log.
 *
 * Created on Feb 13, 2017
 * @author timotej
 * @since 4.6
 */
public interface ILogger {
  /**
   * Logs a message with given severity and optional throwable.
   *
   * @returns void
   */
  public void log(final Severity severity, final String message, final Throwable throwable);

}
