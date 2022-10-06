// Copyright (c) 2022 Silicon Labs. All rights reserved.

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
