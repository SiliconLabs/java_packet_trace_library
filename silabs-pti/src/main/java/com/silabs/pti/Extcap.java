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

package com.silabs.pti;

/**
 * When this jar file is used within wireshark, the extcap wireshark
 * functionality will pass 'extcap' as the first argument. If that happens then
 * we end up here.
 * 
 * @author timotej
 *
 */
public class Extcap {

  /**
   * Execute extcap function. Args will contain 'extcap' as the first argument.
   * 
   * @param args
   * @return
   */
  public static final int run(String[] args) {
    String extcapLocation = System.getenv("EXTCAP_LOC");
    System.out.println("Extcap. Log location: " + (extcapLocation == null ? "unknown (using stdout)" : extcapLocation));
    return 0;
  }

}
