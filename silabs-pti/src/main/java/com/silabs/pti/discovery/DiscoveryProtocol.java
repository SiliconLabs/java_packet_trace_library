/*******************************************************************************
 * # License
 * Copyright 2015 Silicon Laboratories Inc. www.silabs.com
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

/**
 * Static class containing constants for discovery.
 *
 * Created on Dec 17, 2015
 * 
 * @author timotej
 */
public class DiscoveryProtocol {

  public static final int UDP_PORT = 4920;
  public static final int RECEIVE_LENGTH = 500;
  public static final int DEFAULT_BACKOFF = 10;
  public static final byte[] DISCOVERY_MESSAGE = { '*' };
  public static final String BROADCAST_ADDRESS = "255.255.255.255";

  private DiscoveryProtocol() {
  }
}
