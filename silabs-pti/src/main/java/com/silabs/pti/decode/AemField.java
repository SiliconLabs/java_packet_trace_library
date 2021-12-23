// Copyright (c) 2021 Silicon Labs. All rights reserved.

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