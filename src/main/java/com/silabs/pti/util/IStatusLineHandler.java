// Copyright 2016 Silicon Laboratories, Inc.

package com.silabs.pti.util;

/**
 * Object of this type can manage a status line.
 *
 * Created on Feb 13, 2017
 * @author timotej
 * @since 4.6
 */
public interface IStatusLineHandler {

  /**
   * Shows the error message, typically in red.
   *
   * @returns void
   */
  public void setErrorMessage(String errorMessage);

  /**
   * Shows normal message, typically in normal fonts.
   *
   * @returns void
   */
  public void setMessage(String message);

  /**
   * Clears all messages.
   *
   * @returns void
   */
  public void clearMessage();
}
