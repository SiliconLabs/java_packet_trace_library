// Copyright (c) 2020 Silicon Labs. All rights reserved.
package com.silabs.pti.adapter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class PtiProtocolDecoder extends ProtocolDecoderAdapter {
  @Override
  public void decode(IoSession session,
                     IoBuffer in,
                     ProtocolDecoderOutput out) throws Exception {
    byte[] array = new byte[in.remaining()];
    in.get(array);
    out.write(array);
  }
}
