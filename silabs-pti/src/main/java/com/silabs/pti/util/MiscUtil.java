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
    final long now = System.currentTimeMillis();
    if (now >= nextNetworkInterfaceFetch) {
      try {
        final List<NetworkInterface> list = new ArrayList<>();
        for (final Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
          list.add(en.nextElement());
        }
        lastNetworkInterfaces = list.toArray(new NetworkInterface[list.size()]);
      } catch (final SocketException e) {
        if (lastNetworkInterfaces == null)
          lastNetworkInterfaces = new NetworkInterface[0];
      }
      nextNetworkInterfaceFetch = now + networkInterfaceRefreshInterval;
    }
    return lastNetworkInterfaces;
  }

  /**
   * This method returns a list of all non-loopback active local addresses. If you
   * are looking for a "default" address, then the default address will be the
   * first element in the list, if list is non-empty.
   *
   * @return list of IP addresses.
   */
  public static List<InetAddress> getIpAddresses() {
    final List<InetAddress> addresses = new ArrayList<>();
    try {
      for (final NetworkInterface ni : getRecentNetworkInterfaces()) {
        if (!ni.isUp())
          continue;
        final Enumeration<InetAddress> addrs = ni.getInetAddresses();
        while (addrs.hasMoreElements()) {
          final InetAddress ia = addrs.nextElement();
          if (ia.isLoopbackAddress())
            continue;
          if (!addresses.contains(ia))
            addresses.add(ia);
        }
      }
    } catch (final SocketException se) {
      // Whatever. We can't get addresess, we won't return them.
    }
    return addresses;
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
   * This method parses a string into int. String can start with 0x or 0X to
   * denote hexadecimal number. You will typically use this when users enter the
   * string.
   *
   * @param s String containing the value.
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
          final char firstCh = s.charAt(0);
          if (firstCh == '8' || firstCh == '9' || (firstCh >= 'A' && firstCh <= 'F')
              || (firstCh >= 'a' && firstCh <= 'f')) {
            final long l = Long.parseLong(s, 16);
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
    } catch (final NumberFormatException nfe) {
      throw nfe;
    } catch (final Exception e) {
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
    final int x = 0x00000000 | (b << 8);
    return (byte) ((Integer.reverse(x) >> 16) & 0x0000FFFF);
  }

  /**
   * Useful method that returns a new array with reversed order of bytes. Original
   * array remains unchanged.
   *
   *
   * @param old Source byte array.
   * @return byte[]
   */
  public static byte[] reverseBytes(final byte[] old) {
    final byte[] result = new byte[old.length];
    for (int i = 0; i < old.length; i++)
      result[i] = old[old.length - i - 1];
    return result;
  }

  /**
   * Static method for extracting a float number.
   *
   *
   * @param raw    byte array
   * @param offset beginning of array
   * @param length Number of bytes.
   * @return Number
   */
  public static Number byteArrayToFloat(final byte[] raw, final int offset, final int length, final boolean bigEndian) {
    ByteBuffer bb;
    if (bigEndian) {
      bb = ByteBuffer.wrap(raw, offset, length);
    } else {
      byte[] b = new byte[length];
      System.arraycopy(raw, offset, b, 0, length);
      b = reverseBytes(b);
      bb = ByteBuffer.wrap(b);
    }
    switch (length) {
    case 4:
      return bb.getFloat();
    case 8:
      return bb.getDouble();
    default:
      throw new IllegalArgumentException("Only floats of size 4 or 8 can be decoded.");
    }
  }

  /**
   * Converts unsigned byte to integer. You should use this, if you want unsigned
   * conversion. Simply assigning byte to integer in java will cause the typically
   * undesired sign preservation, so any byte that has the high bit set will
   * result in an int that has high bit set.
   *
   *
   * @param b Single unsigned byte.
   * @return int
   */
  public static int unsignedByteToInt(final byte b) {
    return 0x000000FF & b;
  }

}
