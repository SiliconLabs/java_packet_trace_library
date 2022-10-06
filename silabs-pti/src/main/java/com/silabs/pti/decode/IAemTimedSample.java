// Copyright (c) 2022 Silicon Labs. All rights reserved.

package com.silabs.pti.decode;

/**
 * All timed samples imlement this.
 *
 * @author timotej Created on Apr 19, 2022
 */
public interface IAemTimedSample {
  /**
   * Timestamp in microseconds
   */
  public long timestamp();

}
