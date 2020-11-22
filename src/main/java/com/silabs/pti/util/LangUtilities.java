// Copyright 2013 Silicon Laboratories, Inc.

package com.silabs.pti.util;

/**
 * Simple random java language utilities that don't belong anywhere else.
 *
 * Created on Feb 5, 2013
 *
 * @author timotej
 */
public class LangUtilities {

  /**
   * Returns the integer value of the given hex digit, or -1 if it was not a hex
   * digit.
   */
  public static int hexDigitValue(final char digit) {
    if ('0' <= digit && digit <= '9')
      return digit - '0';
    else if ('A' <= digit && digit <= 'F')
      return digit - 'A' + 10;
    else if ('a' <= digit && digit <= 'f')
      return digit - 'a' + 10;
    else
      return -1;
  }

  /**
   * This method parses a string into int. String can start with 0x or 0X to
   * denote hexadecimal number. You will typically use this when users enter the
   * string.
   *
   * @param s
   *          String containing the value.
   * @return int
   */
  public static int parseInt(String s) throws NumberFormatException {
    if (s == null)
	  throw new NumberFormatException("Expecting number: " + s);
    try {
      s = s.trim();
      if (s.startsWith("0x") || s.startsWith("0X")) {
        s = s.substring(2);
        if (s.length() == 8) {
          // deal with 0xFFFFFFFF case and java signed ints
          char firstCh = s.charAt(0);
          if (firstCh == '8' || firstCh == '9'
              || (firstCh >= 'A' && firstCh <= 'F')
              || (firstCh >= 'a' && firstCh <= 'f')) {
            long l = Long.parseLong(s, 16);
            return (int) (l & 0xFFFFFFFF);
          } else {
            return Integer.parseInt(s, 16);
          }
        } else {
          return Integer.parseInt(s, 16);
        }
      } else {
        return Integer.parseInt(s);
      }
    } catch (NumberFormatException nfe) {
      throw nfe;
    } catch (Exception e) {
      throw new NumberFormatException("Expecting number: " + s);
    }
  }

  /**
   * Takes a byte and returns a byte with reversed bits.
   *
   * @return byte
   * @since 4.23
   */
  public static byte reverseBits(final byte b) {
    int x = 0x00000000 | (b<<8);
    return (byte)((Integer.reverse(x) >> 16) & 0x0000FFFF);
  }
}
