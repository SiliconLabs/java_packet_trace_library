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

  private final DebugMessageFilterExpression expression;

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
   * Add another filter expression to the filter with AND operator.
   *
   * @param argument
   * @throws ParseException
   */
  public void andFilter(final String expression) throws ParseException {
    throw new ParseException("Not yet implemented.", 0);
  }

  /**
   * Add another filter expression to the filter with AND operator.
   *
   * @param argument
   * @throws ParseException
   */
  public void orFilter(final String expression) throws ParseException {
    throw new ParseException("Not yet implemented.", 0);
  }

  /**
   * If it returns true, the message is kept.
   */
  @Override
  public boolean isMessageKept(final DebugMessage message) {
    return true;
  }

  public String helpText() {
    StringBuilder sb = new StringBuilder();
    sb.append("Valid filtering expressions:\n");
    sb.append("  typeIn(x,y,z,...)       - type matches one of the values specified as numbers or text\n");
    sb.append("  originatorIn(x,y,z,...) - originator matches one of the values\n");
    sb.append("  contains(x)       - payload contains specified string or hexblob\n");
    sb.append("  sizeWithin(x,y)   - payload size must be within x and y, inclusive\n");
    sb.append("  timeWithin(x,y)   - network time must be within x and y, inclusive\n");
    sb.append("Expression can be prefixed with '!' denoting the negation.");
    return sb.toString();
  }
}

class DebugMessageFilterExpression {
  public static final int FIRST = 0;
  public static final int AND = 1;
  public static final int OR = 2;

  private final int operator;
  private final String expression;
  private final DebugMessageFilterExpression next = null;

  public DebugMessageFilterExpression(final int op, final String expression) {
    this.operator = op;
    this.expression = expression;
  }
}