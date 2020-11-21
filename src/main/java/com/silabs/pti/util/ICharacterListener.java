//Copyright 2008 Ember Corporation. All rights reserved.

package com.silabs.pti.util;

/**
 * Simple listener that can be notified of a sequence of bytes received.
 *
 * Created on Jun 12, 2008
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
