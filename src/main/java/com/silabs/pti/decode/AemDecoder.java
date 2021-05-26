// Copyright (c) 2021 Silicon Labs. All rights reserved.

package com.silabs.pti.decode;

import com.silabs.pti.util.MiscUtil;

/**
 * Raw decoder of AEM packets.
 *
 * @author timotej
 * Created on May 26, 2021
 */
public class AemDecoder {

  private final long time;
  private final byte[] contents;
  private int index;
  private int sampleCount = 0;
  private int sampleRate;
  private float voltage;

  public AemDecoder(final long time, final byte [] contents) {
    this.time = time;
    this.contents = contents;
  }


  // Little endian int decoding.
  private int decode(final int length) {
    index += length;
    return MiscUtil.byteArrayToInt(contents, index, length, false);
  }

  // Little endian float decoding.
  private float decodeFloat(final int length) {
    index += length;
    return MiscUtil.byteArrayToFloat(contents, index, length, false).floatValue();
  }

  private boolean noBytesLeft(final int n) {
    return contents.length < index + n;
  }

  private AemSample firstSample() {
    index += AemField.version.length();
    if ( noBytesLeft(AemField.sampleRate.length())) return null;
    sampleRate = decode(AemField.sampleRate.length());
    index += AemField.sampleRate.length();
    index += AemField.sampleBufferSize.length();
    index += AemField.sampleBufferSequenceNumber.length();
    index += AemField.reservedConfig.length();
    if ( noBytesLeft(AemField.voltage.length())) return null;
    voltage = decodeFloat(AemField.voltage.length());
    index += AemField.reservedData.length();
    index += AemField.status.length();
    return subsequentSample();
  }

  private AemSample subsequentSample() {
    long sampleT = time + ( 1000000 * sampleCount ) / sampleRate;
    if ( noBytesLeft(AemField.current.length())) return null;
    float current = decodeFloat(AemField.current.length());
    sampleCount++;
    return new AemSample(sampleT, current, voltage);
  }

  /**
   * This method returns the next AemSample from the packet, or null
   * if the end was reached.
   *
   * @return AemSample
   */
  public AemSample nextSample() {
    if ( index == 0 ) {
      return firstSample();
    } else {
      return subsequentSample();
    }
  }

}
