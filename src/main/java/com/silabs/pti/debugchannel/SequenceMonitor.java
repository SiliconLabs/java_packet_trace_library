// Copyright (c) 2017 Silicon Labs. All rights reserved.

package com.silabs.pti.debugchannel;

/**
 * This class is responsible for maintaining sequencing. It will keep getting
 * sequence numbers and upon each number it reports problems or success.
 *
 * It can deal with the fact that sequence # can be randomly switching
 * between different byte counts, and will assume that lower bytes follow
 * the sequence.
 *
 * Created on Sep 26, 2017
 * @author Timotej Ecimovic
 */
public class SequenceMonitor {

  /**
   * This is returned by newSequence if this was first sequence.
   */
  public static int OK_FIRST_SEQUENCE = -1;

  /**
   * This is returned by newSequence if match is exact.
   */
  public static int OK_SEQUENCE_MATCHED = -2;

  /**
   * This is returned by the new sequence, if there is potential for missmatch, because bytecount changed
   */
  public static int OK_SEQUENCE_LOW_BYTE_MATCHED = -3;

  private int lastSequence = -1;
  private int lastMask = -1;

  /**
   * This method return value is as follows:
   *    a non-negative value means an error and specifies an expected sequence number.
   *    a negative value means a success, and can be one of OK_ values above.
   * @return int
   */
  public int newSequence(final int sequence, final int mask) {
    int returnValue;
    if ( lastSequence == -1 ) {
      this.lastSequence = sequence;
      this.lastMask = mask;
      returnValue = OK_FIRST_SEQUENCE;
    } else {
      boolean differentMask = (mask != lastMask);
      if ( differentMask ) {
        int actualMask = ( mask > lastMask ? lastMask : mask );
        int ls = lastSequence & actualMask;
        if ( ls == actualMask ) {
          if ( (sequence&actualMask) == 0 )
            returnValue = OK_SEQUENCE_LOW_BYTE_MATCHED;
          else
            returnValue = 0; // we expected zero
        } else {
          if ( (sequence&actualMask) == ls+1 )
            returnValue = OK_SEQUENCE_LOW_BYTE_MATCHED;
          else
            returnValue = ls+1;
        }
      } else {
        int ls = lastSequence & mask;
        if ( ls == mask ) {
          if ( sequence == 0 )
            returnValue = OK_SEQUENCE_MATCHED;
          else
            returnValue = 0; // we expected zero
        } else {
          if ( sequence == ls+1 )
            returnValue = OK_SEQUENCE_MATCHED;
          else
            returnValue = ls+1;
        }
      }
    }
    this.lastSequence = sequence;
    this.lastMask = mask;
    return returnValue;
  }

}
