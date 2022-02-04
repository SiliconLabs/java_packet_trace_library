/*******************************************************************************
 * # License
 * Copyright 2021 Silicon Laboratories Inc. www.silabs.com
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

/**
 * Base IDs for supported stack types.
 *
 * @author timotej Created on Feb 4, 2022
 */
public class StackTypeId {

  private StackTypeId() {
  }

  public static final String IP_ID = "IP";
  public static final String BLE_ID = "BLUETOOTH";
  public static final String CONNECT_ID = "CONNECT";
  public static final String ZIGBEE_ID = "ZIGBEE";
  public static final String ZWAVE_ID = "ZWAVE";
  public static final String WISUN_ID = "WISUN";

}
