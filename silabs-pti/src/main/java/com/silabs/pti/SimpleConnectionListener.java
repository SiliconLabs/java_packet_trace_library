package com.silabs.pti;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;

import com.silabs.pti.adapter.IConnectionListener;
import com.silabs.pti.adapter.TimeSynchronizer;
import com.silabs.pti.debugchannel.DebugMessage;
import com.silabs.pti.debugchannel.DebugMessageType;
import com.silabs.pti.debugchannel.EventType;
import com.silabs.pti.format.FileFormat;
import com.silabs.pti.format.IPtiFileFormat;

class SimpleConnectionListener implements IConnectionListener {
  private final FileFormat ff;
  private final String originator;
  private volatile int nReceived = 0;
  private final Map<String, PrintStream> output;
  private long t0 = -1;
  private boolean readAsText = true;
  private final TimeSynchronizer timeSync;

  // typically we capture from N devices and write to 1 single file.
  // this ensures us to only write 1 header entry.
  private static HashSet<PrintStream> writtenHeader = new HashSet<>();

  public SimpleConnectionListener(final FileFormat ff,
                                  final String originator,
                                  final Map<String, PrintStream> output,
                                  final boolean readText,
                                  final TimeSynchronizer timeSynchronizer) {
    this.ff = ff;
    this.originator = originator;
    this.output = output;
    this.readAsText = readText;
    this.timeSync = timeSynchronizer;

    // write header
    if (!readAsText) {
      output.forEach((k, v) -> {
        if (!writtenHeader.contains(v)) {
          if (ff.format().header() != null) {
            v.println(ff.format().header());
          }
          writtenHeader.add(v);
        }
      });
    }
  }

  public int count() {
    return nReceived;
  }

  @Override
  public void messageReceived(final byte[] message, final long pcTime) {
    PrintStream outputStream = output.get(originator);
    if (!readAsText) {
      long t;
      if (t0 == -1) {
        t0 = System.currentTimeMillis();
        t = 0;
      } else {
        t = System.currentTimeMillis() - t0;
      }
      String formatted = processDebugMsg(t, originator, message, timeSync, ff.format());
      if (formatted != null) {
        outputStream.println(formatted);
        nReceived++;
      }
    } else {
      outputStream.println(new String(message));
    }
  }

  @Override
  public void connectionStateChanged(final boolean isConnected) {
  }

  private static void timeCorrection(final TimeSynchronizer timeSync, final DebugMessage message) {
    long actualTime = timeSync.synchronizedTime(message.originatorId(), message.networkTime());
    message.setNetworkTime(actualTime);
  }

  /**
   * Takes a raw bytes and formats them.
   *
   * @return String
   */
  private static String processDebugMsg(final long timeMs,
                                        final String originator,
                                        final byte[] bytes,
                                        final TimeSynchronizer timeSync,
                                        IPtiFileFormat format) {
    if (format.isUsingRawBytes())
      return format.formatRawBytes(bytes, 0, bytes.length);

    DebugMessage dm = DebugMessage.make("", bytes, timeMs);
    EventType type = EventType.fromDebugMessage(DebugMessageType.get(dm.debugType()));

    // time correction
    SimpleConnectionListener.timeCorrection(timeSync, dm);
    return format.formatDebugMessage(originator, dm, type);
  }
}