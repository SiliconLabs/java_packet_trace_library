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

  private final DebugMessageFilterExpression filterChain;

  /**
   * Create the filter with the initial filter expression.
   *
   * @param expression
   * @throws ParseException
   */
  public CliDebugMessageFilter(final String expression) throws ParseException {
    filterChain = new DebugMessageFilterExpression(DebugMessageFilterExpression.FIRST, expression);
  }

  /**
   * Add another filter expression to the filter with AND operator.
   *
   * @param expression
   * @throws ParseException
   */
  public void andFilter(final String expression) throws ParseException {
    filterChain.and(expression);
  }

  /**
   * Add another filter expression to the filter with AND operator.
   *
   * @param expression
   * @throws ParseException
   */
  public void orFilter(final String expression) throws ParseException {
    filterChain.or(expression);
  }

  /**
   * If it returns true, the message is kept.
   */
  @Override
  public boolean isMessageKept(final DebugMessage message) {
    return filterChain.isMessageKept(message);
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

// Single node in an expression, with an operator and an optional NOT
class DebugMessageFilterExpression implements IDebugMessageFilter {
  public static final int FIRST = 0;
  public static final int AND = 1;
  public static final int OR = 2;

  private final int operator;
  private DebugMessageFilterExpression next = null;
  private final boolean negated;

  private boolean isKept;
  
  public DebugMessageFilterExpression(final int op, final String expression) throws ParseException {
    this.operator = op;
    
    String exp = expression.strip();
    if ( exp.startsWith("!") ) {
      this.negated = true;
      parseExpression(exp.substring(1).strip());
    } else {
      this.negated = false;
      parseExpression(exp);
    }
  }
  
  private void parseExpression(String expression) throws ParseException {
    if ( "true".equals(expression) ) {
      isKept = true;
    } else if ( "false".equals(expression) ) {
      isKept = false;
    } else {
      throw new ParseException("Invalid filter expression: " + expression, 0);
    }
  }
  
  @Override
  public boolean isMessageKept(DebugMessage message) {
    if ( negated ) {
      return !isKept;
    } else {
      return isKept;
    }
  }
  
  public int operator() { return operator; }
  
  public void and(String expression) throws ParseException {
    DebugMessageFilterExpression dmf = new DebugMessageFilterExpression(AND, expression);
    if ( next == null ) {
      next = dmf;
    } else {
      DebugMessageFilterExpression finger = next;
      while ( finger.next != null ) finger = finger.next;
      finger.next = dmf;
    }
  }
  
  public void or(String expression) throws ParseException {
    DebugMessageFilterExpression dmf = new DebugMessageFilterExpression(OR, expression);    
    if ( next == null ) {
      next = dmf;
    } else {
      DebugMessageFilterExpression finger = next;
      while ( finger.next != null ) finger = finger.next;
      finger.next = dmf;
    }
  }

}