# Silabs Debug Channel Format

## Framing

Debug channel data is a stream of data. The Data is framed into individual "debug messages".
The overall format includes the framing information, in the following way.

```
Byte        Description
-----       -----------------------------------------------------------------------------
 0          Initial framing: '['
 1          Length  \ LSB, includes everything except the framing '[' and ']' characters.
 2          Length  / MSB
 length-2   Individual debug message, dependent on the version and type.
 N          End framing:  ']'
```

All tooling that deals with the discrete debug messages is expected to remove the stream framing, so what is left is individual 
debug messages, without the framing and the length bytes. Therefore, any discrete per-event file formats, such as PCAP or PCAPNG, are expected to hold just the individual debug messages, without the stripped `[`, `]` characters, or the two length bytes.

A stray `]` inside a content does not trigger a false end-of-message, since the reader is expected to simply skip the number of characters indicated in the two-byte length, and look for a `]` only after that. That ensures a decent recovery if a stream gets broken.

The following two chapters describe the format of individual debug messages, after the framing bytes are removed.

## Debug Message Version 1.0

Deprecated since 2005. Does not exist any more in any implementations.

## Debug Message Version 2.0

Used in Silicon Labs WSTK adapters as the default version for Packet Captures (as of 2021).

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

Debug Message Version 3. 0 is used in Silicon Labs WSTK adapters as an optional format. It can be turned on as a default in the admin console. 
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
12   Flags \  LSB  (Reserved for future use, contains arbitrary values.)
13   Flags |
14   Flags |
15   Flags /  MSB
16   Sequence number \ LSB
17   Sequence number / MSB
...  Debug-message-type-specific payload.
```

Note regarding the "arbitrary value" bytes: The systems that read this are typically embedded systems, for which the knowledge of actual length of message headers matters. In that environment it is useful to know that next minor version update might start using those fields, while the overall message size doesn't change. So if you have a version 3.0, for example, the value in those bytes is ignored. But if your toplevel format version is 3.1, then the values in some of those fields become important. However, because the overall size of the messages didn't change, all the readers that know how to read 3.0 will not break because those fields gain meaningful data points, they will simply continue ignoring them instead.

Note regarding the "sequence numbers": Sequence number is used to detect if there were gaps in the stream of messages. This is extremely important, as this indicates that somewhere in the long chain of travel of these messages from the originating system, down to your PC which analyzes this data, some firmware ran out of buffer space or memory or something like that, so the whole chain needs to be analyzed and a faulting hardware/software needs to be updated for higher bandwidth.
It's what tells you that a "missing transmission" in your analysis is possibly due to your instrumentation, not because the transmission didn't happen on the actual radio system being analyzed.

## Type-specific debug message payload

Debug message type specific payloads can really be anything, as long as their length fits into a 2-byte length size restriction.

Most typical debug message types are RX and TX packets for various
radio implementations, assertions, stack traces and other debugging
messages, advanced energy measurement data points, and similar.

Debug message types can be added over time, but once established, they cannot change.

The types are described [here](debug-message-types.md).
