// Copyright (c) 2005 Ember Corporation. All rights reserved.

package com.silabs.pti.adapter;

/**
 * Provides utilities for accessing the expect result
 *
 * @author  Guohui Liu (guohui@ember.com)
 */
public class ExpectResponse {
  private final boolean expectSuccess;
  private final String failedReason;
  private final String matchedOutput;
  private final String collectedOutput;

  /**
   * Constructs a ExpectResponse object
   */
  ExpectResponse(final boolean expectSuccess,
                 final String failedReason,
                 final String matchedOutput,
                 final String collectedOutput) {
    this.expectSuccess = expectSuccess;
    this.failedReason = failedReason;
    this.matchedOutput = matchedOutput;
    this.collectedOutput = collectedOutput;
  }

  public boolean succeeded() { return expectSuccess; }
  public boolean failed() { return !expectSuccess; }

  public String failedReason() {
    return failedReason;
  }
  public String matchedOutput() {
    return matchedOutput;
  }
  public String collectedOutput() {
    return collectedOutput;
  }

  @Override
  public String toString() {
    if ( expectSuccess ) {
      return "OK: " + matchedOutput + " / " + collectedOutput;
    } else {
      return "FAIL: " + failedReason + " / " + collectedOutput;
    }
  }
}
