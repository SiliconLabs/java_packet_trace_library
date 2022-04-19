// Copyright (c) 2022 Silicon Labs. All rights reserved.

package com.silabs.pti.decode;

import java.util.stream.Stream;

/**
 * A window of samples.
 *
 * @author timotej
 * Created on Apr 19, 2022
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
