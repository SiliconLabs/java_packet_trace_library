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

  public CliDebugMessageFilter(final String argument) throws ParseException {
  }

  @Override
  public boolean isMessageKept(final DebugMessage message) {
    return false;
  }

}
