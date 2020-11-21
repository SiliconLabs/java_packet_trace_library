// Copyright (c) 2019 Silicon Labs. All rights reserved.

package com.silabs.pti.discover;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.silabs.pti.CommandLine;
import com.silabs.pti.util.NetworkUtils;

/**
 * Utilities for discovery.
 *
 * @author timotej
 * Created on Jan 8, 2019
 */
public class DiscoveryUtil {

  private static byte[] broadcast = { (byte)0xFF, (byte)0xFF,
                                      (byte)0xFF, (byte)0xFF };
  private static byte[] msg = { '*' };
  private static int count = 0;

  private DiscoveryUtil() {}

  public static int runDiscovery(final CommandLine cli) {
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

      byte[] inBuff = new byte[500];
      DatagramPacket incoming = new DatagramPacket(inBuff, inBuff.length);
      incoming.setPort(4920);
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
    List<InetAddress> allLocalAddresses = NetworkUtils.getIpAddresses();
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
