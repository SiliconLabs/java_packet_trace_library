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

package com.silabs.pti.util;

/**
 * Simple listener that can be notified of a sequence of bytes received.
 *
 * Created on Jun 12, 2008
 * 
 * @author Timotej (timotej@ember.com)
 * @since 4.6
 */
@FunctionalInterface
public interface ICharacterListener {
  /**
   * This method is called whenever a new array of bytes is received.
   */
  public void received(byte[] ch, int offset, int len);
}
