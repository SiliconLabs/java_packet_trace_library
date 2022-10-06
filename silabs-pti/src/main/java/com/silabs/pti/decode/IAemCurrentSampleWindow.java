// Copyright (c) 2017 Silicon Labs. All rights reserved.

package com.silabs.pti.decode;

import java.util.stream.Stream;

/**
 * A window of sampler retrieved from resampling.
 *
 * @author Timotej Created on Nov 16, 2017
 */
public interface IAemCurrentSampleWindow {

  /**
   * Returns the count of all samples.
   * 
   * @return
   */
  public int size();

  /**
   * Returns a full information of i-th sample.
   * 
   * @param i
   * @return
   */
  public IAemCurrentSample sample(int i);

  /**
   * Returns the stream of samples.
   * 
   * @return stream.
   */
  public Stream<IAemCurrentSample> stream();

}
