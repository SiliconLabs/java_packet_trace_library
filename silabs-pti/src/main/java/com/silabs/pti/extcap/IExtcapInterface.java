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
package com.silabs.pti.extcap;

/**
 * Abstraction of the extcap interface, to be used by downstream processing to
 * just get access to what's needed.
 * 
 * @author timotej
 *
 */
public interface IExtcapInterface {

  /**
   * This method outputs the string for the extcap interface to read.
   * 
   * @param s
   */
  void extcapPrintln(String s);

  /**
   * This method logs message into the library specific log.
   * 
   * @param s
   */
  void log(String s);
}
