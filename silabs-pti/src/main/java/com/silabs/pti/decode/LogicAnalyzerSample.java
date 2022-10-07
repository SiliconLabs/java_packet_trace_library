// Copyright (c) 2022 Silicon Labs. All rights reserved.

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
