// Copyright (c) 2012 Silicon Labs. All rights reserved.

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
