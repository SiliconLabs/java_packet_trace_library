// Copyright 2013 Silicon Laboratories, Inc.
package com.silabs.pti.util;

/**
 * Various static utilities for dealing with byte arrays and formatting them.
 *
 * Created on Jul 26, 2005
 * @author Timotej (timotej@ember.com)
 */
public class FrameUtil {

  public static String formatByteArray(final byte[] raw, final boolean useSpace) {
    if (raw == null)
      return null;
    return formatByteArray(raw, 0, raw.length, useSpace, true);
  }

  /**
   * Simple formatting of byte array into a "ab cd ef" kind of a string
   */
  public static String formatByteArray(final byte[] raw) {
    return formatByteArray(raw, true);
  }



  // Formats byte array and returns it as string.
  public static String formatByteArray(final byte[] raw,
                                       final int start,
                                       final int length,
                                       final boolean useSpace,
                                       final boolean upperCase) {
    if (raw == null)
      return null;
    StringBuffer result = new StringBuffer();
    formatByteArray(raw, start, length, useSpace, false, upperCase, result);
    return result.toString();
  }

  // Formats byte array into the provided string buffer.
  // This is the bottom-most method that does the actual work.
  // upper level API methods call this with various arguments
  private final static char[] LOWER_CASE = { '0', '1', '2', '3', '4', '5', '6',
                                             '7', '8', '9', 'a', 'b', 'c', 'd',
                                             'e', 'f' };
  private final static char[] UPPER_CASE = { '0', '1', '2', '3', '4', '5', '6',
                                             '7', '8', '9', 'A', 'B', 'C', 'D',
                                             'E', 'F' };
  private static void formatByteArray(final byte[] raw,
                                      final int start,
                                      final int length,
                                      final boolean useSpace,
                                      final boolean use0xPrefixAndComma,
                                      final boolean useUpperCase,
                                      final StringBuffer result) {
    if (raw == null)
      return;
    char[] charArray;
    if ( useUpperCase )
      charArray = UPPER_CASE;
    else
      charArray = LOWER_CASE;
    for (int i = start; i < (start + length); i++) {
      if (useSpace && (i != start))
        result.append((use0xPrefixAndComma?", ":" "));
      if (use0xPrefixAndComma) {
        result.append("0x");
      }
      try {
        byte nibHi = (byte)((raw[i]>>4) & 0x000F);
        byte nibLo = (byte)(raw[i] & 0x000F);
        // Speed up. The toHexInt() is ridiculous, as it
        // allocates 32 bytes for each digit, and runs GC up against the wall.
        // In case of ISD, which does HUGE amount of these calls, it becomes
        // the single largest source of GC activity.
        // So this is implementation that doesn't use any heap,
        // just few bytes of stack.
        result.append(charArray[nibHi]);
        result.append(charArray[nibLo]);
      } catch (ArrayIndexOutOfBoundsException e) {
        result.append("  ");
      }
    }

  }

  /**
   * Converts an array of bytes into a long.
   * The length should be at most 8.
   * @throws  ArrayIndexOutOfBoundsException
   */
  public static long byteArrayToLong(final byte[] raw,
                                     final int offset,
                                     final int length,
                                     final boolean bigEndian) {
    long value = 0;
    int index = bigEndian ? (offset + length) - 1 : offset;
    int increment = bigEndian ? -1 : 1;
    for (int i = 0; i < length; i++) {
      value |= ((long)(raw[index] & 0x00FF)) << (8 * i);
      index += increment;
    }
    return value;
  }
}
