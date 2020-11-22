// Copyright (c) 2018 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

/**
 * Listener for problems in connection. Extracted from earlier
 * IDebugListener.
 *
 * @author Timotej
 * Created on Mar 23, 2018
 */
public interface IConnectionProblemListener {

  /**
   * If some problem happened in the underlying connectivity, which
   * user should be alerted to, then this is the method to tall.
   */
  public void reportProblem(String message, Exception ex);

}
