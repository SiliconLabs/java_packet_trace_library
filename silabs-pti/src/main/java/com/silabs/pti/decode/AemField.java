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

package com.silabs.pti.decode;

/**
 * This enum contains the fields and sizes for the AEM decoding.
 *
 * @author timotej Created on May 26, 2021
 */
public enum AemField implements IFieldNameWithLength {
  version(2),
  // Config
  sampleRate(4),
  sampleBufferSize(2),
  sampleBufferSequenceNumber(2),
  reservedConfig(8),
  // Data info
  voltage(4),
  reservedData(8),
  // Status
  status(4),
  // Data
  current(4),
  currentRaw(-1);

  private final int length;

  AemField(final int length) {
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }

}