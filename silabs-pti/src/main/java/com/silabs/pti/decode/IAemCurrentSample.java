// Copyright (c) 2022 Silicon Labs. All rights reserved.

package com.silabs.pti.decode;

/**
 * Interface that represents a single current sample.
 *
 * @author timotej
 * Created on Apr 18, 2022
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
