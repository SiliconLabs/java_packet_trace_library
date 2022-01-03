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
 * Extends the basic IConnection with debug channel functionality.
 *
 * Created on Feb 13, 2017
 * 
 * @author timotej
 */
public interface IDebugConnection extends IConnection {

  /**
   * Pause this connection for a specified amount of milliseconds.
   */
  public void pauseFor(int milliseconds);

  /**
   * Sets the problem listener.
   *
   * @param l
   */
  public void setConnectionProblemListener(IConnectionProblemListener l);

}
