/*******************************************************************************
 * # License
 * Copyright 2012 Silicon Laboratories Inc. www.silabs.com
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
 * Sometimes field enums are sparse. If that is the case, then make enum
 * implement this method, and ordinal will no longer be used.
 * 
 * Created on Jul 5, 2013
 * 
 * @author timotej
 */
public interface ISparseFieldEnum {
  /** Title of the field */
  public String title();

  public int id();
}
