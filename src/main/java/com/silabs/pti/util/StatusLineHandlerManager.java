// Copyright 2016 Silicon Laboratories, Inc.

package com.silabs.pti.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Class of this type deals with handling status line handlers.
 *
 * Created on Feb 13, 2017
 * @author timotej
 * @since 4.6
 */
public class StatusLineHandlerManager {

  private final List<IStatusLineHandler> statusLines = new ArrayList<>();

  /**
   * Clears the status line.
   *
   * @returns void
   */
  public void clearErrorMessage() {
    for ( IStatusLineHandler slm: statusLines ) {
      slm.setErrorMessage(null);
    }
  }

  /**
   * Sets the status line.
   *
   * @returns void
   */
  public void statusLine(final String message, final boolean isError) {
    for ( IStatusLineHandler slm: statusLines ) {
      if ( isError ) {
        slm.setErrorMessage(message);
      } else {
        slm.setErrorMessage(null);
        slm.setMessage(message);
      }
    }
  }
  /**
   * This is useful for hooking up a status line manager.
   * It is intended for internal use only.
   */
  public void addStatusLineManager(final IStatusLineHandler slm) {
    if ( !statusLines.contains(slm) )
      statusLines.add(slm);
  }

  /**
   * This is useful for hooking up a status line manager.
   * It is intended for internal use only.
   */
  public void removeStatusLineManager(final IStatusLineHandler slm) {
    statusLines.remove(slm);
  }

}
