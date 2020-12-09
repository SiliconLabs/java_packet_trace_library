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

package com.silabs.pti.splitter;

import com.silabs.pti.util.ICharacterListener;

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
   * @param bucket Bucket identifier.
   * @param l listener
   */
  public void setCharacterListener(int bucket, ICharacterListener l);
  
  /**
   * Returns the number of buckets that this splitter splits into.
   * 
   * @return int
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

