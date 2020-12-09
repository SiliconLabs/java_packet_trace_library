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

package com.silabs.pti.adapter;

import java.io.IOException;

/**
 * Tag capability. It describes the adapter as being able to do
 * time synchronization. All ethernet adapters do this.
 *
 * Created on Aug 8, 2006
 * @author Timotej (timotej@ember.com)
 */
public class TimeSync {

  /**
   * Executes the time server logic.
   * If timeServer is null, then we set the given adapter to be
   * a time server.
   * If timeServer is not null, then we set the given adapter to
   * be a time client to the passed server.
   * @param admin TODO
   * @param isConnected TODO
   * @param adapterName TODO
   * @param successRegex TODO
   * @param canTimeSync TODO
   * @param timeServerPresent
   * @param timeServerIp TODO
   * @throws IOException
   *
   * @return Returns true if the time synchronization resulted in
   *  the adapter becoming a client.
   */
  public static boolean synchronizeTime(final IConnection admin,
                                        final boolean isConnected,
                                        final String adapterName,
                                        final String successRegex,
                                        final boolean canTimeSync,
                                        final boolean timeServerPresent,
                                        final String timeServerIp)
  throws IOException {
    boolean isClient = false;

    if (!isConnected) {
        throw new IOException("Failed to connect to debug port.");
    }

    // Let's deal with time server/client stuff.
    if ( canTimeSync ) {
      if (!timeServerPresent) { // Here we set the time server
        boolean success = timeServer(admin, successRegex, 2000);
        if ( !success )
          throw new IOException("Failed to set  "
                                + adapterName
                                + " to be time server.");
      } else { // and here we set a time client
        if ( timeServerIp == null || !timeClient(admin,
                                       timeServerIp,
                                       2000) ) {
          throw new IOException("Failed to set "
              + adapterName
              + " to use time server at "
              + timeServerIp);
        }
        isClient = true;
      }
    }
    return isClient;
  }

  /**
   * Sends a "time server" command to the backchannel and returns its IP
   * address.
   *
   * @return the IP address of the backchannel, or null if failed.
   */
  private static boolean timeServer(final IConnection admin,
                                    final String successRegex,
                                    final int timeout) {
    ExpectConnection ec = new ExpectConnection(admin);
    String msg = ec.expect("time server", successRegex, timeout, false);
    if ( msg == null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Sets the backchannel into time client mode.
   *
   * @param serverIP
   *          the IP address of the time server.
   * @return true if successful.
   */
  private static boolean timeClient(final IConnection admin,
                                    final String serverIP,
                                    final int timeout) {
    ExpectConnection ec = new ExpectConnection(admin);
    String msg = ec.expect("time client " + serverIP,
                           ".*" + serverIP + ".*",
                           timeout,
                           false);
    return ( msg != null);
  }
}
