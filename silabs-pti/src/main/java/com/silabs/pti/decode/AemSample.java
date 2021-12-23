// Copyright (c) 2017 Silicon Labs. All rights reserved.

package com.silabs.pti.decode;

/**
 * Individual sample of the AEM data, representing a single data point in the
 * same stream.
 */
public class AemSample {
  private final long timestamp;
  private final float current;
  private final float voltage;

  public AemSample(final long timestamp, final float current, final float voltage) {
    this.timestamp = timestamp;
    this.current = current;
    this.voltage = voltage;
  }

  @Override
  public String toString() {
    return "[TimeStamp: " + timestamp + "  Current: " + current + "]";
  }

  /**
   * Timestamp in microseconds \
   */
  public long timestamp() {
    return timestamp;
  }

  /**
   * Current
   */
  public float current() {
    return current;
  }

  /**
   * Voltage
   */
  public float voltage() {
    return voltage;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(current);
    result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
    result = prime * result + Float.floatToIntBits(voltage);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AemSample other = (AemSample) obj;
    if (Float.floatToIntBits(current) != Float.floatToIntBits(other.current))
      return false;
    if (timestamp != other.timestamp)
      return false;
    if (Float.floatToIntBits(voltage) != Float.floatToIntBits(other.voltage))
      return false;
    return true;
  }
}
