// Copyright 2013 Silicon Laboratories, Inc.

package com.silabs.pti.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple random java language utilities that don't belong anywhere else.
 *
 * Created on Feb 5, 2013
 *
 * @author timotej
 */
public class LangUtilities {
  private static String DEFAULT_ESCAPER = "\\";

  /**
   * Counts the number of bits in an integer. See:
   * http://infolab.stanford.edu/~manku/bitcount/bitcount.html
   *
   * This implementation is too beautiful to be ignored! :) Thank you, Richard!
   *
   * NOTE: This works only for positive integers, so first bit may not be set.
   * Make sure of that.
   *
   * @param incoming
   *          integer
   * @returns number of bits that are set to 1
   */
  public static int bitcount(final int n) {
    int tmp = n - ((n >> 1) & 033333333333) - ((n >> 2) & 011111111111);
    return ((tmp + (tmp >> 3)) & 030707070707) % 63;
  }

  /**
   * Counts the number of bits in a byte array.
   * @returns int
   */
  public static int bitcount(final byte[] bytes) {
    int n = 0, i=0, rem = 0;
    while(i<bytes.length) {
      rem |= ((bytes[i]&0xFF) << ((i%3)*8));
      if ( i%3 == 2 && rem != 0 ) {
        n+=bitcount(rem);
        rem = 0;
      }
      i++;
    }
    if ( rem != 0 )
      n += bitcount(rem);
    return n;
  }

  /**
   * Method that takes enum class and returns all the names of enums. We do this
   * surprisingly many times in our tables, so the common function was much
   * needed.
   */
  public static String[] enumNames(final Class<? extends Enum<?>> x) {
    Enum<?>[] arr = x.getEnumConstants();
    if (arr == null)
      throw new IllegalArgumentException("Not an enum");
    String[] names = new String[arr.length];
    for (int i = 0; i < arr.length; i++)
      names[i] = arr[i].name();
    return names;
  }

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
   * More bit stuff from Richard. Checks if only one bit is set.
   *
   * @param incoming
   *          integer
   * @returns true if only one bit is set, false if more.
   */
  public static boolean isOneBitSet(final int n) {
    if (n == 0)
      return false;
    else
      return (n & (n - 1)) == 0;
  }

  /**
   * This method parses a string into a boolean. The string can be "true,"
   * "yes," or "1." The case of the string and leading or trailing whitespace
   * are ignored.
   *
   * @param s
   *          String containing the value
   * @returns boolean
   */
  public static boolean parseBoolean(String s) {
    if (s == null)
      return false;
    s = s.trim();
    return ("ENABLE".equalsIgnoreCase(s)
	        || "ENABLED".equalsIgnoreCase(s)
            || "TRUE".equalsIgnoreCase(s)
            || "YES".equalsIgnoreCase(s)
            || "1".equals(s));
  }

  /**
   * This method parses an object into a boolean.
   *
   * @param s
   *          String containing the value
   * @returns boolean
   */
  public static boolean parseBoolean(final Object x) {
    if ( x instanceof String ) {
      return parseBoolean((String)x);
    } else if ( x instanceof Boolean ) {
      return ((Boolean)x).booleanValue();
    } else {
      return false;
    }
  }

  /**
   * Unlike toBytes() which is anal about spaces between the characters, this
   * method is more suited to GUI text inputs. It will essentially remove all
   * whitespace and parse result. It will all be happy with 0x or missing 0x in
   * front.
   *
   * So for example: 1234ab is same as: 0x1234ab is same as: 12 34 ab is same
   * as: 1 234a b is same as: 0 x 1 2 3 4 a b is same as 0x12, 0x34, 0xab
   */
  public static byte[] parseBytes(final String text) throws ParseException {
    if (text.contains(",")) {
      String[] split = text.split(",");
      byte[] arr = new byte[split.length];
      for (int i = 0; i < split.length; i++) {
        String s = split[i].trim();
        int n;
        if (s.startsWith("0x") || s.startsWith("0X")) {
          // hex
          n = Integer.parseInt(s.substring(2), 16);
        } else {
          // decimal
          n = Integer.parseInt(s);
        }
        arr[i] = (byte) n;
      }
      return arr;
    }

    StringBuffer realText = new StringBuffer();
    try {
      int added = 0;
      for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        if (Character.isWhitespace(c))
          continue;
        realText.append(c);
        added++;
        if (added % 2 == 0) {
          realText.append(' ');
        }
      }

      // Strip out 0x thingies
      int i;
      do {
        i = realText.indexOf("0x ");
        if ( i >= 0 )
          realText.delete(i, i+3);
      } while(i>=0);

      return toBytesStrict(realText.toString().trim());
    } catch (ParseException e) {
      throw new ParseException(realText.toString() + " : " + e.getMessage(), 0);
    }
  }

  /** Essentially does parseBytes and then revertBytes on it. */
  public static byte[] parseEui64(final String text) throws ParseException {
    byte[] bs = parseBytes(text);
    revertBytes(bs);
    return bs;
  }

  /**
   * This method parses a string into long. String can start with 0x or 0X to
   * denote hexadecimal number. You will typically use this when users enter the
   * string.
   *
   * @param s
   *          String containing the value.
   * @returns long
   */
  public static long parseLong(String s) throws NumberFormatException {
    if (s == null)
  	  throw new NumberFormatException("Expecting number: " + s);
    try {
      s = s.trim();
      if (s.startsWith("0x") || s.startsWith("0X")) {
        // Check if the high bit would be set on the long. Oh Java, you and
        // your signed types...
        s = s.substring(2);
        if (s.length() > 15) {
          char firstCh = s.charAt(0);
          if (firstCh == '8' || firstCh == '9'
              || (firstCh >= 'A' && firstCh <= 'F')
              || (firstCh >= 'a' && firstCh <= 'f')) {
            throw new NumberFormatException("Cannot parse unsigned long.");
          } else {
            return Long.parseLong(s, 16);
          }
        } else {
          return Long.parseLong(s, 16);
        }
      } else {
        return Long.parseLong(s);
      }
    } catch (NumberFormatException nfe) {
      throw nfe;
    } catch (Exception e) {
      throw new NumberFormatException("Expecting number: " + s);
    }
  }

  /**
   * This method parses a string into int. String can start with 0x or 0X to
   * denote hexadecimal number. You will typically use this when users enter the
   * string.
   *
   * @param s
   *          String containing the value.
   * @returns int
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
   * This method parses a hexadecimal string into an integer.  The "0x" or "0X"
   * prefix is optional.  This is meant for input that should be hex, but where
   * the user might omit the prefix.
   *
   * @param input the string containing the value
   * @returns the integer
   */
  public static int parseHexInt(String input) throws NumberFormatException {
    if (input == null)
      throw new NumberFormatException("Expecting number: " + input);
    input = input.trim();
    if (!input.startsWith("0x") && !input.startsWith("0X"))
      input = "0x" + input;
    return parseInt(input);
  }

  /**
   * Reverts the order of bytes in the array. It reverts them IN PLACE, meaning
   * that original array is modified. Same array is also returned. NO NEW ARRAY
   * IS CONSTRUCTED.
   */
  public static byte[] revertBytes(final byte[] bytes) {
    byte reg;
    for (int i = 0; i < (bytes.length / 2); i++) {
      reg = bytes[i];
      bytes[i] = bytes[bytes.length - i - 1];
      bytes[bytes.length - i - 1] = reg;
    }
    return bytes;
  }

  /**
   * Converts a String representing a hex byte array into a byte array. The
   * bytes must be delimited by space, "\n, and must be two hex characters. If
   * other tokens are present, IllegalArgumentException is thrown. Carefully
   * profiled and coded for high performance. For example, avoids costly calls
   * to String.substring().
   *
   */
  public static byte[] toBytesStrict(final String input) throws ParseException {
    byte[] bytes = new byte[input.length() / 2 + 1];
    int byteIndex = 0;
    boolean midNibble = false;
    int hiNibble = 0;

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (Character.isWhitespace(c)) {
        if (midNibble)
          throw new ParseException("Incomplete byte.", i - 1);
        else
          continue;
      }
      int nibble = hexDigitValue(c);
      if (nibble == -1) {
        throw new ParseException("Not a hex digit.", i);
      }
      if (midNibble) {
        midNibble = false;
        bytes[byteIndex++] = (byte) ((hiNibble << 4) + nibble);
      } else {
        hiNibble = nibble;
        midNibble = true;
      }
    }
    if (midNibble) {
      throw new ParseException("Partial nibble at end.", input.length() - 1);
    }
    byte[] trimmed = new byte[byteIndex];
    System.arraycopy(bytes, 0, trimmed, 0, byteIndex);
    return trimmed;
  }


  /**
   * Compares two integers. Does the same as Integer.compare() in java 1.7,
   * but since we support 1.6 as well, we put it here.
   *
   *
   * @param
   * @returns String
   */
  public static int compare(final int x, final int y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }


  private static String escape(String string, String escaper, final Object... escapees) {
    if (string != null) {
      escaper = Matcher.quoteReplacement(escaper);
      for (Object escapee : escapees)
        string = string.replaceAll(("("
                                    + Pattern.quote(escapee.toString())
                                    + ")"),
                                   escaper + "$1");
    }
    return string;
  }

  private static String unescape(String string, String escaper, final Object... escapees) {
    if (string != null) {
      escaper = Matcher.quoteReplacement(escaper);
      for (Object escapee : escapees)
        string = string.replaceAll((escaper
                                    + "("
                                    + Pattern.quote(escapee.toString())
                                    + ")"),
                                   "$1");
    }
    return string;
  }

  /**
   * Escape all instances of the escapees within the given string using the
   * given escaper.  For example:
   *
   * escape("{foo}", "\\", "{", "}") returns "\{foo\}"
   *
   * @param string string to escape
   * @param escapees list of strings to escape
   * @returns the escaped string
   */
  public static String escape(final String string, final String escaper, final String... escapees) {
    return escape(string, escaper, (Object[]) escapees);
  }

  /**
   * Escape all instances of the escapees within the given string using the
   * given escaper.
   *
   * escape("{foo}", "\\", "{", "}") returns "\{foo\}"
   *
   * @param string string to escape
   * @param escapees list of characters to escape
   * @returns the escaped string
   */
  public static String escape(final String string, final String escaper, final Character... escapees) {
    return escape(string, escaper, (Object[]) escapees);
  }

  /**
   * Escape a string using the default escape character:
   * {@value #DEFAULT_ESCAPER}.  See
   * {@link #escape(String, String, String...)}.
   *
   * @param string string to escape
   * @param escaper string with which to escape
   * @param escapees list of strings to escape
   * @returns the escaped string
   */
  public static String escape(final String string, final String... escapees) {
    return escape(string, DEFAULT_ESCAPER, (Object[]) escapees);
  }

  /**
   * Escape a string using the default escape sequence:
   * {@value #DEFAULT_ESCAPER}.  See
   * {@link #escape(String, String, String...)}.
   *
   * @param string string to escape
   * @param escaper string with which to escape
   * @param escapees list of characters to escape
   * @returns the escaped string
   */
  public static String escape(final String string, final Character... escapees) {
    return escape(string, DEFAULT_ESCAPER, (Object[]) escapees);
  }

  /**
   * Unescape all instances of the escapees within the given string using the
   * given escaper.  For example:
   *
   * unescape("\{foo\}", "\\", "{", "}") returns "foo"
   *
   * @param string string to unescape
   * @param escaper string used to escape
   * @param escapees list of strings that were escaped
   * @returns the unescaped string
   */
  public static String unescape(final String string, final String escaper, final String... escapees) {
    return unescape(string, escaper, (Object[]) escapees);
  }

  /**
   * Unescape all instances of the escapees within the given string using the
   * given escaper.  For example:
   *
   * unescape("\{foo\}", "\\", "{", "}") returns "foo"
   *
   * @param string string to unescape
   * @param escaper string used to escape
   * @param escapees list of characters that were escaped
   * @returns the unescaped string
   */
  public static String unescape(final String string, final String escaper, final Character... escapees) {
    return unescape(string, escaper, (Object[]) escapees);
  }

  /**
   * Unescape a string using the default escape sequence:
   * {@value #DEFAULT_ESCAPER}.  See
   * {@link #unescape(String, String, String...)}.
   *
   * @param string string to unescape
   * @param escapees list of strings that were escaped
   * @returns the unescaped string
   */
  public static String unescape(final String string, final String... escapees) {
    return unescape(string, DEFAULT_ESCAPER, (Object[]) escapees);
  }

  /**
   * Unescape a string using the default escape sequence:
   * {@value #DEFAULT_ESCAPER}.  See
   * {@link #unescape(String, String, String...)}.
   *
   * @param string string to unescape
   * @param escapees list of characters that were escaped
   * @returns the unescaped string
   */
  public static String unescape(final String string, final Character... escapees) {
    return unescape(string, DEFAULT_ESCAPER, (Object[]) escapees);
  }

  /**
   * Converts unsigned byte to integer. You should use this, if you
   * want unsigned conversion. Simply assigning byte to integer in java
   * will cause the typically undesired sign preservation, so any
   * byte that has the high bit set will result in an int that has high
   * bit set.
   *
   *
   * @param
   * @returns int
   */
  public static int unsignedByteToInt(final byte b) {
    return 0x000000FF & b;
  }

  /**
   * If you have a byte array, this method will return true
   * if N-th bit is set, where N is couted from the most significant bit on
   * the 0-th byte, so in natural bit order as a human would see them.
   *
   * For example:
   *    byte array {0x80, 0x00} has 0-th bit set.
   *    byte array {0x40, 0x00} has 1-st bit set.
   *    etc.
   *
   *
   * @param
   * @returns boolean
   */
  public static boolean isNthBitSet(final byte[] bytes, final int n) {
    int byteN = n/8;
    if ( byteN < 0 || byteN >= bytes.length )
      return false;
    int b = LangUtilities.unsignedByteToInt(bytes[byteN]);
    int mask = 0x0000001 << (7-n%8);
    return (mask & b) != 0;
  }

  /**
   * This method returns the number of least significant bits that are unset.
   * For example:
   *    0xFF => Returns 0
   *    0xA2 => return 1
   *    0xFF00 => return 8
   *    0 => returns 64.
   * @param
   * @returns int
   */
  private static long lowZeroBitCount(long n, final int maxBitCount) {
    long cnt = 0;
    while((n & 1) == 0 && cnt < maxBitCount) {
      cnt++;
      n >>= 1;
    }
    return cnt;
  }

  /**
   * This method returns the number of least significant bits that are unset.
   * For example:
   *    0xFF => Returns 0
   *    0xA2 => return 1
   *    0xFF00 => return 8
   *    0 => returns 64.
   * @param
   * @returns int
   * @since 5.8
   */
  public static long lowZeroBitCount(final long n) {
    return lowZeroBitCount(n, 64);
  }

  /**
   * This method returns the number of least significant bits that are unset.
   * For example:
   *    0xFF => Returns 0
   *    0xA2 => return 1
   *    0xFF00 => return 8
   *    0 => returns 32.
   * @param
   * @returns int
   */
  public static int lowZeroBitCount(final int n) {
    return (int)lowZeroBitCount(n, 32);
  }

  /**
   * Unsigned xor between two bytes.
   * @returns byte
   */
  public static byte xorBytes(final byte a, final byte b) {
    return (byte)(0x000000FF & (a ^ b));
  }

  /**
   * Compares two objects.  If both objects are null, they are considered
   * equal.  Otherwise, equality is determined by the
   * {@link Object#equals(Object)} method for simple objects and
   * {@link Arrays#deepEquals(Object[], Object[])} for arrays.  This should be
   * equivalent to Objects.deepEquals, which was added in Java 1.7.
   *
   * @param object1 the first object
   * @param object2 the second object
   * @returns true if the objects are equal or false if not
   */
  public static <T> boolean equals(final T object1, final T object2) {
    return Objects.deepEquals(object1, object2);
  }

  /**
   * Useful method that returns a new array with reversed
   * order of bytes. Original array remains unchanged.
   *
   *
   * @param byte[]
   * @returns byte[]
   */
  public static byte[] reverseBytes(final byte[] old) {
    byte[] result = new byte[old.length];
    for (int i = 0; i < old.length; i++)
      result[i] = old[old.length - i - 1];
    return result;
  }

  /**
   * Take any number of byte arrays and concatenate them together.
   * @returns byte[]
   */
  public static byte[] concat(final byte[]... data) {
    byte[] result = new byte[0];
    for (byte[] datum : data) {
      result = Arrays.copyOf(result, result.length + datum.length);
      System.arraycopy(datum, 0, result, result.length - datum.length, datum.length);
    }
    return result;
  }

  /**
   *  Formats a sentence. This involves:
   *    - An uppercase letter at the beginning of the sentence (usually).
   *    - Some sort of punctuation at the end of the sentence.
   *
   *  @param sentence, The sentence to be formatted.
   *
   *  @return A string of the formatted sentence.
   */
  public static String formatSentence(final String sentence) {
    // Check to make sure the sentence is not empty.
    if (sentence.length() == 0)
      return sentence;

    StringBuilder sentenceBuilder = new StringBuilder();

    // Find the first alphabetic character and make sure it is uppercase.
    int i;
    char c;
    for (i = 0; i < sentence.length(); i ++) {
      c = sentence.charAt(i);

      if (Character.isLetter(c)) {
        // If c is a letter, capitalize it and then break
        sentenceBuilder.append(Character.toUpperCase(sentence.charAt(i)));
        break;
      } else if (Character.isDigit(c)) {
        // If c is a number, assume that we don't need to capitalize anything.
        sentenceBuilder.append(c);
        break;
      }

      // If c is something else, then add it and go to the next character.
      sentenceBuilder.append(c);
    }

    sentenceBuilder.append(sentence.substring(i+1));

    // Check for punctuation at end of upgradeSentence.
    char lastCharacterOfSentence
      = sentence.charAt(sentence.length() - 1);
    boolean sentenceNeedsPeriod
      = (lastCharacterOfSentence != '.'
         && lastCharacterOfSentence != '!'
         && lastCharacterOfSentence != '?');
    if (sentenceNeedsPeriod)
      sentenceBuilder.append(".");

    return sentenceBuilder.toString();
  }

  /**
   * Returns own process ID. If it returns -1 it failed.
   */
  public static long myOwnProcessId() {
    try {
      RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
      String jvmName = rmxb.getName();
      long pid = Long.valueOf(jvmName.split("@")[0]);
      return pid;
    } catch (Throwable e) {
      return -1;
    }
  }

  /**
   * Takes a byte and returns a byte with reversed bits.
   *
   * @returns byte
   * @since 4.23
   */
  public static byte reverseBits(final byte b) {
    int x = 0x00000000 | (b<<8);
    return (byte)((Integer.reverse(x) >> 16) & 0x0000FFFF);
  }

  /**
   * Returns index of sub-array within array, or -1 if not found.
   *
   * @param haystack The array where needle is looked in.
   * @parem needle The array that is looked for in the haystack.
   * @return index of beginning of needle inside haystack, or -1 if not found.
   * @since 5.1
   */
  public static int findSubarray(final byte[] haystack, final byte[] needle) {
    if ( haystack == null || needle == null ) return -1;
    if ( haystack.length == 0 || needle.length == 0 ) return -1;
    int foundIndex = -1;
    outer: for ( int i=0; i <= haystack.length - needle.length; i++ ) {
      for ( int j=0; j < needle.length; j++ ) {
        if ( needle[j] != haystack[i+j] )
          continue outer;
      }
      foundIndex = i;
      break outer;
    }
    return foundIndex;
  }

}
