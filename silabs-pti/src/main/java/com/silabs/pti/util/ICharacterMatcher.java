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
 * If you want an object that matches one or the other string, you can use
 * this interface.
 * 
 * Created on Mar 6, 2013 
 * @author Timotej Ecimovic
 */
public interface ICharacterMatcher {

  /** Returns true if character ch matches this object at index */
  public boolean isByteAt(int index, byte ch);
  
  /** Length of the matcher */
  public int length();
}
