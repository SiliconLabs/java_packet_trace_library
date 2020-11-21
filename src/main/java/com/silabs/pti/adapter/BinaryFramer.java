// Copyright (c) 2004 Ember Corporation. All rights reserved.

package com.silabs.pti.adapter;

import com.silabs.pti.util.FrameUtil;


/**
 * Base class for binary framing schemes.  Implements the
 * string conversion methods of the {@link IFramer} interface
 * so that subclasses don't have to.  The string form of
 * messages is hex bytes separated by spaces.
 * 
 * @author  Matteo Neale Paris (matteo@ember.com)
 */
public class BinaryFramer implements IFramer {

  @Override
  public byte[] assembleMessage(byte nextByte) {
    byte[] message = new byte[1];
    message[0] = nextByte;
    return message;
  }
  
  @Override
  public byte[] flushMessage() {
    return null;
  }
  
  @Override
  public byte[] frame(byte[] message) {
    return message;
  }

  @Override
  public byte[] toBytes(String message) {
    if (message == null)
      return null;
    String[] tokens = message.split("\\s");
    byte[] result = new byte[tokens.length];
    for(int i = 0; i < result.length; i++) {
      try { 
        result[i] = Byte.parseByte(tokens[i], 16);
      } catch(NumberFormatException e) {
        return null;
      }
    }
    return result;
  }

  @Override
  public String toString(byte[] message) {
    return FrameUtil.formatByteArray(message, true);
  }

}
