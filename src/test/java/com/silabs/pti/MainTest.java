// Copyright (c) 2020 Silicon Labs. All rights reserved.

package com.silabs.pti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainTest {
    @Test void testCommandLineHelp() {
        Main main = new Main(new String[] {"-?"});
        assertNotNull(main);
        assertTrue(main.cli().shouldExit() );
        assertEquals(0, main.cli().exitCode());
    }
    @Test void testCommandLine() {
      Main main = new Main(new String[] {""});
      assertNotNull(main);
      assertTrue(main.cli().shouldExit() );
      assertEquals(1, main.cli().exitCode());
  }
}
