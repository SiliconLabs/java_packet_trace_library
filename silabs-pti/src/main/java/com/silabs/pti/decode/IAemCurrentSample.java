/*******************************************************************************
 * # License
 * Copyright 2022 Silicon Laboratories Inc. www.silabs.com
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

package com.silabs.pti.decode;

/**
 * Interface that represents a single current sample.
 *
 * @author timotej Created on Apr 18, 2022
 */
public interface IAemCurrentSample extends IAemTimedSample {

  /**
   * Current value
   */
  public float current();

  /**
   * Voltage value
   */
  public float voltage();

}
