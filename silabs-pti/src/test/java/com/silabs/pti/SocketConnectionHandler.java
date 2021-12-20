package com.silabs.pti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Socket server responsible for sending test messages.
 * 
 * @author jiteng
 *
 */
public class SocketConnectionHandler implements Runnable {
  private Socket clientSocket;
  ServerSocket server = null;
  PrintWriter out = null;
  BufferedReader in = null;
  private int port = 0;

  public SocketConnectionHandler(int p) {
    port = p;
  }

  @Override
  public void run() {
    try {
      server = new ServerSocket(this.port);
      this.clientSocket = server.accept();

      out = new PrintWriter(clientSocket.getOutputStream(), true);

      in = new BufferedReader(new InputStreamReader(clientSocket
          .getInputStream()));
      
      System.out.println("Listening for connection on port " + this.port);
      out.println("Connection established");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void send(String msg) {
    if (out != null)  {
      out.println(msg);
    }
  }
}
