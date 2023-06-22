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

import java.util.stream.Stream;

/**
 * A window of samples.
 *
 * @author timotej Created on Apr 19, 2022
 */
public interface IAemLogicAnalyzerSampleWindow {

  /**
   * Time of the first recorded sample in this window.
   *
   */
  public long startTime();

  /**
   * Time of the last recorded sample in this window.
   *
   */
  public long endTime();

  /**
   * Number of samples in this class.
   *
   * @return
   */
  public int size();

  /**
   * Returns i-th sample.
   *
   * @param i
   * @return
   */
  public IAemLogicAnalyzerSample sample(int i);

  /**
   * Returns a stream of all samples.
   *
   * @return
   */
  public Stream<IAemLogicAnalyzerSample> stream();
}
