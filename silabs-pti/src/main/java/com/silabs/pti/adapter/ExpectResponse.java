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

/**
 * Provides utilities for accessing the expect result
 *
 * @author Guohui Liu (guohui@ember.com)
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

  public boolean succeeded() {
    return expectSuccess;
  }

  public boolean failed() {
    return !expectSuccess;
  }

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
    if (expectSuccess) {
      return "OK: " + matchedOutput + " / " + collectedOutput;
    } else {
      return "FAIL: " + failedReason + " / " + collectedOutput;
    }
  }
}
