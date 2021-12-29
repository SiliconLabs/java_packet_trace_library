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

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;

import com.silabs.pti.adapter.IConnectionListener;
import com.silabs.pti.adapter.TimeSynchronizer;
import com.silabs.pti.format.FileFormat;
import com.silabs.pti.format.IPtiFileFormat;

/**
 * Connection listener, responsible for forwarding the data into the appropriate
 * formater.
 * 
 * @author timotej
 *
 */
public class DebugMessageConnectionListener implements IConnectionListener {
  private final IPtiFileFormat ptiFormat;
  private final String originator;
  private volatile int nReceived = 0;
  private final Map<String, PrintStream> output;
  private long t0 = -1;
  private boolean readAsText = true;
  private final TimeSynchronizer timeSync;

  // typically we capture from N devices and write to 1 single file.
  // this ensures us to only write 1 header entry.
  private static HashSet<PrintStream> writtenHeader = new HashSet<>();

  public DebugMessageConnectionListener(final IPtiFileFormat format,
                                        final String originator,
                                        final Map<String, PrintStream> output,
                                        final boolean readText,
                                        final TimeSynchronizer timeSynchronizer) {
    this.ptiFormat = format;
    this.originator = originator;
    this.output = output;
    this.readAsText = readText;
    this.timeSync = timeSynchronizer;

    // write header
    if (!readAsText) {
      output.forEach((k, v) -> {
        if (!writtenHeader.contains(v)) {
          if (ptiFormat.header() != null) {
            v.println(ptiFormat.header());
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
      String formatted = processDebugMsg(t, originator, message, timeSync, ptiFormat);
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
    DebugMessageConnectionListener.timeCorrection(timeSync, dm);
    return format.formatDebugMessage(originator, dm, type);
  }
}