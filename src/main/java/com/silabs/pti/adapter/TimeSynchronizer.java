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

package com.silabs.pti.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * Time synchronizer will be receiving input events tracking microsecond times
 * from multiple, potentially unsynchronized sources, and
 * will use drift and PC clock to correct those times.
 *
 * This class is NOT THREAD SAFE. TimeShift method should always be called either from
 * a single thread, or synchronized externally.
 *
 * @author Timotej
 * Created on Dec 5, 2017
 * (This logic exists since 2006, but in 2017 it has been moved out of the
 *  LiveDataSource into this separate class to make things clearer and simpler.)
 */
public class TimeSynchronizer {

  public static enum Op {
    NONE(false),
    GLOBAL_ZERO(false),
    ORIGINATOR_ZERO(false),
    ORIGINATOR_ZERO_WITH_CORRECTION(true),
    ZERO_OFFSET(false),
    ZERO_OFFSET_WITH_CORRECTION(true);

    private boolean correction;
    Op(final boolean performedCorrection) {
      this.correction = performedCorrection;
    }
    public boolean performedCorrection() { return correction; }
  }

  // Default PC time supplier supplies millisecond time from System.currentTimeMillis();
  public static LongSupplier DEFAULT_PC_TIME_SUPPLIER = () -> System.currentTimeMillis();

  private final LongSupplier millisecondTimeSupplier;
  private final boolean performDriftCorrection;
  private final long driftThreshold;
  private final long zeroTimeDifferenceThreshold;

  private Long masterPcTimeZero = null;
  private final Map<String, Long> tZeroMap;

  private Op lastOp = Op.NONE;
  private Long lastCorrection = null;

  public TimeSynchronizer(final LongSupplier millisecondTimeSupplier,
                          final boolean performDriftCorrection,
                          final long driftThresholdMicroseconds,
                          final long zeroTimeDifferenceThreshold) {
    this.performDriftCorrection = performDriftCorrection;
    this.millisecondTimeSupplier = millisecondTimeSupplier;
    this.driftThreshold = driftThresholdMicroseconds;
    this.zeroTimeDifferenceThreshold = zeroTimeDifferenceThreshold;
    this.tZeroMap = new HashMap<>();
  }

  /**
   * Returns time zero for the given originator, or Long
   * @param originator
   * @return timezero or null if not set yet.
   */
  public Long timeZero(final String originator) {
    return tZeroMap.get(originator);
  }

  private void setTimeZero(final String originator, final Long time) {
    tZeroMap.put(originator, time);
  }

  /**
   * Returns the last operation performed by the timeShift.
   * @return
   */
  public Op lastOp() { return lastOp; }

  /**
   * Returns the value of time adjust the last time drift correction kicked
   * in. Returns null if there was no drift correction.
   *
   * @return Long
   */
  public Long lastCorrection() { return lastCorrection; }

  /**
   * This method receives the event from a given originator, with a given
   * microsecond time. It returns the actual time that should be used after
   * synchronization.
   *
   * This is time-zeroed, meaning that the first event will be called time zero
   * and a returned value will be 0.
   *
   * This method will call getAsLong() on a time supplier EXACTLY once per
   * each invocation.
   *
   * @param originatorId
   * @param timeInMicroseconds
   * @return
   */
  public long synchronizedTime(final String originatorId,
                               final long timeInMicroseconds) {
    long tMillis = millisecondTimeSupplier.getAsLong();
    Long tZero = timeZero(originatorId);
    Op op;
    if ( tZero == null ) {
      // First event from this originator. Calculate tZero.
      if ( tZeroMap.isEmpty() ) {
        // Wow, first event ever!
        tZero = timeInMicroseconds;
        masterPcTimeZero = tMillis;
        setTimeZero(originatorId, tZero);
        op = Op.GLOBAL_ZERO;
      } else {
        // Not the first ever. Match this against the millisecond supplier
        Long usableT0 = findUsableTimeZero(tMillis, timeInMicroseconds);
        if ( usableT0 == null ) {
          // Nope, not synchronized. Needs to correct,
          long expectedRealMicrosecondTime = (tMillis - masterPcTimeZero)*1000;
          tZero = timeInMicroseconds - expectedRealMicrosecondTime;
          op = Op.ORIGINATOR_ZERO_WITH_CORRECTION;
          lastCorrection = tZero;
        } else {
          // we can trust the timeInMicroseconds! Use same T0.
          tZero = usableT0;
          op = Op.ORIGINATOR_ZERO;
        }
        setTimeZero(originatorId, tZero);
      }
    } else {
      // We have a tZero already. Let's just check for drift.
      if ( performDriftCorrection ) {
        long expectedRealMicrosecondTime = (tMillis - masterPcTimeZero)*1000;
        long microsecondTime = timeInMicroseconds - tZero;
        if ( isDriftTooLarge(microsecondTime,
                             expectedRealMicrosecondTime) ) {
          // We need to adjust shift.

          long newTZero = timeInMicroseconds - expectedRealMicrosecondTime;
          setTimeZero(originatorId, newTZero);
          op = Op.ZERO_OFFSET_WITH_CORRECTION;
          lastCorrection = newTZero - tZero;
          tZero = newTZero;
        } else {
          op = Op.ZERO_OFFSET;
        }
      } else {
        op = Op.ZERO_OFFSET;
      }
    }
    lastOp = op;
    return timeInMicroseconds - tZero;
  }

  // Returns true if the times are within treshold.
  private boolean isDriftTooLarge(final long micros1, final long micros2) {
    return Math.abs(micros1-micros2) >= driftThreshold;
  }

  private Long findUsableTimeZero(final long tMillis, final long micros) {
    long expectedRealMicrosecondTime = (tMillis - masterPcTimeZero)*1000;
    for ( Long t0: tZeroMap.values() ) {
      long expectedEventMicrosecondTime = t0 + expectedRealMicrosecondTime;
      if ( isZeroTimeWithinThreshold(expectedEventMicrosecondTime, micros))
        return t0;
    }
    return null;
  }

  private boolean isZeroTimeWithinThreshold(final long micros1, final long micros2) {
    return Math.abs(micros1-micros2) < zeroTimeDifferenceThreshold;
  }
}
