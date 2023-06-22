/*******************************************************************************
 * # License
 * Copyright 2017 Silicon Laboratories Inc. www.silabs.com
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
