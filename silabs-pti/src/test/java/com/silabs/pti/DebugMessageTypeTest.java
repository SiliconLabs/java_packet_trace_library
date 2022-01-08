package com.silabs.pti;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.silabs.pti.debugchannel.DebugMessageType;

/**
 * Tests for debug message type.
 * 
 * @author timotej
 *
 */
public class DebugMessageTypeTest {

  @Test
  public void testDebugMessageTypeDuplicates() {
    final Set<Integer> codes = new HashSet<Integer>();
    for (final DebugMessageType t : DebugMessageType.values()) {
      if (codes.contains(t.value())) {
        fail("Debug message type code is duplicated: " + t.value());
      } else {
        codes.add(t.value());
      }
    }
  }

  @Test
  public void testDebugMessageTypeSorting() {
    int lastCode = -1;
    for (final DebugMessageType t : DebugMessageType.values()) {
      if (t == DebugMessageType.INVALID)
        continue;
      if (t.value() > lastCode) {
        lastCode = t.value();
      } else {
        fail("Debug message types must be sorted incrementally: " + t.value());
      }
    }
  }

  public void createJsonOutOfEnum() throws IOException {
    try (PrintWriter pw = new PrintWriter(new File("debug-message-type.json"))) {
      int i = 0;
      final DebugMessageType[] values = DebugMessageType.values();
      pw.println("{");
      pw.println("\"description\": \"This file contains the valid debug message type codes for the Silicon Labs Debug Message protocol.\",");
      pw.println("\"featureLevel\": \"22\",");
      pw.println("\"date\": \"2022.1.8\",");
      pw.println("\"types\": {");
      for (final DebugMessageType t : values) {
        pw.println("  \"" + t.name() + "\" : {");
        String hex = Integer.toHexString(t.value()).toUpperCase();
        while (hex.length() < 4) {
          hex = "0" + hex;
        }
        pw.println("    \"code\":\"0x" + hex + "\",");
        pw.println("    \"shortDescription\":\"" + t.description() + "\",");
        pw.println("    \"longDescription\":\"" + t.longDescription() + "\",");
        pw.println("    \"status\":\"\"");
        i++;
        if (i < values.length)
          pw.println("  },");
        else
          pw.println("  }");
      }
      pw.println("}");
      pw.println("}");
    }
  }
}
