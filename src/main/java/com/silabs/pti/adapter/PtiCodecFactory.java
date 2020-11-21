// Copyright (c) 2020 Silicon Labs. All rights reserved.
package com.silabs.pti.adapter;
import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

public class PtiCodecFactory extends DemuxingProtocolCodecFactory {
  
  private ProtocolDecoder decoder;
  private ProtocolEncoder encoder;

  public PtiCodecFactory(Charset charset) {
    decoder = new PtiProtocolDecoder();
    encoder = new PtiProtocolEncoder(charset);
  }

  public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
    return encoder;
  }

  public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
    return decoder;
  }
}
