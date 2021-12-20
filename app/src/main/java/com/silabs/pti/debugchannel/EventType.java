/*******************************************************************************
 * # License
 * Copyright 2020 Silicon Laboratories Inc. www.silabs.com
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

package com.silabs.pti.debugchannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Event type. Each event has one of these. This class internally keeps
 * structure for the integer that is the type. External callers, however,
 * should not assume this structure and should only use API methods
 * for access (isPacket(), isGroup(), etc.). Should you have a need to know
 * something about the type, ALWAYS add aditional API methods to this class,
 * rather than assume that the integer returned by value() has a meaning.
 *
 * Created on Sep 29, 2005
 * @author Timotej (timotej@ember.com)
 */
public class EventType implements Comparable<EventType> {

  // Type is 32 bit integer used like this:
  //   MSB byte: bitmask, defined as:
  //             x x x x x x x x
  //                     | | | |
  //                     | | | +- if set, this is a packet event
  //                     | | +--- if set, then don't persist event
  //                     | +----- if set, then this is a non-toplevel event
  //                     +------- if set, then this is an AEM event
  //   byte 2:   category byte: see CATEGORY_ constants
  //   byte 3-4 (short): 16 bit subtype (these for example map to debugMessage types
  private int value = 0;

  private static short nextGroupSubtype=0;

  private static final int MAX_NAME_LENGTH = 10;
  private final String name; // up to MAX_NAME_LENGTH characters
  private final String description; // unlimited length

  // STATIC ATTRIBUTES
  private static Map<String,EventType> storage
    = Collections.synchronizedMap(new HashMap<String,EventType>());
  private static List<EventType> groups
    = Collections.synchronizedList(new ArrayList<EventType>());

  // Defined masks

  private static final int MASK_NONE = 0;

  // Packets with this mask contain the payload data which is a packet trace
  private static final int MASK_PACKET = 1 << 24;

  // Packets with this mask will not be saved by the persister. Others are.
  private static final int MASK_DONTSAVE = 1 << 25;

  // Groups tagged as MASK_GENERIC will not be shown at toplevel by default
  private static final int MASK_GENERIC = 1 << 26;

  // Mask for AEM events.
  private static final int MASK_AEM = 1 << 27;

  // Defined categories
  private static final byte CATEGORY_GROUP = 1; // Group events
  private static final byte CATEGORY_BACKCHANNEL = 2; // events from debug message
  private static final byte CATEGORY_SIMULATED = 3; // simulated events
  private static final byte CATEGORY_FROM_OLD_LOG = 4; // simulated events
  private static final byte CATEGORY_SYNTHETIC_USER = 5; // events user created
  private static final byte CATEGORY_SYNTHETIC_SYSTEM = 6; // transient events
  private static final byte CATEGORY_PACKET_SOURCE = 7; // live packet source other than backchannel
  private static final byte CATEGORY_IMPORTED = 8; // Imported from some other capture system
  private static final byte CATEGORY_INSTRUMENTATION = 9; // These are instrumentation events for debugging
  private static final byte CATEGORY_UNKNOWN = (byte)0xFF; // Unknown

  private static EventType[] debugMessageEventTypes
    = new EventType[DebugMessageType.values().length];

  /** Returns the event type for a given debug message type */
  public static final EventType fromDebugMessage(final DebugMessageType t) {
    if ( t == DebugMessageType.INVALID )
      return EventType.UNKNOWN_DEBUG_MESSAGE;

    EventType et = debugMessageEventTypes[t.ordinal()];
    if ( et == null ) {
      int mask = 0;
      if ( t.name().startsWith("PACKET") )
        mask = MASK_PACKET;
      else if ( t.name().startsWith("AEM") )
        mask = MASK_AEM;
      String name = t.description();
      if ( name.length() > MAX_NAME_LENGTH )
        name = name.substring(0, MAX_NAME_LENGTH);
      et = make(mask,
                CATEGORY_BACKCHANNEL,
                (short)t.value(),
                name,
                t.longDescription());
      debugMessageEventTypes[t.ordinal()] = et;
    }
    return et;
  }

  static {
    for ( DebugMessageType dmt: DebugMessageType.values() ) {
      if ( dmt == DebugMessageType.INVALID ) continue;
      fromDebugMessage(dmt);
    }
  }

  public static final EventType UNKNOWN_DEBUG_MESSAGE
    = make(MASK_NONE, CATEGORY_UNKNOWN, (short)0, "Unknown", "Unknown debug message event");

  public static final EventType UNKNOWN_TYPE
    = make (MASK_NONE, CATEGORY_UNKNOWN, (short)1, "Unknown", "Unknown event type");


  // Defined backchannel event types which are referred. Do note, that
  // all debug message types have their own event types anyway, these are just
  // the ones that have references elsewhere, so API is cleaner
  public static final EventType RESET_INFO = fromDebugMessage(DebugMessageType.RESET_INFO);
  public static final EventType TIME_SYNC = fromDebugMessage(DebugMessageType.TIME_SYNC);
  public static final EventType CPU_USAGE = fromDebugMessage(DebugMessageType.CPU_USAGE);
  public static final EventType PRINTF = fromDebugMessage(DebugMessageType.PRINTF);
  public static final EventType API_TRACE = fromDebugMessage(DebugMessageType.API_TRACE);
  public static final EventType ASSERT = fromDebugMessage(DebugMessageType.ASSERT);
  public static final EventType EZSP = fromDebugMessage(DebugMessageType.EZSP);
  public static final EventType INFO_RESPONSE = fromDebugMessage(DebugMessageType.INFO_RESPONSE);
  public static final EventType SNIFFER_PACKET = fromDebugMessage(DebugMessageType.PACKET_TRACE);
  public static final EventType RX_2420  = fromDebugMessage(DebugMessageType.PACKET_TRACE_EM2420_RX);
  public static final EventType TX_2420  = fromDebugMessage(DebugMessageType.PACKET_TRACE_EM2420_TX);
  public static final EventType RX_250  = fromDebugMessage(DebugMessageType.PACKET_TRACE_EM2XX_RX);
  public static final EventType TX_250  = fromDebugMessage(DebugMessageType.PACKET_TRACE_EM2XX_TX);
  public static final EventType RX_350  = fromDebugMessage(DebugMessageType.PACKET_TRACE_EM3XX_RX);
  public static final EventType TX_350  = fromDebugMessage(DebugMessageType.PACKET_TRACE_EM3XX_TX);
  public static final EventType RX_PRO2P = fromDebugMessage(DebugMessageType.PACKET_TRACE_PRO2P_RX);
  public static final EventType TX_PRO2P = fromDebugMessage(DebugMessageType.PACKET_TRACE_PRO2P_TX);
  public static final EventType OTHER_PRO2P = fromDebugMessage(DebugMessageType.PACKET_TRACE_PRO2P_OTHER);
  public static final EventType RX_EFR = fromDebugMessage(DebugMessageType.PACKET_TRACE_EFR_RX);
  public static final EventType TX_EFR = fromDebugMessage(DebugMessageType.PACKET_TRACE_EFR_TX);
  public static final EventType OTHER_EFR = fromDebugMessage(DebugMessageType.PACKET_TRACE_EFR_OTHER);

  public static final EventType TX_GENERIC = make ( MASK_PACKET,
                                              CATEGORY_PACKET_SOURCE,
                                              (short)0,
                                              "Tx",
                                              "Generic Tx packet");

  public static final EventType RX_GENERIC = make ( MASK_PACKET,
                                              CATEGORY_PACKET_SOURCE,
                                              (short)1,
                                              "Rx",
                                              "Generic Rx packet");

  public static final EventType TX_PRO2 = make ( MASK_PACKET,
                                           CATEGORY_PACKET_SOURCE,
                                           (short)2,
                                           "TxPro2",
                                           "Pro2 Tx packet");

  public static final EventType RX_PRO2 = make ( MASK_PACKET,
                                           CATEGORY_PACKET_SOURCE,
                                           (short)3,
                                           "RxPro2",
                                           "Pro2 Rx packet");

  public static final EventType TX_OWL = make ( MASK_PACKET,
                                           CATEGORY_PACKET_SOURCE,
                                           (short)4,
                                           "TxOWL",
                                           "OWL Tx packet");

  public static final EventType RX_OWL = make ( MASK_PACKET,
                                           CATEGORY_PACKET_SOURCE,
                                           (short)5,
                                           "RxOWL",
                                           "OWL Rx packet");

  public static final EventType SIMULATED_AEM_SAMPLE = make ( MASK_AEM,
                                                        CATEGORY_SIMULATED,
                                                        (short)0,
                                                        "AEMSample",
                                                        "Energy Measurement Sample");
  public static final EventType SIMULATED_AEM_COUNTER = make ( MASK_AEM,
                                                         CATEGORY_SIMULATED,
                                                         (short)1,
                                                         "AEMCntr",
                                                         "Energy Measurement Counters");

  // Imported event types
  public static final EventType IMPORTED_COM_PROBE = make ( MASK_PACKET,
                                                      CATEGORY_IMPORTED,
                                                      (short)0,
                                                      "ComProbe",
                                                      "ComProbe Protocol Analysis System");

  public static final EventType HCI_UNENC = make ( MASK_PACKET,
                                             CATEGORY_IMPORTED,
                                             (short)1,
                                             "HCIUnencap",
                                             "HCI Unencapsulated");

  public static final EventType HCI_UART = make ( MASK_PACKET,
                                              CATEGORY_IMPORTED,
                                              (short)2,
                                              "HCIUart",
                                              "HCI UART Datalink");

  public static final EventType HCI_BSCP = make ( MASK_PACKET,
                                              CATEGORY_IMPORTED,
                                              (short)3,
                                              "HCIBSCP",
                                              "HCI BSCP Datalink");

  public static final EventType HCI_SERIAL = make ( MASK_PACKET,
                                              CATEGORY_IMPORTED,
                                              (short)4,
                                              "HCISerial",
                                              "HCI Serial Datalink");

  public static final EventType IMPORTED_BT_SNOOP = make ( MASK_PACKET,
                                                     CATEGORY_IMPORTED,
                                                     (short)5,
                                                     "BTSnoop",
                                                     "BT Snoop Imported Packet");

  public static final EventType IMPORTED_PCAP = make ( MASK_PACKET,
                                                     CATEGORY_IMPORTED,
                                                     (short)6,
                                                     "PCAP",
                                                     "PCAP Imported Packet");

  public static final EventType IMPORTED_ZNIFFER = make ( MASK_PACKET,
                                                    CATEGORY_IMPORTED,
                                                    (short)7,
                                                    "Zniffer",
                                                    "Zniffer Imported Packet");

  public static final EventType IMPORTED_ZBOSS = make ( MASK_PACKET,
                                                  CATEGORY_IMPORTED,
                                                  (short)8,
                                                  "ZBOSS",
                                                  "Packet produced by ZBOSS dump");

  public static final EventType DEBUG
  = make( 0,
          CATEGORY_BACKCHANNEL,
          (short)0xF00F,
          "Unknown",
          "Unknown adapter event" );


  // Simulated events
  public static final EventType SIMULATED_LOCATION
  = make ( MASK_DONTSAVE,
           CATEGORY_SIMULATED,
           (short)1,
           "Location",
           "Node location information" );

  public static final EventType SIMULATED_TX
  = make ( MASK_PACKET,
           CATEGORY_SIMULATED,
           (short)2,
           "Packet",
           "Simulated packet" );

  public static final EventType SIMULATED_RX
  = make ( 0,
           CATEGORY_SIMULATED,
           (short)3,
           "Rx",
           "Simulated RX" );

  // Defined group event types
  public static final EventType GROUP_GENERIC
  = mkGrp ( MASK_GENERIC,
            "Transactn",
            "Generic Transaction");

  public static final EventType GROUP_MAC
  = mkGrp ( MASK_GENERIC,
            "Mac",
            "15.4 MAC transaction");

  public static final EventType GROUP_UNICAST
  = mkGrp ( MASK_NONE,
            "Unicast",
            "ZigBee unicast transaction");

  public static final EventType GROUP_BROADCAST
  = mkGrp ( MASK_NONE,
           "Broadcast",
           "ZigBee broadcast transaction");

  public static final EventType GROUP_TRANSPORT_DGRAM
  = mkGrp ( MASK_NONE,
           "Datagram",
           "ZigBee transport datagram");

  public static final EventType GROUP_APS
  = mkGrp ( MASK_NONE,
           "APS",
           "ZigBee APS transaction");

  public static final EventType GROUP_ASSOCIATION
  = mkGrp ( MASK_NONE,
           "Associate",
           "ZigBee association transaction");

  public static final EventType GROUP_ROUTE_DISCOVERY
  = mkGrp ( MASK_NONE,
           "Route",
           "Route discovery transaction");

  public static final EventType GROUP_EMBER_NETWORK
  = mkGrp ( MASK_NONE,
           "EmberNet",
           "EmberNet network transaction");

  // These are bootload thingies.
  public static final EventType GROUP_BOOTLOAD
  = mkGrp ( MASK_NONE,
           "Bootload",
           "OTA Bootload Transaction");

  public static final EventType GROUP_BOOTLOAD_QUERY
  = mkGrp ( MASK_NONE,
           "BootQry",
           "OTA Bootload Query Transaction");

  public static final EventType GROUP_BOOTLOAD_REQUEST
  = mkGrp ( MASK_NONE,
           "BootReq",
           "OTA Bootload Request Transaction");

  public static final EventType GROUP_BOOTLOAD_XMODEM
  = mkGrp ( MASK_NONE,
           "BootXMod",
           "OTA Bootload XModem Transaction");

  public static final EventType GROUP_NETWORK_REJOIN
  = mkGrp ( MASK_NONE,
           "Rejoin",
           "Network Rejoin transaction");

  public static final EventType GROUP_ZIGBEE_UNICAST_COMMAND
  = mkGrp ( MASK_NONE,
           "CmdUnicast",
           "ZigBee Unicast Command");

  public static final EventType GROUP_SKKE
  = mkGrp ( MASK_NONE,
           "SKKE",
           "SKKE Protocol Negotiation");

  public static final EventType GROUP_ENTITY_AUTHENTICATION
  = mkGrp ( MASK_NONE,
           "EA",
           "Entity Authentication");

  public static final EventType GROUP_ALARM
  = mkGrp ( MASK_NONE,
           "Alarm",
           "ZigBee APS Alarm");

  public static final EventType GROUP_LEAVE
  = mkGrp ( MASK_NONE,
           "Leave",
           "Leave Transaction");

  public static final EventType GROUP_ZIGBEE_BROADCAST_COMMAND
  = mkGrp ( MASK_NONE,
           "CmdBcast",
           "ZigBee Broadcast Command");

  public static final EventType GROUP_IP
  = mkGrp ( MASK_GENERIC,
            "IP",
            "IP Message");

  public static final EventType GROUP_FRAGMENT
  = mkGrp ( MASK_GENERIC,
            "Reassembly",
            "Reassembled Fragments");

  public static final EventType GROUP_TCP
  = mkGrp ( MASK_NONE,
           "TCP",
           "TCP Message");

  public static final EventType GROUP_UDP
  = mkGrp ( MASK_NONE,
           "UDP",
           "UDP Message");

  public static final EventType GROUP_ZLL_COMMISSIONING
  = mkGrp ( MASK_NONE,
           "ZllComm",
           "ZllCommissioning");

  public static final EventType GROUP_PANA
  = mkGrp ( MASK_NONE,
           "Pana",
           "PANA session");

  public static final EventType GROUP_ICMP
  = mkGrp ( MASK_NONE,
           "ICMPv6",
           "ICMPv6 Message");

  public static final EventType GROUP_DNS
  = mkGrp ( MASK_NONE,
           "DNS",
           "DNS Message");

  public static final EventType GROUP_MUSTANG
  = mkGrp ( MASK_NONE,
           "Mustang",
           "Mustang");

  public static final EventType GROUP_RF4CE_DISCOVERY
  = mkGrp ( MASK_NONE,
           "Discovery",
           "RF4CE Discovery");

  public static final EventType GROUP_RF4CE_PAIRING
  = mkGrp ( MASK_NONE,
           "Pairing",
           "RF4CE Pairing");

  public static final EventType GROUP_LATENCY
  = mkGrp ( MASK_GENERIC,
            "Latency",
            "Latency");

   public static final EventType GROUP_OWL
  = mkGrp ( MASK_NONE,
           "OWL",
           "One Way Link");

   public static final EventType GROUP_RF4CE_NETWORK
   = mkGrp ( MASK_GENERIC,
             "Network",
             "RF4CE Network");

   public static final EventType GROUP_RF4CE_GDP
   = mkGrp ( MASK_NONE,
            "GDP",
            "RF4CE GDP");

   public static final EventType GROUP_RF4CE_ZRC_1_X
   = mkGrp ( MASK_NONE,
            "ZRC1.x",
            "ZigBee Remote Control 1.x");

   public static final EventType GROUP_RF4CE_ZRC_2_X
   = mkGrp ( MASK_NONE,
            "ZRC2.x",
            "ZigBee Remote Control 2.x");

   public static final EventType GROUP_RF4CE_MSO
   = mkGrp ( MASK_NONE,
            "MSO",
            "RF4CE MSO");

   public static final EventType GROUP_CONNECT_NETWORK
   = mkGrp ( MASK_NONE,
            "ConctNet",
            "Connect Network");

   public static final EventType GROUP_BLE_ATTRIBUTES
   = mkGrp ( MASK_NONE,
             "BleAttr",
             "BLE Attributes Protocol");

   public static final EventType GROUP_BLE_ATTRIBUTE_COMMANDS
   = mkGrp ( MASK_NONE,
             "BleAttrCmd",
             "BLE Attribute Command");

   public static final EventType GROUP_BLE_ADVERTISEMENT
   = mkGrp ( MASK_NONE,
             "BleAdv",
             "BLE Advertisement");

   public static final EventType GROUP_BLE_ADVERTISEMENT_EXT
   = mkGrp ( MASK_NONE,
             "BleAdvExt",
             "BLE Advertisement Extensions");

   public static final EventType GROUP_BLE_LINKLAYER
   = mkGrp ( MASK_NONE,
             "BleLl",
             "BLE Link Layer Control");

   public static final EventType GROUP_BLE_SMP
   = mkGrp ( MASK_NONE,
             "BleSmp",
             "BLE Pairing");

   public static final EventType GROUP_BT_MESH_ADVERTISEMENT
   = mkGrp ( MASK_NONE,
             "BtMeshAdv",
             "BT Mesh Advertisement");

   public static final EventType GROUP_BT_MESH_RELAY
   = mkGrp ( MASK_NONE,
             "BtMeshRel",
             "BT Mesh Relay");

   public static final EventType GROUP_BT_MESH_LINK_ESTABLISHMENT
   = mkGrp ( MASK_NONE,
             "BtMeshLink",
             "BT Mesh Link Establishment");

   public static final EventType GROUP_BT_MESH_PROVISIONING
   = mkGrp ( MASK_NONE,
             "BtMeshProv",
             "BT Mesh Provisioning");

   public static final EventType GROUP_BT_MESH_ACCESS
   = mkGrp ( MASK_NONE,
             "BtMeshAcc",
             "BT Mesh Access Message Transactions");

   public static final EventType GROUP_BT_MESH_SEGMENTATIONACK
   = mkGrp ( MASK_NONE,
             "BtMeshSeg",
             "BT Mesh Segmentation Acknowledgement");

  // Synthetic event types that are created as a result of user action
  // These stay in the cache and are seen by the user.
  public static final EventType CHANNEL_CHANGE
  = make ( 0,
           CATEGORY_SYNTHETIC_USER,
           (short)1,
           "ChChange",
           "Sniffer channel change notification");

  // Synthetic event types that are created as a result of user action
  // These stay in the cache and are seen by the user.
  public static final EventType NETWORK_CHANGE
  = make ( 0,
           CATEGORY_SYNTHETIC_USER,
           (short)3,
           "NetChange",
           "Sniffer network change notification");

  public static final EventType BOOKMARK_PACKET
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_USER,
           (short)4,
           "Bookmark",
           "Packet annotation");

  public static final EventType BOOKMARK_GROUP
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_USER,
           (short)5,
           "Bookmark",
           "Transaction annotation");

  public static final EventType ADAPTER_INFO
  = make ( 0,
           CATEGORY_SYNTHETIC_USER,
           (short)6,
           "Adapter",
           "Adapter information from discovery");


  // Synthetic event type that are created by the system on the fly
  // These are filtered out by the VirtualEventFilter synthesizer.
  public static final EventType GROUP_MARK
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)1,
           "GrpMark",
           "Group marker" );

  public static final EventType STREAM_STOP
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)2,
           "StopStream",
           "Stream stopped" );

  public static final EventType TIME_SHIFT
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)3,
           "TimeShift",
           "Time shift performed" );

  public static final EventType DECRYPTOR_TYPE
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)4,
           "Decryptor",
           "Decryptor type detected");

  public static final EventType TIMER_TICK
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)5,
           "Tick",
           "Tick");

  public static final EventType STACK_PROFILE
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)6,
           "StackPro",
           "Stack profile changed");

  public static final EventType STREAM_CLEAR
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)7,
           "Clear",
           "Stream Clear");

  public static final EventType CAPTURE_PROFILE
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)8,
           "CapturePro",
           "Capture Profile");

  public static final EventType SEGMENT_START
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)9,
           "Segment",
           "Segment Start");

  public static final EventType OUT_OF_SEQUENCE
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)10,
           "OutOfSeq",
           "Message out of Sequence" );

  public static final EventType TIME_SET
  = make ( MASK_DONTSAVE,
           CATEGORY_SYNTHETIC_SYSTEM,
           (short)11,
           "TimeSet",
           "Time Set Notification" );


  // Event types that come from old log files or other sources.
  // Effectively the next two event types are:
  //    "packets that we got from an unknown source"
  // These packets may differ in terms of having or not having the length byte,
  // everything else is considered to be the rest of the ZigBee or embernet
  // packet structure, without any extra radio information attached either
  // at the end or at the beginning.
  public static final EventType LOG_PACKET_NO_LENGTH
  = make ( MASK_PACKET,
           CATEGORY_FROM_OLD_LOG,
           (short)0,
           "Packet",
           "Packet with no length byte" );

  // Event types that come from old log files
  public static final EventType LOG_PACKET_WITH_LENGTH
  = make ( MASK_PACKET,
           CATEGORY_FROM_OLD_LOG,
           (short)1,
           "Packet",
           "Packet with length byte" );

  // Methods

  public static final EventType CAPTURE_PROBLEM
  = make ( MASK_NONE,
           CATEGORY_INSTRUMENTATION,
           (short)0,
           "Error",
           "Capture Errors");

  public static final EventType CAPTURE_INFO
  = make ( MASK_NONE,
           CATEGORY_INSTRUMENTATION,
           (short)1,
           "Diagnostic",
           "Capture Diagnostics");

  private EventType(final int value, final String name, final String description) {
    this.value = value;
    this.name = name;
    this.description = description;
  }

  // Use this to create a group
  private static EventType mkGrp ( final int mask,
                                   final String name,
                                   final String description ) {
    return make ( mask | MASK_DONTSAVE,
                  CATEGORY_GROUP,
                  nextGroupSubtype++,
                  name,
                  description);
  }

  // Retrieves or creates the event type
  private static EventType make ( final int mask,
                                  final byte category,
                                  final short subtype,
                                  final String name,
                                  final String description ) {
    int value = makeValue(mask, category, subtype);
    String key = ""+value;
    EventType eventType = storage.get(key);
    if ( eventType != null )
      throw new IllegalArgumentException("Duplicate event type: " + name + "(" + mask + "/" + category + "/" + subtype + ")");
    if ( name == null )
      throw new IllegalArgumentException("Event type name is null");
    if ( description == null )
      throw new IllegalArgumentException("Event type description is null");
    if ( name.length() > MAX_NAME_LENGTH )
      throw new IllegalArgumentException("Event name " + name
                                         + " exceeds max length "
                                         + MAX_NAME_LENGTH);
    if ( name.indexOf(' ') != -1 )
      throw new IllegalArgumentException("No spaces in event name allowed");
    eventType = new EventType(value, name, description);
    storage.put(key, eventType);
    if ( eventType.isGroup() ) {
      groups.add(eventType);
    }
    return eventType;
  }

  /**
   * This method can be used to retrieve the event type from a value.
   * This will be used by file reader/writers. May return null, but that
   * means something is seriously wrong. Probably file missmatch.
   * If you use this anywhere else then in persist/depersist mechanisms
   * you are probably doing something VERY WRONG.
   */
  public static final EventType get(final int value) {
    return storage.get(""+value);
  }

  /**
   * Returns all the group event types. This is used by filters.
   */
  public static final EventType[] getGroups() {
    return groups.toArray(new EventType[0]);
  }

  /**
   * Returns all the event types. This is used by filters.
   */
  public static final EventType[] getAllTypes() {
    EventType[] array
      = storage.values().toArray(new EventType[0]);
    Arrays.sort(array);
    return array;
  }

  /**
   * Returns true is this event is packet and contains the PAYLOAD raw bytes
   */
  public boolean isPacket() {
    return ( (value & MASK_PACKET) != 0 );
  }

  /** Returns true if this type is an aem event */
  public boolean isAem() {
    return ( ( value & MASK_AEM ) != 0 );
  }

  /** Returns true if this type is aem sample */
  public boolean isAemSample() {
    return ( isAem() && (
               this == SIMULATED_AEM_SAMPLE
               || this == fromDebugMessage(DebugMessageType.AEM_SAMPLE)) );
  }

  /** Returns true if this type is aem counter */
  public boolean isAemCounter() {
    return ( isAem()  && (
        this == SIMULATED_AEM_COUNTER
        || this == fromDebugMessage(DebugMessageType.AEM_COUNTER)) );
  }

  public boolean isTx() {
    return this == TX_2420 || this == TX_250 || this == SIMULATED_TX ||
    this == TX_350 || this == TX_GENERIC || this == TX_PRO2 || this == TX_PRO2P ||
    this == TX_OWL || this == TX_EFR;
  }

  public boolean isRx() {
    return this == RX_2420 || this == RX_250 || this == SIMULATED_RX ||
    this == RX_350 || this == RX_GENERIC || this == RX_PRO2 || this == RX_PRO2P ||
    this == RX_OWL || this == RX_EFR;
  }

  /**
   * Returns true if this event type should not be saved by persister.
   */
  public boolean doNotSave() {
    return ( (value & MASK_DONTSAVE) != 0 );
  }
  /**
   * Returns true if this event is a group.
   */
  public boolean isGroup() {
    return (CATEGORY_GROUP == category());
  }

  /**
   * Return true if the type is a generic
   * type such as BROADCAST or UNICAST
   * 
   * @return boolean
   */
  public boolean isGeneric() {
    return ((value() & MASK_GENERIC) != 0);
  }

  /**
   * Returns true if this packet has no length byte. It will not validate
   * whether this event is a packet event at all, so this is up to you.
   */
  public boolean hasNoLengthByte() {
    return ( this == LOG_PACKET_NO_LENGTH
             || this == SNIFFER_PACKET
             || this == SIMULATED_TX
             || this == IMPORTED_PCAP
             || this == IMPORTED_ZBOSS );
  }

  public boolean isAlertOnAMap() {
    return this == debugMessageEventTypes[DebugMessageType.ASSERT.ordinal()]
        || this == debugMessageEventTypes[DebugMessageType.CORE_DUMP.ordinal()]
            || this == debugMessageEventTypes[DebugMessageType.ERROR.ordinal()];
  }

  public boolean isHci() {
    return this == HCI_BSCP || this == HCI_SERIAL || this == HCI_UART || this == HCI_UNENC;
  }

  /**
   * Returns true if this event comes from backchannel.
   */
  public boolean isFromBackchannel() {
    return (CATEGORY_BACKCHANNEL == category());
  }

  public boolean isSimulatedRxTx() {
    return this == SIMULATED_RX || this == SIMULATED_TX;
  }

  public boolean isFrom2420() {
    return this == TX_2420 || this == RX_2420 || this == SNIFFER_PACKET;
  }

  public boolean isFromPro2() {
    return this == TX_PRO2 || this == TX_PRO2P || this == RX_PRO2 || this == RX_PRO2P;
  }

  public boolean isFromEfr() {
    return this == TX_EFR || this == RX_EFR || this == OTHER_EFR;
  }

  public boolean isFromOWL(){
    return this == TX_OWL || this == RX_OWL;
  }

  public boolean isSystemTransient() {
    return (CATEGORY_SYNTHETIC_SYSTEM == category());
  }

  public boolean isUserGenerated() {
    return (CATEGORY_SYNTHETIC_USER == category());
  }

  public boolean isBookmark() {
    return this == BOOKMARK_GROUP || this == BOOKMARK_PACKET;
  }

  /**
   * Returns raw type. Caller should not assume any internal structure
   * of this number, or particular bit properties.
   * If you use this anywhere else then persist/depersist mechanism, you
   * are most probably doing something WRONG!
   * If you want to check equality, use equals() method.
   */
  public int value() {
    return value;
  }

  /**
   * Returns name. Length is limited internally. (See MAX_NAME_LENGTH).
   */
  public String name() {
    return name;
  }

  // Returns description.
  public String description() {
    return description;
  }

  /** This method is fairly heavy. Don't use too much */
  public DebugMessageType debugMessageType() {
    for ( int i=0; i<debugMessageEventTypes.length; i++ ) {
      if ( this == debugMessageEventTypes[i] )
        return DebugMessageType.values()[i];
    }
    return null;
  }

  public boolean isDebugMessageType(final DebugMessageType debugMessageType) {
    return this == fromDebugMessage(debugMessageType);
  }

  // Internal method: returns category: 2nd byte
  private byte category() {
    return (byte)((value >> 16) & 0xFF);
  }
  // Predefined static event types

  /** Returns the subtype */
  public short subtype() {
    return (short)(value & 0x0000FFFF);
  }

  // Internal method that constructs the value from individual elements
  private static int makeValue(final int mask, final byte category, final short subtype) {
    int st = (0x0000FFFF & subtype);
    int ct = (0x000000FF & category) << 16;
    return ct|st|mask;
  }

  /**
   * Returns event type with a given name.
   *
   * @return EventType
   */
  public static final EventType findByName(final String name) {
    for ( EventType et: getAllTypes() ) {
      if ( et.name().equalsIgnoreCase(name) )
        return et;
    }
    return null;
  }

  // Sorts according to name
  @Override
  public int compareTo(final EventType o) {
    EventType et = o;
    return name.compareTo(et.name);
  }

  @Override
  public String toString() {
    return name + " [" + value + "," + description + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + value;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EventType other = (EventType) obj;
    return (value == other.value);
  }
}

