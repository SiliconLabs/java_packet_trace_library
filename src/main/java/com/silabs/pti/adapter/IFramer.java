// Copyright (c) 2004 Ember Corporation. All rights reserved.

package com.silabs.pti.adapter;


/**
 * Interface for framing and unframing messages.
 *
 * @author  Matteo Neale Paris (matteo@ember.com)
 */
public interface IFramer {

  /**
   * Assembles a complete message from the individual bytes supplied by multiple
   * calls to this method.
   *
   * @param nextByte the next byte to append to the current message
   * @return         the complete message (with framing stripped), or
   *                 null if the next byte did not complete the frame.
   */
  byte[] assembleMessage(byte nextByte);

  /**
   * Returns the incomplete message that is currently being assembled from the
   * bytes supplied to the assembleMessage method.
   *
   * @return         the incomplete message (with framing stripped), or
   *                 null if there is no incomplete message being assembled.
   */
  byte[] flushMessage();

  /**
   * Adds framing to the supplied message.
   *
   * @param message  the message to frame.
   * @return         the framed message.
   */
  byte[] frame(byte[] message);

  /**
   * Converts a message from a String representation to byte array.
   *
   * @param message  the message to convert.
   * @return         the message as a byte array.
   */
  byte[] toBytes(String message);

  /**
   * Converts a message from a byte array to a String representation.
   *
   * @param message  the message to convert.
   * @return         the message as a String.
   */
  String toString(byte[] message);

}
