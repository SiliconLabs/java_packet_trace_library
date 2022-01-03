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

package com.silabs.pti.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.silabs.pti.util.MiscUtil;

/**
 * Utilities for discovery.
 *
 * @author timotej Created on Jan 8, 2019
 */
public class DiscoveryUtil {

  private static byte[] broadcast = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

  private DiscoveryUtil() {
  }

  public static int runDiscovery(final IDiscoveryListener listener) {
    try {
      discover(500, listener);
      return 0;
    } catch (Exception e) {
      e.printStackTrace();
      return 1;
    }
  }

  private static void discoverIndividualAddress(final InetAddress localAddress, final int durationMs, final IDiscoveryListener listener) {
    String log = localAddress.getHostName();
    try (DatagramSocket socket = new DatagramSocket(0, localAddress)) {
      socket.setSoTimeout(durationMs / 10);
      DatagramPacket dp = new DatagramPacket(DiscoveryProtocol.DISCOVERY_MESSAGE, DiscoveryProtocol.DISCOVERY_MESSAGE.length);
      dp.setAddress(InetAddress.getByAddress(broadcast));
      dp.setPort(4920);
      try {
        socket.send(dp);
      } catch (Exception e) {
        System.err.println(log + ": failed to send discovery packet.");
        return;
      }

      byte[] inBuff = new byte[DiscoveryProtocol.RECEIVE_LENGTH];
      DatagramPacket incoming = new DatagramPacket(inBuff, inBuff.length);
      incoming.setPort(DiscoveryProtocol.UDP_PORT);
      long lastDiscoverTime = System.currentTimeMillis();
      do {
        try {
          socket.receive(incoming);
          lastDiscoverTime = System.currentTimeMillis();
          listener.discovered(incoming);
        } catch (SocketTimeoutException ste) {
          // Not a big deal. Keep going.
        }
      } while (System.currentTimeMillis() - lastDiscoverTime < durationMs);
    } catch (Exception e) {
      System.err.println(log + ": discovery failed.");
      e.printStackTrace();
    }
  }

  /**
   * Given the incoming datagram packet, this uses the parseDiscoveryMap(String)
   * to parse the payload of the packet.
   * 
   * @param packet
   * @return map of discovery keys.
   */
  public static Map<DiscoveryKey, String> parseDiscoveryMap(DatagramPacket packet) {
    return parseDiscoveryMap(new String(packet.getData()));
  }
  
  /**
   * Given the string text from the discovery, this parses the data into a map.
   * 
   * @param info
   * @return map of discovery keys
   */
  public static Map<DiscoveryKey, String> parseDiscoveryMap(final String info) {
    Map<DiscoveryKey, String> map = new LinkedHashMap<>();
    String [] tokens = info.split("\\n");
    outer: for ( String token: tokens ) {
      token = token.trim();
      String[] subToks = token.split("=");
      if (subToks.length != 2)
        continue;
      // ignore values that aren't set
      if (subToks[1].equals("NotSet"))
        continue;
      if (subToks[0].equals(DiscoveryKey.CONNECTION_TIME.key())) {
        String[] timeToken = subToks[1].split(":");
        try {
          int day = Integer.parseInt(timeToken[0]);
          int hour = Integer.parseInt(timeToken[1]);
          int minute = Integer.parseInt(timeToken[2]);
          int second = Integer.parseInt(timeToken[3]);
          long timePassed = ((((((day * 24) + hour) * 60) + minute) * 60) + second) * 1000;
          Date connectTime = new Date((new Date()).getTime() - timePassed);
          map.put(DiscoveryKey.CONNECTION_TIME, connectTime.toString());
        } catch (Exception e) {
          // Ignore bogus connection times.
        }
      } else {
        for ( DiscoveryKey t: DiscoveryKey.values() ) {
          if ( t.key().equals(subToks[0])) {
            map.put(t, subToks[1]);
            continue outer;
          }
        }
      }

    }
    return map;
  }

  private static void discover(final int durationMs, final IDiscoveryListener listener) throws Exception {
    List<Thread> threads = new ArrayList<>();
    List<InetAddress> allLocalAddresses = MiscUtil.getIpAddresses();
    for (InetAddress localAddress : allLocalAddresses) {
      Runnable r = () -> discoverIndividualAddress(localAddress, durationMs, listener);
      threads.add(new Thread(r));
    }
    for (Thread t : threads)
      t.start();
    for (Thread t : threads)
      t.join();
  }

}
