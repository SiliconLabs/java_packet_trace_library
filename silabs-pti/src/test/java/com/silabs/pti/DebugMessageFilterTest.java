package com.silabs.pti;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;

import org.junit.jupiter.api.Test;

import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.DebugMessageType;
import com.silabs.pti.filter.CliDebugMessageFilter;

/**
 * Test for the debug message filters.
 * 
 * @author timotej
 *
 */
public class DebugMessageFilterTest {

  @Test
  public void basicFilters() throws ParseException {
    DebugMessage dm = new DebugMessage(0, 0, new byte[] { 0, 0});
    CliDebugMessageFilter f = new CliDebugMessageFilter("true");
    assertTrue(f.isMessageKept(dm));
    f = new CliDebugMessageFilter("!true");
    assertFalse(f.isMessageKept(dm));
    f = new CliDebugMessageFilter("false");
    assertFalse(f.isMessageKept(dm));
    f = new CliDebugMessageFilter("!false");
    assertTrue(f.isMessageKept(dm));
    f.andFilter("true");
    assertTrue(f.isMessageKept(dm));
    f.andFilter("false");
    assertFalse(f.isMessageKept(dm));
    
    f = new CliDebugMessageFilter("false");
    assertFalse(f.isMessageKept(dm));
    f.orFilter("true");
    assertTrue(f.isMessageKept(dm));
  }
  
  @Test
  public void negativeTest() {
    try {
      new CliDebugMessageFilter("definitely an invalid expression");
      fail("Invalid expression should throw an exception.");
    } catch (ParseException pe) {
      // We should end up here.
    }
  }
  
  @Test
  public void typeInFilter() throws ParseException {
    CliDebugMessageFilter f = new CliDebugMessageFilter("typeIn(" 
        + DebugMessageType.API_RX 
        + "," 
        + DebugMessageType.LATENCY.description() 
        + ")");
    
    DebugMessage dm = new DebugMessage(0, DebugMessageType.API_RX.value(), new byte[] { 0, 0});
    assertTrue(f.isMessageKept(dm));
    dm = new DebugMessage(0, DebugMessageType.AEM_COUNTER.value(), new byte[] { 0, 0});
    assertFalse(f.isMessageKept(dm));
    dm = new DebugMessage(0, DebugMessageType.LATENCY.value(), new byte[] { 0, 0});
    assertTrue(f.isMessageKept(dm));
  }
  
  @Test
  public void originatorInFilter() throws ParseException {
    CliDebugMessageFilter f = new CliDebugMessageFilter("originatorIn(a,b,c)");
    
    DebugMessage dm = DebugMessage.make("a", "very long debug message".getBytes(), 0);
    assertTrue(f.isMessageKept(dm));
    dm = DebugMessage.make("c", "very long debug message".getBytes(), 0);
    assertTrue(f.isMessageKept(dm));
    dm = DebugMessage.make("d", "very long debug message".getBytes(), 0);
    assertFalse(f.isMessageKept(dm));
  }
  
  @Test
  public void timeWithinFilter() throws ParseException {
    CliDebugMessageFilter f = new CliDebugMessageFilter("timeWithin(12,16)");
    
    DebugMessage dm = new DebugMessage(11, DebugMessageType.API_RX.value(), new byte[] { 0, 0});
    assertFalse(f.isMessageKept(dm));
    dm = new DebugMessage(12, DebugMessageType.API_RX.value(), new byte[] { 0, 0});
    assertTrue(f.isMessageKept(dm));
    dm = new DebugMessage(15, DebugMessageType.API_RX.value(), new byte[] { 0, 0});
    assertTrue(f.isMessageKept(dm));
    dm = new DebugMessage(16, DebugMessageType.API_RX.value(), new byte[] { 0, 0});
    assertTrue(f.isMessageKept(dm));
    dm = new DebugMessage(17, DebugMessageType.API_RX.value(), new byte[] { 0, 0});
    assertFalse(f.isMessageKept(dm));
  }
  
  @Test
  public void sizeWithinFilter() throws ParseException {
    CliDebugMessageFilter f = new CliDebugMessageFilter("sizeWithin(1,3)");
    
    DebugMessage dm = new DebugMessage(11, DebugMessageType.API_RX.value(), new byte[] {  0});
    assertTrue(f.isMessageKept(dm));
    dm = new DebugMessage(12, DebugMessageType.API_RX.value(), new byte[] { 0, 0, 0});
    assertTrue(f.isMessageKept(dm));
    dm = new DebugMessage(15, DebugMessageType.API_RX.value(), new byte[] { 0, 0, 0, 0});
    assertFalse(f.isMessageKept(dm));
  }
  
  @Test
  public void containsFilter() throws ParseException {
    CliDebugMessageFilter f = new CliDebugMessageFilter("contains(coffee)");
    DebugMessage dm = new DebugMessage(0, DebugMessageType.AEM_RESPONSE.value(), "I could really use some coffee right now.".getBytes());
    assertTrue(f.isMessageKept(dm));
    f = new CliDebugMessageFilter("contains(cookies)");
    assertFalse(f.isMessageKept(dm));
    
    f = new CliDebugMessageFilter("contains(coffee)");
    f.andFilter("sizeWithin(10,100)");
    assertTrue(f.isMessageKept(dm));

    f = new CliDebugMessageFilter("contains(cookies)");
    f.orFilter("sizeWithin(10,100)");
    assertTrue(f.isMessageKept(dm));
  }
}
