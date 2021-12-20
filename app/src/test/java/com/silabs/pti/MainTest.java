// Copyright (c) 2020 Silicon Labs. All rights reserved.

package com.silabs.pti;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static spark.Spark.get;
import static spark.Spark.port;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class MainTest {
  @Test
  void testCommandLineHelp() {
    Main main = new Main(new String[] { "-?" });
    assertNotNull(main);
    assertTrue(main.cli().shouldExit());
    assertEquals(0, main.cli().exitCode());
  }

  @Test
  void testCommandLine() {
    Main main = new Main(new String[] { "" });
    assertNotNull(main);
    assertTrue(main.cli().shouldExit());
    assertEquals(1, main.cli().exitCode());
  }

  @Test
  void testSingleNodeCapture() {
    final int totalTestPorts = 200;
    final int testPort = 2000;
    final String filename = "test_captures.log";
    final int messageCount = 100;
    final String testMsg = "12345678";

    ArrayList<SocketConnectionHandler> socketServerList = new ArrayList<SocketConnectionHandler>();

    // delete old test files
    File file = new File(".");
    for (File f : file.listFiles()) {
      if (f.getName().startsWith("test_captures")) {
        System.out.println("Deleting file: " + f.getName());
        f.delete();
      }
    }

    // start http server
    get("/hello", (request, response) -> "Hello World!");
    get("/broadcast", (request, response) -> {
      System.out.println("Broadcasting message now...");
      for (SocketConnectionHandler sch : socketServerList) {
        sch.send(testMsg);
      }
      return socketServerList.size();
    });
    System.out.println("Listening for HTTP connection on port " + port());

    // start socket servers
    StringBuffer testPortArgs = new StringBuffer();
    for (int port = testPort; port <= testPort + totalTestPorts; port++) {
      SocketConnectionHandler sch = new SocketConnectionHandler(port);
      Thread t = new Thread(sch);
      t.start();
      socketServerList.add(sch);
      testPortArgs.append(port + ",");
    }

    String[] cmd = new String[] { "-testPort=" + testPortArgs.toString(),
                                  "-out=" + filename };
    MainRunner mr = new MainRunner(cmd);
    Thread t = new Thread(mr);
    t.start();

    try {
      Thread.sleep(1000);
      for (int i = 0; i < messageCount; i++) {
        broadcastMessage();
      }
    } catch (Exception e) {
    }

    // check capture results
    ArrayList<Executable> execs = new ArrayList<Executable>();
    for (int port = testPort; port <= testPort + totalTestPorts; port++) {
      try {
        String captureLog = String.format("test_captures_%s.log", port);
        List<String> lines = Files.readAllLines(Paths.get(captureLog));
        execs.add(() -> assertEquals(messageCount,
                                     lines.stream()
                                         .filter(x -> x.contains(testMsg))
                                         .count(),
                                     captureLog));

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    assertAll(execs);

    // clean up old files
    file = new File(".");
    for (File f : file.listFiles()) {
      if (f.getName().startsWith("test_captures")) {
        f.delete();
      }
    }
  }

  private void broadcastMessage() {
    // create a request
    try {
      getHTML("http://localhost:4567/broadcast");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static String getHTML(String urlToRead) throws Exception {
    StringBuilder result = new StringBuilder();
    URL url = new URL(urlToRead);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn
        .getInputStream()))) {
      for (String line; (line = reader.readLine()) != null;) {
        result.append(line);
      }
    }
    return result.toString();
  }
}
