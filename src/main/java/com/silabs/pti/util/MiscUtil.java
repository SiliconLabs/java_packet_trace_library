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

package com.silabs.pti.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Simple random java language utilities that don't belong anywhere else.
 *
 * Created on Feb 5, 2013
 *
 * @author timotej
 */
public class MiscUtil {

  private static long networkInterfaceRefreshInterval = 5 * 1000; // ms
  private static long nextNetworkInterfaceFetch = 0;
  private static NetworkInterface[] lastNetworkInterfaces;

  /**
   * It can be SLLOOOOOW to query these, so cache the results for a reasonable
   * window of time
   */
  private static NetworkInterface[] getRecentNetworkInterfaces() {
      long now = System.currentTimeMillis();
      if (now >= nextNetworkInterfaceFetch) {
          try {
              List<NetworkInterface> list = new ArrayList<>();
              for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                  list.add(en.nextElement());
              }
              lastNetworkInterfaces = list.toArray(new NetworkInterface[list.size()]);
          } catch (SocketException e) {
              if (lastNetworkInterfaces == null)
                  lastNetworkInterfaces = new NetworkInterface[0];
          }
          nextNetworkInterfaceFetch = now + networkInterfaceRefreshInterval;
      }
      return lastNetworkInterfaces;
  }

  /**
   * This method returns a list of all non-loopback active local addresses.
   * If you are looking for a "default" address, then the default address will
   * be the first element in the list, if list is non-empty.
   *
   * @return list of IP addresses.
   */
  public static List<InetAddress> getIpAddresses() {
      List<InetAddress> addresses = new ArrayList<>();
      try {
          for (NetworkInterface ni : getRecentNetworkInterfaces()) {
              if ( !ni.isUp() )
                  continue;
              Enumeration<InetAddress> addrs = ni.getInetAddresses();
              while ( addrs.hasMoreElements() ) {
                  InetAddress ia = addrs.nextElement();
                  if ( ia.isLoopbackAddress() )
                      continue;
                  if ( !addresses.contains(ia) )
                      addresses.add(ia);
              }
          }
      } catch (SocketException se ) {
          // Whatever. We can't get addresess, we won't return them.
      }
      return addresses;
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

  public static String formatByteArray(final byte[] raw, final boolean useSpace) {
    if (raw == null)
      return null;
    return MiscUtil.formatByteArray(raw, 0, raw.length, useSpace, true);
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
    MiscUtil.formatByteArray(raw, start, length, useSpace, false, upperCase, result);
    return result.toString();
  }
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
   * Static method for extracting a float number.
   *
   *
   * @param
   * @returns Number
   */
  public static Number byteArrayToFloat(final byte[] raw,
                                        final int offset,
                                        final int length,
                                        final boolean bigEndian) {
    ByteBuffer bb;
    if ( bigEndian ) {
      bb = ByteBuffer.wrap(raw, offset, length);
    } else {
      byte[] b = new byte[length];
      System.arraycopy(raw, offset, b, 0, length);
      b = reverseBytes(b);
      bb = ByteBuffer.wrap(b);
    }
    switch(length) {
    case 4:
      return bb.getFloat();
    case 8:
      return bb.getDouble();
    default:
      throw new IllegalArgumentException("Only floats of size 4 or 8 can be decoded.");
    }
  }

}
