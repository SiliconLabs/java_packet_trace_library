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

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

/**
 * Various classes for apache mina connectivity.
 * 
 * @author Jing
 */
public class PtiCodecFactory extends DemuxingProtocolCodecFactory {

  private final ProtocolDecoder decoder;
  private final ProtocolEncoder encoder;

  public PtiCodecFactory(final Charset charset) {
    decoder = new PtiProtocolDecoder();
    encoder = new PtiProtocolEncoder(charset);
  }

  @Override
  public ProtocolEncoder getEncoder(final IoSession ioSession) throws Exception {
    return encoder;
  }

  @Override
  public ProtocolDecoder getDecoder(final IoSession ioSession) throws Exception {
    return decoder;
  }
}
