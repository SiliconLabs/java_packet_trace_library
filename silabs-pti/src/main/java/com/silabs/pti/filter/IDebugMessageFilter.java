// Copyright (c) 2022 Silicon Labs. All rights reserved.

package com.silabs.pti.filter;

import com.silabs.pti.debugchannel.DebugMessage;

/**
 * Interface describing a filter that filters debug messages.
 *
 * @author timotej
 * Created on Oct 4, 2022
 */
@FunctionalInterface
public interface IDebugMessageFilter {

  /**
   * Default all pass filter.
   */
  public static final IDebugMessageFilter ALL_PASS_FILTER = message -> true;

  /**
   * If the method returns true, then the message is kept in the queue.
   * If it returns false, then it is discarded.
   *
   * @param message Debug message under observation
   * @return true if message is kept, false if discarded.
   */
  public boolean isMessageKept(DebugMessage message);
}
