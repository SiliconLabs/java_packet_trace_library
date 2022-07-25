// Copyright (c) 2020 Silicon Labs. All rights reserved.

package com.silabs.pti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.silabs.pti.adapter.AdapterPort;

class MainTest {
  
  private static Path propsFile;


  @BeforeAll
  static void setUpBefore() {
    try {
      propsFile = Files.createTempFile("ptiMainTestCliProps", ".properties");
    } catch (IOException e) {
      e.printStackTrace();
      propsFile = null;
    }
  }
  
  @AfterAll
  static void tearDownAfter() {
    if (propsFile != null) {
      //clean up
      try {
        Files.deleteIfExists(propsFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
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
  void test_CommandLine_PropertiesNotExist() {
    Main main = new Main(new String[] { "-properties=\"~/nonExistantFile.properties\"" });
    assertNotNull(main);
    assertTrue(main.cli().shouldExit());
    assertEquals(1, main.cli().exitCode());
  }
  

  @Test
  void test_CommandLine_WithPropertiesArg() throws IOException {
    String outputLog = "output.log";
    String delayOverride = "5000";
    String threshold = "1000000";
    String zeroTimeOverride = "3000000";
    String fileFormat = "text";
    
    Properties props = new Properties();
    //Args with values
    props.put("-sn", "440012345,440098765");
    props.put("-out", outputLog);
    props.put("-time", "600000"); //10 minutes
    props.put("-driftCorrection", "enable");
    props.put("-driftCorrectionThreshold", threshold);
    props.put("-delay", "1000");
    props.put("-zeroTimeThreshold", threshold);
    props.put("-format", fileFormat);
    props.put("-ip", "1.2.3.4,9.8.7.6");
    
    //Args without values
    props.put("-serial0", "");
    props.put("-discreteNodeCapture", "");
    props.put("-discover", "");
    
    assertNotNull(propsFile, "Expected input properties file to exist");
    
    //write props to file for input to Main
    try (PrintWriter writer = new PrintWriter(propsFile.toFile());) {
      props.store(writer, "");
    } catch (Exception e) {
      throw e;
    }
    
    //call Main with properties file that arguments as input and override -delay
    //using additional
    Main main = new Main(new String[] { "-zeroTimeThreshold="+zeroTimeOverride, 
                                        "-properties=\""+propsFile.toFile().getAbsolutePath()+"\"", 
                                        "-delay="+delayOverride });
    
    //verify basic passing statuses
    assertNotNull(main, "Expected main object to be non-null");
    assertFalse(main.cli().shouldExit(), "Expected CLI to not exit");
    assertTrue(main.cli().exitCode() <= 0, "Expected successful exit code");
    
    //verify specific output
    assertTrue(main.cli().driftCorrection(), "Expected drift correction to be enabled");
    assertTrue(main.cli().isDiscovery(), "Expected discover to be set to enable");
    assertTrue(main.cli().discreteNodeCapture(), "Expected discrete node capture to always be enabled");
    assertTrue(fileFormat.equalsIgnoreCase(main.cli().fileFormat().name()), "Expected matching file format values");

    assertEquals( outputLog, main.cli().output(), "Expected matching path for output log");
    assertEquals(Integer.valueOf(delayOverride), main.cli().delayMs(),"Expected matching delay");
    assertEquals(Integer.valueOf(threshold), main.cli().driftCorrectionThreshold(), "Expected matching drift correction threshold");
    assertEquals(AdapterPort.SERIAL0, main.cli().port(), "Expected matching serial port");
    assertEquals(Integer.valueOf(zeroTimeOverride), main.cli().zeroTimeThreshold(),"Expected matching zero time threshold");
    
  }
  
  @Test
  void test_CommandLine_WithoutPropertiesArg() throws IOException {
    String outputLog = "output.log";
    String delayOverride = "5000";
    String threshold = "1000000";
    String zeroTimeOverride = "3000000";
    String fileFormat = "text";
    
    //call Main with properties file that arguments as input and override -delay
    //using additional
    Main main = new Main(new String[] { "-sn=440012345,440098765",
     "-out="+outputLog,
     "-time=600000",
      "-driftCorrection=enable",
      "-driftCorrectionThreshold="+threshold,
      "-delay=1000",
      "-zeroTimeThreshold="+threshold,
      "-format="+fileFormat,
      "-ip=1.2.3.4,9.8.7.6",
      "-admin",
      "-discreteNodeCapture",
      "-discover",
      //Arg Overrides
      "-delay=5000",
      "-zeroTimeThreshold=3000000" });
    
    //verify basic passing statuses
    assertNotNull(main, "Expected main object to be non-null");
    assertFalse(main.cli().shouldExit(), "Expected CLI to not exit");
    assertTrue(main.cli().exitCode() <= 0, "Expected successful exit code");
    
    //verify specific output
    assertTrue(main.cli().driftCorrection(), "Expected drift correction to be enabled");
    assertTrue(main.cli().isDiscovery(), "Expected discover to be set to enable");
    assertTrue(main.cli().discreteNodeCapture(), "Expected discrete node capture to always be enabled");
    assertTrue(fileFormat.equalsIgnoreCase(main.cli().fileFormat().name()), "Expected matching file format values");

    assertEquals( outputLog, main.cli().output(), "Expected matching path for output log");
    assertEquals(Integer.valueOf(delayOverride), main.cli().delayMs(),"Expected matching delay");
    assertEquals(Integer.valueOf(threshold), main.cli().driftCorrectionThreshold(), "Expected matching drift correction threshold");
    assertEquals(AdapterPort.ADMIN, main.cli().port(), "Expected matching serial port");
    assertEquals(Integer.valueOf(zeroTimeOverride), main.cli().zeroTimeThreshold(),"Expected matching zero time threshold");
    
  }
}
