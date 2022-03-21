/*******************************************************************************
 * # License
 * Copyright 2022 Silicon Laboratories Inc. www.silabs.com
 *******************************************************************************
 *
 * The licensor of this software is Silicon Laboratories Inc. Your use of this
 * software is governed by the terms of Silicon Labs Master Software License
 * Agreement (MSLA) available at
 * www.silabs.com/about-us/legal/master-software-license-agreement. This
 * software is distributed to you in Source Code format and is governed by the
 * sections of the MSLA applicable to Source Code.
 *
 ******************************************************************************/
    
    
// !!!! This file is generated via 'gradle createDebugMessageTypes' command. Please do not edit manually!!!!!


package com.silabs.pti.debugchannel;

import java.util.ResourceBundle;

public enum DebugMessageType {
  TIME_SYNC(0x0000),
  RESET_INFO(0x0001),
  PRINTF(0x0002),
  API_TRACE(0x0003),
  ASSERT(0x0004),
  CORE_DUMP(0x0005),
  PHY_RX(0x0006),
  API_RX(0x0007),
  PHY_TX(0x0008),
  API_TX(0x0009),
  PACKET_TRACE(0x000A),
  ERROR(0x000B),
  STATS(0x000C),
  TIME_SYNC_TEST(0x000D),
  RADIO_REBOOT_COUNT(0x000E),
  VIRTUAL_UART_TX(0x0011),
  VIRTUAL_UART_RX(0x0012),
  PACKET_TRACE_EM2420_TX(0x0020),
  PACKET_TRACE_EM2420_RX(0x0021),
  PACKET_TRACE_EM2XX_TX(0x0022),
  PACKET_TRACE_EM2XX_RX(0x0023),
  PACKET_TRACE_EM3XX_TX(0x0024),
  PACKET_TRACE_EM3XX_RX(0x0025),
  PACKET_TRACE_PRO2P_TX(0x0026),
  PACKET_TRACE_PRO2P_RX(0x0027),
  PACKET_TRACE_PRO2P_OTHER(0x0028),
  PACKET_TRACE_EFR_TX(0x0029),
  PACKET_TRACE_EFR_RX(0x002A),
  PACKET_TRACE_EFR_OTHER(0x002B),
  FLASH_READ_REQUEST(0x0030),
  FLASH_READ_RESPONSE(0x0031),
  EEPROM_READ_REQUEST(0x0032),
  EEPROM_READ_RESPONSE(0x0033),
  EEPROM_WRITE_REQUEST(0x0034),
  EEPROM_WRITE_RESPONSE(0x0035),
  RAM_READ_REQUEST(0x0036),
  RAM_READ_RESPONSE(0x0037),
  RAM_WRITE_REQUEST(0x0038),
  RAM_WRITE_RESPONSE(0x0039),
  INFO_REQUEST(0x003A),
  INFO_RESPONSE(0x003B),
  EZSP(0x003C),
  EZSP_UART(0x003D),
  DAG_TRACE(0x003E),
  SIMULATED_EZSP_CALLBACK_READY(0x003F),
  SIMULATED_WAKEUP_NCP(0x0040),
  SIMULATED_NCP_IS_AWAKE(0x0041),
  INFO_ZNET_VERSION(0x0042),
  INFO_ZIP_VERSION(0x0043),
  TIME(0x0044),
  HEAP_STACK(0x0045),
  MUSTANG(0x0046),
  LATENCY(0x0047),
  TMSP(0x0048),
  AEM_SAMPLE(0x0050),
  AEM_COUNTER(0x0051),
  AEM_REQUEST(0x0060),
  AEM_RESPONSE(0x0061),
  AEM_CURRENT_PACKET(0x0062),
  AEM_CURRENT_PACKET_V2(0x0063),
  PC_SAMPLE_PACKET(0x0064),
  EXCEPTION_PACKET(0x0065),
  LOGIC_ANALYZER(0x0066),
  CPU_USAGE(0x0070),
  CONFIG_OVER_SWO(0x0080),
  USER_COMMAND(0xFFFE),
  USER_RESPONSE(0xFFFF),

  INVALID(-1);

  private final int value;
  private static final String BUNDLE_NAME = "debugMessageType";
  private static final ResourceBundle names = ResourceBundle.getBundle(BUNDLE_NAME);

  private DebugMessageType(final int value) {
    this.value = value;
  }

  /** Returns the integer value of this debug message type */
  public int value() {
    return value;
  }

  /** Returns human-readable description of the debug message type, 10 char max */
  public String description() {
    try {
      return names.getString(name());
    } catch (Exception e) {
      return name().toLowerCase();
    }
  }

  /** Returns human-readable long description of the debug message type */
  public String longDescription() {
    try {
      return names.getString(name() + ".long");
    } catch (Exception e) {
      return description();
    }
  }

  /**
   * Finds the debug message type that matches value or INVALID if the value is
   * not valid debug message type.
   */
  public static DebugMessageType get(final int value) {
    DebugMessageType[] values = values();
    int low = 0;
    int high = values.length - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      DebugMessageType dmt = values[mid];
      if (dmt.value() == value) {
        return dmt;
      } else if (dmt.value() > value) {
        high = mid - 1;
      } else {
        low = mid + 1;
      }
    }
    return DebugMessageType.INVALID;
  } 

  public static int featureLevel() { return 22; }

  public static String featureDate() { return "2022.1.8"; }

}