// Copyright (c) 2020 Silicon Labs. All rights reserved.
package com.silabs.pti.adapter;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class PtiProtocolEncoder extends ProtocolEncoderAdapter {
  private final Charset charset;

  public PtiProtocolEncoder(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void encode(IoSession session,
                     Object message,
                     ProtocolEncoderOutput out) throws Exception {
    CharsetEncoder encoder = charset.newEncoder();
    String value = message == null ? "" : message.toString();
    IoBuffer buf = IoBuffer.allocate(value.length()).setAutoExpand(true);
    buf.putString(value, encoder);
    buf.flip();
    out.write(buf);
  }
}
