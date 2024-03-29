/*******************************************************************************
 * # License
 * Copyright 2022 Silicon Laboratories Inc. www.silabs.com
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

package com.silabs.pti.filter;

import com.silabs.pti.debugchannel.DebugMessage;

/**
 * Interface describing a filter that filters debug messages.
 *
 * @author timotej Created on Oct 4, 2022
 */
@FunctionalInterface
public interface IDebugMessageFilter {

  /**
   * Default all pass filter.
   */
  public static final IDebugMessageFilter ALL_PASS_FILTER = message -> true;

  /**
   * Default no pass filter.
   */
  public static final IDebugMessageFilter NO_PASS_FILTER = message -> false;

  /**
   * If the method returns true, then the message is kept in the queue. If it
   * returns false, then it is discarded.
   *
   * @param message Debug message under observation
   * @return true if message is kept, false if discarded.
   */
  public boolean isMessageKept(DebugMessage message);
}
