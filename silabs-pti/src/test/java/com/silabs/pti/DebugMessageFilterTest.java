package com.silabs.pti;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;

import org.junit.jupiter.api.Test;

import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.filter.CliDebugMessageFilter;

public class DebugMessageFilterTest {

  @Test
  public void basicFilter() throws ParseException {
    DebugMessage dm = new DebugMessage(0, 0, new byte[] { 0, 0});
    CliDebugMessageFilter f = new CliDebugMessageFilter("true");
    assertTrue(f.isMessageKept(dm));
    f = new CliDebugMessageFilter("false");
    assertFalse(f.isMessageKept(dm));
  }
  
  @Test
  public void negativeTest() {
    try {
      CliDebugMessageFilter f = new CliDebugMessageFilter("definitely an invalid expression");
      fail("Invalid expression should throw an exception.");
    } catch (ParseException pe) {
      // We should end up here.
    }
  }
}
