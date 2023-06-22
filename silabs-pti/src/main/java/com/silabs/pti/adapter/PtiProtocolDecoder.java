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

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * Various classes for apache mina connectivity.
 * 
 * @author Jing
 */
public class PtiProtocolDecoder extends ProtocolDecoderAdapter {
  @Override
  public void decode(final IoSession session, final IoBuffer in, final ProtocolDecoderOutput out) throws Exception {
    final byte[] array = new byte[in.remaining()];
    in.get(array);
    out.write(array);
  }
}
