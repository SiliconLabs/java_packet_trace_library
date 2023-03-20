// Copyright (c) 2020 Silicon Labs. All rights reserved.

package com.silabs.pti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.silabs.na.pcap.LinkType;
import com.silabs.pti.debugchannel.EventType;

class EventTypeTest {

  @Test void eventTypes() {
    EventType et = EventType.ASSERT;
    assertNotNull(et);
  }

  @Test void dlts() {
    assertEquals(LinkType.SILABS_DEBUG_CHANNEL, LinkType.silabsDebugChannel());
  }

}
