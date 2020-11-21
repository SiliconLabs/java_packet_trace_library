// Copyright (c) 2016 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

/**
 * List of supported ports on adapters, such as ISA2, ISA3, WSTK.
 * Not all hardware supports all ports. This is a collection of
 * all known ports over all known adapters.
 *
 * Created long time ago
 * @author timotej
 */
public enum AdapterPort {
  SERIAL0(4900),
  SERIAL1(4901),
  ADMIN(4902),
  RESET(4903),
  TIMESYNC(4904),
  DEBUG(4905),
  CAMP(4949),
  TELNET(23);

  private int defaultPort;
  AdapterPort(final int defaultPort) {
    this.defaultPort = defaultPort;
  }
  public int defaultPort() { return defaultPort; }

  // Silink prints out some names for these ports from its
  // automap command. These are the mappings.
  public static AdapterPort parseFromSilink(final String name) {
    if ("ADM_CONSOLE".equalsIgnoreCase(name) ) {
      return AdapterPort.ADMIN;
    } else if ( "DCH".equalsIgnoreCase(name) ) {
      return AdapterPort.DEBUG;
    } else if ( "VUART0".equalsIgnoreCase(name) ) {
      return AdapterPort.SERIAL0;
    } else if ( "VCOM0".equalsIgnoreCase(name) ) {
      return AdapterPort.SERIAL1;
    } else {
      return null;
    }
  }
}
