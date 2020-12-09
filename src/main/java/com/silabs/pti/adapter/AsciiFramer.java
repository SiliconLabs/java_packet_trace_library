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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.silabs.pti.util.LineTerminator;

/**
 * Treats incoming messages as US-ASCII framed by newlines (any variety).
 * Uses "\r\n" to frame new messages.
 *
 * @author  Matteo Neale Paris (matteo@ember.com)
 */
public class AsciiFramer implements ILineTerminatingFramer {

  private LineTerminator lineTerminator;
  private static final String CHARSET = "US-ASCII";

  private List<Byte> message;
  private boolean ignoreNewline = false;
  private Charset cs;

  public AsciiFramer() {
    message = new ArrayList<Byte>();
    lineTerminator = LineTerminator.CRLF;
    try {
      cs = Charset.forName(CHARSET);
    } catch (Throwable t) {
      throw new IllegalStateException("Java error. Not supporting " + CHARSET);
    }
  }

  @Override
  public byte[] assembleMessage(final byte nextByte) {
    if (nextByte == '\n' && ignoreNewline) {
      ignoreNewline = false;
      return null;
    }
    if (nextByte == '\r') {
      ignoreNewline = true;
    } else {
      ignoreNewline = false;
    }
    // The right thing to do here is to check nextByte against toBytes(NEWLINE)
    // but that seems like an excessive amount of extra work.
    if (nextByte == '\r' || nextByte == '\n') {
      byte[] ints = new byte[message.size()];
      for(int i = 0; i < ints.length; i++)
        ints[i] = message.get(i);
      message = new ArrayList<Byte>();
      return ints;
    }
    message.add(nextByte);
    return null;
  }

  @Override
  public byte[] flushMessage() {
    if (message.size() == 0) {
      // Don't return an empty message when flushing.
      return null;
    }
    byte[] ints = new byte[message.size()];
    for(int i = 0; i < ints.length; i++)
      ints[i] = message.get(i);
    message = new ArrayList<Byte>();
    return ints;
  }

  @Override
  public byte[] frame(final byte[] message) {
    return toBytes(toString(message) + lineTerminator.terminator());
  }

  @Override
  public byte[] toBytes(final String message) {
    if (message == null)
      return null;
    else
      return message.getBytes(cs);
  }

  @Override
  public String toString(final byte[] message) {
    if (message == null)
      return null;
    else
      return new String(message, cs);
  }

  /** Returns the line terminator that is used in output framing. */
  @Override
  public LineTerminator lineTerminator() { return lineTerminator; }

  /** Sets the line terminator that is used in output framing. */
  @Override
  public void setLineTerminator(final LineTerminator lt) {
    this.lineTerminator = lt;
  }
}
