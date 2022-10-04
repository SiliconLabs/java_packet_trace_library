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

package com.silabs.pti.adapter;

/**
 * An IFramer for Debug Channel.
 *
 * See specs at:
 *
 * https://confluence.silabs.com/display/LegacySW/BinaryDebugProtocol
 *
 * @author Matteo Neale Paris (matteo@ember.com)
 * @author Guohui Liu (guohui@ember.com)
 */
public class DebugChannelFramer extends BinaryFramer {

  private static final int READING_START = 0;
  private static final int READING_LENGTH_LSB = 1;
  private static final int READING_LENGTH_MSB = 2;
  private static final int READING_MESSAGE = 3;
  private static final int READING_END = 4;

  private static final int OPEN_BRACKET = 91;
  private static final int CLOSE_BRACKET = 93;

  private byte[] message;
  private int state = READING_START;
  private int index;
  private final int sizeError; // Amount by which the size bytes are incorrect.
  private int lengthLSB = 0;
  private final boolean twoByteLength;

  public DebugChannelFramer(final boolean twoByteLength) {
    this.twoByteLength = twoByteLength;
    if (twoByteLength) {
      sizeError = -2;
    } else {
      sizeError = 5;
    }
  }

  @Override
  public byte[] assembleMessage(final byte nextByte) {
    switch (state) {
    case READING_START:
      if (nextByte == OPEN_BRACKET)
        state = READING_LENGTH_LSB;
      break;
    case READING_LENGTH_LSB:
      if (twoByteLength) {
        lengthLSB = nextByte & 0xFF;
        state = READING_LENGTH_MSB;
      } else {
        message = new byte[(nextByte & 0xFF) + sizeError];
        index = 0;
        state = READING_MESSAGE;
      }
      break;
    case READING_LENGTH_MSB:
      message = new byte[((nextByte & 0xFF) << 8) + lengthLSB + sizeError];
      index = 0;
      state = READING_MESSAGE;
      break;
    case READING_MESSAGE:
      message[index] = nextByte;
      index++;
      if (index == message.length)
        state = READING_END;
      break;
    case READING_END:
      state = READING_START;
      if (nextByte == CLOSE_BRACKET)
        return message;
      else
        return null;
    default:
      return null;
    }
    return null;
  }

  @Override
  public byte[] flushMessage() {
    switch (state) {
    case READING_MESSAGE:
    case READING_END:
      state = READING_START;
      return message;
    default:
      state = READING_START;
      return null;
    }
  }

  /**
   * Add the framing to a message.
   */
  @Override
  public byte[] frame(final byte[] msg) {
    byte[] temp;
    int extra = (twoByteLength ? 1 : 0);
    temp = new byte[msg.length + 3 + extra];
    temp[0] = OPEN_BRACKET;
    temp[1] = (byte) (msg.length - sizeError);
    temp[2] = (byte) ((msg.length - sizeError) >> 8);
    System.arraycopy(msg, 0, temp, 2 + extra, msg.length);
    temp[msg.length + 2 + extra] = CLOSE_BRACKET;
    return temp;
  }
}
