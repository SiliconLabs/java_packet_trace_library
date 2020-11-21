// Copyright (c) 2012 Silicon Labs. All rights reserved.

package com.silabs.pti.util;

/**
 * Implementations of this interface are able to split data into multiple 
 * buckets.
 * 
 * Created on Mar 5, 2013 
 * @author timotej
 */
public interface ISplitter extends ICharacterListener {
  
  /**
   * Sets a character listener for a given bucket.
   * 
   *
   * @param 
   * @returns void
   */
  public void setCharacterListener(int bucket, ICharacterListener l);
  
  /**
   * Returns the number of buckets that this splitter splits into.
   * 
   *
   * @param 
   * @returns int
   */
  public int bucketCount();
  
  
  /**
   * When there is no more input, this method flushes the given state.
   * The current state content goes to a best guess destination, but
   * after the flush() there is no more state allowed within the class.
   * 
   */
  public void flush();
}

