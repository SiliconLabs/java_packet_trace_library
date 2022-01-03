// Copyright 2015 Silicon Laboratories, Inc.

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
