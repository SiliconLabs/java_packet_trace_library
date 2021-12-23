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

package com.silabs.pti.splitter;

import com.silabs.pti.util.ICharacterListener;
import com.silabs.pti.util.ICharacterMatcher;

/**
 * This is a generic stream splitter based on open/close brackets.
 * 
 * It takes an input data in the form:
 * abcOPEN_BRACKET123CLOSE_BRACKETdefOPEN_BRACKET456CLOSE_BRACKET
 * 
 * and splits it into two output data streams: 1.) abcdef 2.)
 * OPEN_BRACKET123CLOSE_BRACKETOPEN_BRACKET456CLOSE_BRACKET.
 * 
 * So output 1 contains everything that is not within the brackets. And output 2
 * contains both brackets and what's inside them.
 * 
 * Created on Mar 5, 2013
 * 
 * @author timotej
 */
public class BracketSplitter implements ISplitter {

  private final ICharacterListener[] buckets = new ICharacterListener[bucketCount()];
  private boolean insideBrackets = false;

  private final ICharacterMatcher openBracket, closeBracket;

  private final byte[] stack;
  private int stackSize = 0;

  /**
   * Creates a splitter with open and close brackets.
   */
  public BracketSplitter(ICharacterMatcher openBracket, ICharacterMatcher closeBracket) {
    stack = new byte[Math.max(openBracket.length(), closeBracket.length())];
    stackSize = 0;
    this.openBracket = openBracket;
    this.closeBracket = closeBracket;
  }

  @Override
  public void setCharacterListener(int bucket, ICharacterListener l) {
    buckets[bucket] = l;
  }

  @Override
  public int bucketCount() {
    return 2;
  }

  @Override
  public void received(byte[] input, int offset, int len) {
    int startIndex = offset;
    int count = 0;
    for (int i = offset; i < offset + len; i++) {
      byte c = input[i];
      ICharacterMatcher expectedBracket = (insideBrackets ? closeBracket : openBracket);
      int currentTarget = (insideBrackets ? 1 : 0);

      if (expectedBracket.isByteAt(stackSize, c)) { // Do we match expected?
        stack[stackSize++] = c;
        if (stackSize == expectedBracket.length()) { // Full bracket matches
          // Flush accumulated input before bracket, flush bracket, and reset.
          if (count > 0)
            buckets[currentTarget].received(input, startIndex, count);
          startIndex = i + 1;
          count = 0;
          insideBrackets = !insideBrackets; // Flip insideness
          buckets[1].received(stack, 0, stackSize); // Bracket goes to output 1
          stackSize = 0; // Clear stack
        }
      } else { // No match. Add stack to current output and flush it.
        count += (stackSize + 1);
        stackSize = 0;
      }
    }
    if (count > 0) {
      buckets[insideBrackets ? 1 : 0].received(input, startIndex, count);
    }
  }

  @Override
  public void flush() {
    if (stackSize > 0) {
      buckets[insideBrackets ? 1 : 0].received(stack, 0, stackSize);
      stackSize = 0;
    }
    insideBrackets = false;
  }

}
