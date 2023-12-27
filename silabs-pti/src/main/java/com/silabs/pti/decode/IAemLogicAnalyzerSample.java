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

package com.silabs.pti.decode;

/**
 * Single sample from a logic analyzer.
 *
 * @author timotej Created on Apr 19, 2022
 */
public interface IAemLogicAnalyzerSample extends IAemTimedSample {

  /**
   * Returns the full state of the channels. Least significant bit, is by
   * convention called "channel zero".
   *
   * @return state of channels, one bit per channel.
   */
  public int channelState();
}
