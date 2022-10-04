/*******************************************************************************
 * # License
 * Copyright 2021 Silicon Laboratories Inc. www.silabs.com
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
package com.silabs.pti;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.silabs.pti.format.IDebugChannelExportOutput;
import com.silabs.pti.log.PtiLog;

/**
 * Class containing output formatters.
 *
 * @author timotej
 *
 */
public class OutputMap<T> {

  private final Map<String, IDebugChannelExportOutput<T>> outputMap;

  public OutputMap() {
    this.outputMap = new HashMap<>();
  }

  /**
   * Retrieves the output from the map by originator.
   *
   * @param originator
   * @return
   */
  public IDebugChannelExportOutput<T> output(final String originator) {
    return outputMap.get(originator);
  }

  /**
   * Inserts the output into the map, keyed by originator.
   *
   * @param originator
   * @param output
   */
  @SuppressWarnings("resource")
  public void put(final String originator, final IDebugChannelExportOutput<T> output) {
    this.outputMap.put(originator, output);
  }

  /**
   * Returns all the output values in this map.
   *
   * @return maps
   */
  public Collection<IDebugChannelExportOutput<T>> values() {
    return outputMap.values();
  }

  /**
   * Clears the map.
   */
  public void closeAndClear() {
    for (final IDebugChannelExportOutput<T> out : outputMap.values()) {
      try {
        out.close();
      } catch (final IOException ioe) {
        PtiLog.error("Failed to close the output.", ioe);
      }
    }
    outputMap.clear();
  }
}
