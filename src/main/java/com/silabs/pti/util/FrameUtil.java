// Copyright 2013 Silicon Laboratories, Inc.
package com.silabs.pti.util;

/**
 * Various static utilities for dealing with byte arrays and formatting them.
 *
 * Created on Jul 26, 2005
 * @author Timotej (timotej@ember.com)
 */
public class FrameUtil {
  /**
   * Formats a byte array as an ascii block, with dots for non-printable
   * characters.
   *
   *
   * @param
   * @returns String
   */
  public static String formatAsciiBlock(final byte[] raw,
                                        int bytesPerBlock,
                                        int blocksPerLine,
                                        final String blockSeparator) {
    // Sanity protection
    if ( bytesPerBlock < 1 )
      bytesPerBlock = 1;
    if ( blocksPerLine < 1 )
      blocksPerLine = 1;

    StringBuffer result = new StringBuffer();
    int index = 0;
    while (index < raw.length) {
      for (int i = 0; i < blocksPerLine; i++) {
        if (i != 0)
          result.append(blockSeparator);
        formatByteArrayAscii(raw, index, bytesPerBlock, result);
        index += bytesPerBlock;
      }
      result.append("\n");
    }
    return result.toString();

  }


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


  /**
   * Simple formatting of byte array into a C-like source for bytes.
   */
  public static String formatBytesAsSourceCode(final byte[] raw,
                                               final int start,
                                               final int length) {
    if (raw == null)
      return null;
    StringBuffer result = new StringBuffer();
    formatByteArray(raw, start, length, true, true, true, result);
    return result.toString();
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
   * Formats the given bytes in ASCII as space-delimited hex strings, but
   * restricts the total length of the formatted output to outputLength
   * bytes using an ellipsis if necessary.
   */
  public static String formatByteArraySummary(final byte[] raw,
                                              final int start,
                                              final int length,
                                              final int outputLength) {
	if (raw == null)
	  return "";
    return ( (length > outputLength)
             ? (FrameUtil.formatByteArray(raw, start, outputLength - 2, true, true)
               + " .. "
               + FrameUtil.hex(raw[(start + length) - 1] & 0xFF, 2))
             : FrameUtil.formatByteArray(raw, start, length, true, true));
  }

  /**
   * Method used by ascii printing of byte arrays to determing which characters
   * are dots.
   */
  public static boolean unprintableByte(final int intByte) {
    return (intByte < 33) || (intByte > 126);
  }

  /**
   * Create an ascii representation of byte array.
   */
  public static void formatByteArrayAscii(final byte[] raw,
                                          final int start,
                                          final int length,
                                          final StringBuffer result) {
    if (raw == null)
      return;
    for (int i = start; i < (start + length); i++) {
      try {
        int b = raw[i] & 0xFF;
        if (unprintableByte(b))
          result.append('.');
        else
          result.append((char)b);
      } catch (ArrayIndexOutOfBoundsException e) {
        result.append(" ");
      }
    }
  }

  public static String formatByteArrayAscii(final byte[] raw) {
    return formatByteArrayAscii(raw, 0, raw.length);
  }

  // Formats and returns a string
  public static String formatByteArrayAscii(final byte[] raw, final int start, final int length) {
    if (raw == null)
      return null;
    StringBuffer result = new StringBuffer();
    formatByteArrayAscii(raw, start, length, result);
    return result.toString();
  }

  public static int
  highLowToInt(final int high, final int low)
  {
    return ((high & 0xFF) << 8) + (low & 0xFF);
  }

  /**
   * Converts an array of bytes into an unsigned integer.
   * The length should be at most 4.  When the length is 4 bytes,
   * is there a way to make sure this an unsigned integer without using a long?
   * @throws  ArrayIndexOutOfBoundsException
   */
  public static int byteArrayToInt(final byte[] raw,
                                   final int offset,
                                   final int length,
                                   final boolean bigEndian) {
    int value = 0;
    int index = bigEndian ? (offset + length) - 1 : offset;
    int increment = bigEndian ? -1 : 1;
    for (int i = 0; i < length; i++) {
      value += (raw[index] & 0xFF) << (8 * i);
      index += increment;
    }
    return value;
  }

  /**
   * Takes a long and lays it out into a destination array in
   * big- or little-endian format. It will use 8 bytes of the array.
   *
   * @throws ArrayIndexOutOfBoundsException if there is not enough space.
   * @returns void
   * @since 4.16
   */
  public static void longToByteArray(final long value,
                                     final byte[] dest,
                                     final int offset,
                                     final boolean bigEndian) {
    long v = value;
    for ( int i=0; i<8; i++ ) {
      int index = bigEndian ? offset+7-i : offset+i;
      dest[index] = (byte)(v & 0x00FF);
      v >>= 8;
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

  /**
   * Formats an integer as a hex string with an even number of digits,
   * given minimum length, and no spaces.
   */
  public static String hex(final long value, final int minLength) {
    String result = Long.toHexString(value).toUpperCase();
    while (((result.length() % 2) != 0) || (result.length() < minLength)) {
      result = "0" + result;
    }
    return result;
  }

  /**
   * Formats an integer as a hex string with an even number of digits,
   * given minimum length, and no spaces.
   */
  public static String hex(final int value, final int minLength) {
    String result = Integer.toHexString(value).toUpperCase();
    while (((result.length() % 2) != 0) || (result.length() < minLength)) {
      result = "0" + result;
    }
    return result;
  }

  /**
   * Returns the value of the given hex string (no leading '0x'), or -1
   * if it was not a hex string.  Does not check for overflow if the
   * value of the supplied string is too large.
   */
  public static int hexStringValue(final String hexString) {
    int result = 0;
    for (int i = 0, j = hexString.length() - 1;
        i < hexString.length();
        i++, j--) {
      int value = LangUtilities.hexDigitValue(hexString.charAt(j));
      if (value == -1)
        return -1;
      result += (value << (4 * i));
    }
    return result;
  }
}
