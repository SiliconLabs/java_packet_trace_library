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

import com.silabs.pti.util.LineTerminator;

/**
 * Framer that is sensitive to line terminator characters.
 *
 * @author Timotej Created on Mar 22, 2018
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
