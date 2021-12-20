// Copyright (c) 2020 Silicon Labs. All rights reserved.

package com.silabs.pti;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.silabs.pti.debugchannel.EventType;

class EventTypeTest {

  @Test void eventTypes() {
    EventType et = EventType.ASSERT;
    assertNotNull(et);
  }

}
