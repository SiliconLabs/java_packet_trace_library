// Copyright (c) 2014 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

/**
 * Classes of this type are able to map logical backchannel ports
 * to different physical ports than 4901, etc.
 *
 * Created on Nov 22, 2014
 * @author timotej
 */
public interface IBackchannelPortMapper {

  /**
   * Default mapper simply uses the port.defaultPort() values.
   */
  public static IBackchannelPortMapper DEFAULT_MAPPER
    = new IBackchannelPortMapper() {

      @Override
      public int port(final AdapterPort logicalPort) {
        return logicalPort.defaultPort();
      }
    };

  /**
   * This method returns the TCP port for a given backchannel logical port.
   *
   *
   * @param logicalPort
   * @return int
   */
  public int port(AdapterPort logicalPort);
}
