// Copyright (c) 2021 Silicon Labs. All rights reserved.

package com.silabs.pti.decode;

/**
 * Interface that extends field name with length.
 *
 * @author timotej Created on May 25, 2021
 */
public interface IFieldNameWithLength extends IFieldName {

  /**
   * Length of a given field. -1 means variable.
   * 
   * @return The length in bytes.
   */
  public int length();
}
