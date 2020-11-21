// Copyright (c) 2020 Silicon Labs. All rights reserved.
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

    if (c != null){
      c.processIncomingData(System.currentTimeMillis(), ((byte[])msg).length, (byte[])msg);
    }
  }
}
