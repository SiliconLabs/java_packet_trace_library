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
