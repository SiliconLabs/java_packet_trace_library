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
 * Single class representing the state of the logic analyzer.
 *
 * @author timotej Created on Apr 19, 2022
 */
public class LogicAnalyzerSample implements IAemLogicAnalyzerSample {

  private final long t;
  private final int channelState;

  public LogicAnalyzerSample(final long t, final int channelState) {
    this.t = t;
    this.channelState = channelState;
  }

  @Override
  public long timestamp() {
    return t;
  }

  @Override
  public int channelState() {
    return channelState;
  }
}
