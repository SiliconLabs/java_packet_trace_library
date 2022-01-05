# Silabs Debug Channel Format

## Framing

Debug channel data is a stream of data. Data is framed into individual "debug messages".
The overall format includes the framing information, in a following way.

```
Byte        Description
-----       -----------------------------------------------------------------------------
 0          Initial framing: '['
 1          Length  \ LSB, includes everything except the framing '[' and ']' characters.
 2          Length  / MSB
 length-2   Individual debug message, dependent on the version and type.
 N          End framing:  ']'
```

All tooling that deals with the discrete debug messages is expected to remove the framing, so what is left is individual 
debug messages, without the framing and the length bytes.

In case of errors (such as the `]` not being present after the length bytes) the recovery is typically accomplished by
the deframing state engine reading forward until a next `[` is found, and then attempting to resume the deframing.
This case can be detected, because the payload of individual message contains the sequence number.

Following two chapters describe the format of individual debug messages, after the framing bytes are removed.

## Debug Message Version 1.0

Deprecated since 2005. Does not exist any more in any implementations.

## Debug Message Version 2.0

Used in Silicon Labs WSTK adapters as default version for Packet Captures (as of 2021).

```
Byte        Description
-----       -----------------------------------------------------------------------------
 0   Version number  \ LSB  ( contains 2 in version 2.0)
 1   Version number  / MSB  ( contains 0 in version 2.0)
 2   Timestamp byte 0  \ LSB, microsecond tics
 3   Timestamp byte 1  |
 4   Timestamp byte 2  |
 5   Timestamp byte 3  |
 6   Timestamp byte 4  |
 7   Timestamp byte 5  / MSB
 8   Debug Message Type \ LSB (coresponds to single byte message type.
 9   Debug Message Type / MSB
10   sequence number
...  Data dependent on the Message Type'
```

## Debug Message Version 3.0

Used in Silicon Labs WSTK adapters as an optional format, can be turned on as a default in the admin console. 
The only difference from 2.0 is, that it introduces nanosecond precision, 2-byte sequencing and few bytes of
future-proofing flags.

```
Byte        Description
-----       -----------------------------------------------------------------------------
 0   Version number  \ LSB  ( contains 3 in version 3.0)
 1   Version number  / MSB  ( contains 0 in version 3.0)
 2   Timestamp byte 0  \ LSB, nanosecond tics
 3   Timestamp byte 1  |
 4   Timestamp byte 2  |
 5   Timestamp byte 3  |
 6   Timestamp byte 4  |
 7   Timestamp byte 5  |
 8   Timestamp byte 6  |
 9   Timestamp byte 7  / MSB
10   Debug Message Type \ LSB (coresponds to single byte message type.
11   Debug Message Type / MSB
12   Flags \  LSB
13   Flags |
14   Flags |
15   Flags /  MSB
16   Sequence number \ LSB
17   Sequence number / MSB
...  Debug-message-type-specific payload.
```

## Type-specific debug message payload

Debug message type specific payload can really be anything, as long as its length fits into a 2-byte length size restriction.

Most typical debug message types are RX and TX packets for various
radio implementations, assertions, stack traces and other debugging
messages, advanced energy measurement data points, and similar.

Debug message types can be added over time, but once established, they don't change any more.

The types supported are listed [in this file](../silabs-pti/src/main/java/com/silabs/pti/debugchannel/DebugMessageType.java).