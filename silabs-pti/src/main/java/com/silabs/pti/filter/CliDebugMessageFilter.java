// Copyright (c) 2022 Silicon Labs. All rights reserved.

package com.silabs.pti.filter;

import java.text.ParseException;

import com.silabs.pti.debugchannel.DebugMessage;

/**
 * Filter implementation fed from the CLI argument.
 *
 * @author timotej
 * Created on Oct 5, 2022
 */
public class CliDebugMessageFilter implements IDebugMessageFilter {

  /**
   * Create the filter with the initial filter expression.
   *
   * @param argument
   * @throws ParseException
   */
  public CliDebugMessageFilter(final String expression) throws ParseException {
    throw new ParseException("Not yet implemented.", 0);
  }

  /**
   * Add another filter expression to the filter.
   *
   * @param argument
   * @throws ParseException
   */
  public void additionalFilter(final String expression) throws ParseException {
    throw new ParseException("Not yet implemented.", 0);
  }

  /**
   * If it returns true, the message is kept.
   */
  @Override
  public boolean isMessageKept(final DebugMessage message) {
    return true;
  }

}
