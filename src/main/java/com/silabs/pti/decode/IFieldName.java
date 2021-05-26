// Copyright (c) 2011 Ember Corporation. All rights reserved.

package com.silabs.pti.decode;

/**
 * All the field names need to ultimately implement this interface.
 *
 * Created on Apr 3, 2012
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
