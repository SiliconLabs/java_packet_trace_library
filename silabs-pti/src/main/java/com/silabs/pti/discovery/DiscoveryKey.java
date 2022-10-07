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

/**
 * Standard keys sent by the discovery.
 *
 * @author timotej Created on Dec 28, 2021
 */
public enum DiscoveryKey {
  ADAPTER_TYPE("adapter type"),
  ADAPTER_NICKNAME("adapter nick"),
  ADAPTER_JLINK_SN("adapter jlinksn"),
  ADAPTER_NETIF("adapter netif"),
  BOARD("board"),
  BOARD_LIST("board list"),
  CONNECTION_STATUS("connection status"),
  CONNECTION_ADDRESS("connection address"),
  CONNECTION_TIME("connection time"),
  DEBUG_MODE("debug mode"),
  DISCOVERY_KEY("discovery key"),
  FIRMWARE_TYPE("firmware type"),
  FIRMWARE_VERSION("firmware version"),
  NODE_TYPE("node type"),
  NODE_EUI("node EUI");

  private String key;

  DiscoveryKey(final String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }
}
