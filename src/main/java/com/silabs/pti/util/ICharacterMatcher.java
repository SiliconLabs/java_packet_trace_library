// Copyright (c) 2013 Silicon Labs. All rights reserved.

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
