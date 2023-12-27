/*******************************************************************************
 * # License
 * Copyright 2011 Silicon Laboratories Inc. www.silabs.com
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

package com.silabs.pti.decode;

/**
 * All the field names need to ultimately implement this interface.
 *
 * Created on Apr 3, 2012
 * 
 * @author timotej
 */
public interface IFieldName {
  /** The actual name of the field, as used in filtering and decoding */
  public String name();

  /** Returns the endianess for a field */
  public default Endianess endianess() {
    return null;
  }
}
