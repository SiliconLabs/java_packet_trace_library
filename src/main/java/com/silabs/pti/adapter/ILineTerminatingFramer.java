// Copyright (c) 2018 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

import com.silabs.pti.util.LineTerminator;

/**
 * Framer that is sensitive to line terminator characters.
 *
 * @author Timotej
 * Created on Mar 22, 2018
 */
public interface ILineTerminatingFramer extends IFramer {

  /**
   * Returns the current line terminator being used.
   *
   * @return
   */
  public LineTerminator lineTerminator();

  /**
   * Set the line terminator.
   *
   * @param lt
   */
  public void setLineTerminator(LineTerminator lt);
}
