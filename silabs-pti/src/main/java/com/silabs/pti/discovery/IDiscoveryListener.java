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

import java.net.DatagramPacket;

/**
 * Classes of this type can listen to discovery replies.
 * 
 * @author timotej
 */
public interface IDiscoveryListener {
  /**
   * This method is called when discovery happens.
   * 
   * @param ordinal
   * @param in
   */
  public void discovery(int ordinal, DatagramPacket in);
}
