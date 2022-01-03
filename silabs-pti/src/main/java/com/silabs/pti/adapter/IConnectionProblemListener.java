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
 * Listener for problems in connection. Extracted from earlier IDebugListener.
 *
 * @author Timotej Created on Mar 23, 2018
 */
public interface IConnectionProblemListener {

  /**
   * If some problem happened in the underlying connectivity, which user should be
   * alerted to, then this is the method to tall.
   */
  public void reportProblem(String message, Exception ex);

}
