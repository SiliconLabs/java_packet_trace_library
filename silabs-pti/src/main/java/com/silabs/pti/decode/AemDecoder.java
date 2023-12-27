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

import com.silabs.na.pcap.util.ByteArrayUtil;
import com.silabs.pti.util.MiscUtil;

/**
 * Raw decoder of AEM packets.
 *
 * @author timotej Created on May 26, 2021
 */
public class AemDecoder {

  private final long time;
  private final byte[] contents;
  private int index;
  private int sampleCount = 0;
  private int sampleRate;
  private float voltage;

  public AemDecoder(final long time, final byte[] contents) {
    this.time = time;
    this.contents = contents;
  }

  // Little endian int decoding.
  private int decode(final int length) {
    final int offset = index;
    final int val = ByteArrayUtil.byteArrayToInt(contents, offset, length, false);
    index += length;
    return val;
  }

  // Little endian float decoding.
  private float decodeFloat(final int length) {
    final float val = MiscUtil.byteArrayToFloat(contents, index, length, false).floatValue();
    index += length;
    return val;
  }

  private boolean notEnoughBytesLeft(final int requiredBytes) {
    return contents.length < index + requiredBytes;
  }

  private AemSample firstSample() {
    index += AemField.version.length();
    if (notEnoughBytesLeft(AemField.sampleRate.length()))
      return null;
    sampleRate = decode(AemField.sampleRate.length());
    index += AemField.sampleBufferSize.length();
    index += AemField.sampleBufferSequenceNumber.length();
    index += AemField.reservedConfig.length();
    if (notEnoughBytesLeft(AemField.voltage.length()))
      return null;
    voltage = decodeFloat(AemField.voltage.length());
    index += AemField.reservedData.length();
    index += AemField.status.length();
    return subsequentSample();
  }

  private AemSample subsequentSample() {
    final long sampleT = time + (1000000 * sampleCount) / sampleRate;
    if (notEnoughBytesLeft(AemField.current.length()))
      return null;
    final float current = decodeFloat(AemField.current.length());
    sampleCount++;
    return new AemSample(sampleT, current, voltage);
  }

  /**
   * This method returns the next AemSample from the packet, or null if the end
   * was reached.
   *
   * @return AemSample
   */
  public AemSample nextSample() {
    if (index == 0) {
      return firstSample();
    } else {
      return subsequentSample();
    }
  }

}
