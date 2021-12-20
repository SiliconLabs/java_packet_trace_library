// Copyright (c) 2020 Silicon Labs. All rights reserved.

package com.silabs.pti;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
