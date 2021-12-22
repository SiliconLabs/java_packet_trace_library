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

package com.silabs.pti;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.silabs.pti.discovery.DiscoveryProtocol;
import com.silabs.pti.util.MiscUtil;

/**
 * Utilities for discovery.
 *
 * @author timotej
 * Created on Jan 8, 2019
 */
class DiscoveryUtil {

  private static byte[] broadcast = { (byte)0xFF, (byte)0xFF,
                                      (byte)0xFF, (byte)0xFF };
  private static byte[] msg = { '*' };
  private static int count = 0;

  private DiscoveryUtil() {}

  public static int runDiscovery() {
    try {
      discover(500);
      return 0;
    } catch (Exception e) {
      e.printStackTrace();
      return 1;
    }
  }

  private static void discoverIndividualAddress(final InetAddress localAddress,
                                                final int durationMs) {
    String log = localAddress.getHostName();
    try(DatagramSocket socket = new DatagramSocket(0, localAddress)) {
      socket.setSoTimeout(durationMs/10);
      DatagramPacket dp = new DatagramPacket(msg, msg.length);
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
          count++;
          lastDiscoverTime = System.currentTimeMillis();
          printReply(count, incoming);
        } catch (SocketTimeoutException ste) {
          // Not a big deal. Keep going.
        }
      } while ( System.currentTimeMillis() - lastDiscoverTime < durationMs );
    } catch (Exception e) {
      System.err.println(log + ": discovery failed.");
      e.printStackTrace();
    }
  }

  private static void discover(final int durationMs) throws Exception {
    count = 0;
    List<Thread> threads = new ArrayList<>();
    List<InetAddress> allLocalAddresses = MiscUtil.getIpAddresses();
    for ( InetAddress localAddress: allLocalAddresses ) {
      Runnable r = () -> discoverIndividualAddress(localAddress, durationMs);
      threads.add(new Thread(r));
    }
    for ( Thread t: threads ) t.start();
    for ( Thread t: threads ) t.join();
  }

  private static synchronized void printReply(final int ordinal, final DatagramPacket in) {
    System.out.println(ordinal + ": " + in.getAddress().getHostName() + " (" + in.getAddress().getHostAddress() + ")");
  }
}
