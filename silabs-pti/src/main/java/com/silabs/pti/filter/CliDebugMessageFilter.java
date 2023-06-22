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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.DebugMessageType;
import com.silabs.pti.util.MiscUtil;

/**
 * Filter implementation fed from the CLI argument.
 *
 * @author timotej Created on Oct 5, 2022
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

  public static String helpText() {
    final StringBuilder sb = new StringBuilder();
    sb.append("  " + FilterExpression.TYPE_IN.functionName()
        + "(x,y,z,...)       - type matches one of the values specified as numbers or text\n");
    sb.append("  " + FilterExpression.ORIGINATOR_IN.functionName()
        + "(x,y,z,...) - originator matches one of the values\n");
    sb.append("  " + FilterExpression.CONTAINS.functionName()
        + "(x)             - payload contains specified string or hexblob\n");
    sb.append("  " + FilterExpression.SIZE_WITHIN.functionName()
        + "(x,y)         - payload size must be within x and y, inclusive\n");
    sb.append("  " + FilterExpression.TIME_WITHIN.functionName()
        + "(x,y)         - network time must be within x and y, inclusive\n");
    sb.append("(Expression can be prefixed with '!' for negation.)\n");
    return sb.toString();
  }
}

enum FilterExpression {
  TYPE_IN("typeIn"),
  ORIGINATOR_IN("originatorIn"),
  CONTAINS("contains"),
  SIZE_WITHIN("sizeWithin"),
  TIME_WITHIN("timeWithin"),;

  private String fnName;

  private FilterExpression(final String name) {
    this.fnName = name;
  }

  /**
   * Given an expression, return the appropriate FilterExpression, or null if it
   * doesn't match.
   */
  public static FilterExpression locate(final String expression) {
    for (final FilterExpression fe : FilterExpression.values()) {
      if (expression.startsWith(fe.functionName() + "(") && expression.endsWith(")")) {
        return fe;
      }
    }
    return null;
  }

  public IDebugMessageFilter createExpression(final String expression) throws ParseException {
    final String parenthesesContent = expression.substring(fnName.length() + 1, expression.length() - 1);
    IDebugMessageFilter dms = IDebugMessageFilter.NO_PASS_FILTER;
    switch (this) {
    case TYPE_IN:
      dms = new TypeInFilter(parenthesesContent);
      break;
    case CONTAINS:
      dms = new ContainsFilter(parenthesesContent);
      break;
    case ORIGINATOR_IN:
      dms = new OriginatorInFilter(parenthesesContent);
      break;
    case SIZE_WITHIN:
      dms = new WithinFilter(WithinFilter.SIZE, parenthesesContent);
      break;
    case TIME_WITHIN:
      dms = new WithinFilter(WithinFilter.TIME, parenthesesContent);
      break;
    }
    return dms;
  }

  public String functionName() {
    return fnName;
  }
}

class WithinFilter implements IDebugMessageFilter {
  public static final int SIZE = 0;
  public static final int TIME = 1;
  private final int mode;
  private long lowerBound, upperBound;

  WithinFilter(final int mode, final String values) throws ParseException {
    this.mode = mode;
    final String[] s = values.split(Pattern.quote(","));
    if (s.length != 2)
      throw new ParseException("Two values required: " + values, 0);
    try {
      lowerBound = Long.parseLong(s[0]);
      upperBound = Long.parseLong(s[1]);
    } catch (final NumberFormatException nfe) {
      throw new ParseException("Not a number: " + values, 0);
    }
  }

  @Override
  public boolean isMessageKept(final DebugMessage message) {
    switch (mode) {
    case SIZE:
      return (message.contentLength() >= lowerBound && message.contentLength() <= upperBound);
    case TIME:
      return (message.networkTime() >= lowerBound && message.networkTime() <= upperBound);
    default:
      return false;
    }
  }
}

class ContainsFilter implements IDebugMessageFilter {
  private final String pattern;

  public ContainsFilter(final String pattern) {
    this.pattern = pattern;
  }

  @Override
  public boolean isMessageKept(final DebugMessage message) {
    final String s = new String(message.contents());
    return s.contains(pattern);
  }
}

class TypeInFilter implements IDebugMessageFilter {
  private final List<Integer> ints = new ArrayList<>();
  private final List<String> names = new ArrayList<>();

  TypeInFilter(final String types) {
    for (String s : types.split(Pattern.quote(","))) {
      s = s.toLowerCase();
      try {
        ints.add(MiscUtil.parseInt(s));
      } catch (final NumberFormatException e) {
        names.add(s);
      }
    }
  }

  @Override
  public boolean isMessageKept(final DebugMessage message) {
    final int dt = message.debugType();
    for (final Integer i : ints) {
      if (dt == i)
        return true;
    }
    final DebugMessageType dmt = DebugMessageType.get(dt);
    for (final String s : names) {
      if (dmt.description().toLowerCase().equals(s))
        return true;
    }
    return false;
  }
}

class OriginatorInFilter implements IDebugMessageFilter {
  private final String[] originators;

  OriginatorInFilter(final String origs) {
    originators = origs.split(Pattern.quote(","));
  }

  @Override
  public boolean isMessageKept(final DebugMessage message) {
    final String orig = message.originatorId();
    for (final String s : originators) {
      if (s.equals(orig))
        return true;
    }
    return false;
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
  private IDebugMessageFilter expressionFilter;

  public DebugMessageFilterExpression(final int op, final String expression) throws ParseException {
    this.operator = op;

    final String exp = expression.strip();
    if (exp.startsWith("!")) {
      this.negated = true;
      parseExpression(exp.substring(1).strip());
    } else {
      this.negated = false;
      parseExpression(exp);
    }
  }

  private void parseExpression(final String expression) throws ParseException {
    FilterExpression fexp = null;
    if ("true".equals(expression)) {
      expressionFilter = IDebugMessageFilter.ALL_PASS_FILTER;
    } else if ("false".equals(expression)) {
      expressionFilter = IDebugMessageFilter.NO_PASS_FILTER;
    } else if ((fexp = FilterExpression.locate(expression)) != null) {
      expressionFilter = fexp.createExpression(expression);
    } else {
      throw new ParseException("Invalid filter expression: " + expression, 0);
    }
  }

  @Override
  public boolean isMessageKept(final DebugMessage message) {
    boolean evaluatedExpression = expressionFilter.isMessageKept(message);
    if (negated) {
      evaluatedExpression = !evaluatedExpression;
    }

    if (next != null) {
      if (next.operator() == AND) {
        evaluatedExpression = (evaluatedExpression && next.isMessageKept(message));
      } else if (next.operator() == OR) {
        evaluatedExpression = (evaluatedExpression || next.isMessageKept(message));
      }
    }

    return evaluatedExpression;
  }

  public int operator() {
    return operator;
  }

  public void and(final String expression) throws ParseException {
    final DebugMessageFilterExpression dmf = new DebugMessageFilterExpression(AND, expression);
    if (next == null) {
      next = dmf;
    } else {
      DebugMessageFilterExpression finger = next;
      while (finger.next != null)
        finger = finger.next;
      finger.next = dmf;
    }
  }

  public void or(final String expression) throws ParseException {
    final DebugMessageFilterExpression dmf = new DebugMessageFilterExpression(OR, expression);
    if (next == null) {
      next = dmf;
    } else {
      DebugMessageFilterExpression finger = next;
      while (finger.next != null)
        finger = finger.next;
      finger.next = dmf;
    }
  }

}