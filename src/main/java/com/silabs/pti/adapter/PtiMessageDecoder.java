// Copyright (c) 2020 Silicon Labs. All rights reserved.
package com.silabs.pti.adapter;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderAdapter;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

public class PtiMessageDecoder extends MessageDecoderAdapter {

  @Override
  public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
    return MessageDecoderResult.OK;
  }

  @Override
  public MessageDecoderResult decode(IoSession session,
                                     IoBuffer in,
                                     ProtocolDecoderOutput out) throws Exception {
    byte[] array = new byte[in.remaining()];
    in.get(array);
    out.write(array);
    return MessageDecoderResult.OK;
  }
}
