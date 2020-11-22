// Copyright (c) 2016 Silicon Labs. All rights reserved.

package com.silabs.pti.adapter;

/**
 * Extends the basic IConnection with debug channel functionality.
 *
 * Created on Feb 13, 2017
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
