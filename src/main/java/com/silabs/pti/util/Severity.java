// Copyright 2014 Silicon Laboratories, Inc.

package com.silabs.pti.util;

import java.util.List;

/**
 * Simple enum that deals with severities where you need them.
 *
 * Created on Sep 15, 2014
 * @author timotej
 */
public enum Severity implements ISeverityHolder { 
  // These are sorted by decreasing severity order. Don't change that!!
  ERROR, 
  WARNING, 
  INFO, 
  NONE;
  
  @Override
  public Severity severity() {
    return this;
  }
  
  /**
   * Returns the total severity of an array of severity holders.
   * Total severity is the maximum severity present.
   * 
   *
   * @param holders holders that contains severity.
   * @return Severity
   */
  public static Severity combinedSeverity(List<? extends ISeverityHolder> holders) {
    Severity total = NONE;
    for ( ISeverityHolder h: holders ) {
      if ( h.severity().ordinal() < total.ordinal() ) {
        total = h.severity();
      }
    }
    return total;
  }
  
}