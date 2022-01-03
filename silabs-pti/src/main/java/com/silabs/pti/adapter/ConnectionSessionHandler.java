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

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class ConnectionSessionHandler extends IoHandlerAdapter {

  @Override
  public void exceptionCaught(final IoSession session, final Throwable arg1) throws Exception {
    session.closeNow();
  }

  @Override
  public void messageReceived(final IoSession session, final Object msg) throws Exception {
    Connection c = (Connection) session.getAttribute("connection");

    if (c != null) {
      c.processIncomingData(System.currentTimeMillis(), ((byte[]) msg).length, (byte[]) msg);
    }
  }
}
