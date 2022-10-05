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

import java.io.IOException;
import java.util.HashSet;

import com.silabs.pti.OutputMap;
import com.silabs.pti.adapter.IConnectionListener;
import com.silabs.pti.adapter.TimeSynchronizer;
import com.silabs.pti.filter.IDebugMessageFilter;
import com.silabs.pti.format.IDebugChannelExportFormat;
import com.silabs.pti.format.IDebugChannelExportOutput;
import com.silabs.pti.log.PtiLog;

/**
 * Connection listener, responsible for forwarding the data into the appropriate
 * formater.
 *
 * @author timotej
 *
 */
public class DebugMessageConnectionListener<T> implements IConnectionListener {
  private final IDebugChannelExportFormat<T> ptiFormat;
  private final String originator;
  private volatile int nReceived = 0;
  private final OutputMap<T> output;
  private long t0 = -1;
  private final TimeSynchronizer timeSync;

  private IDebugMessageFilter filter = null;


  // typically we capture from N devices and write to 1 single file.
  // this ensures us to only write 1 header entry.
  private static HashSet<IDebugChannelExportOutput<?>> writtenHeader = new HashSet<>();

  public DebugMessageConnectionListener(final IDebugChannelExportFormat<T> format,
                                        final String originator,
                                        final OutputMap<T> output,
                                        final TimeSynchronizer timeSynchronizer) {
    this.ptiFormat = format;
    this.originator = originator;
    this.output = output;
    this.timeSync = timeSynchronizer;

    for (final IDebugChannelExportOutput<T> v : output.values()) {
      if (!writtenHeader.contains(v)) {
        try {
          format.writeHeader(v.writer());
        } catch (final IOException ioe) {
          PtiLog.error("Could not write header.", ioe);
        }
        writtenHeader.add(v);
      }
    }
  }

  @Override
  public int count() {
    return nReceived;
  }

  @Override
  public void messageReceived(final byte[] message, final long pcTime) {
    @SuppressWarnings("resource")
    final IDebugChannelExportOutput<T> outputStream = output.output(originator);
    long t;
    if (t0 == -1) {
      t0 = System.currentTimeMillis();
      t = 0;
    } else {
      t = System.currentTimeMillis() - t0;
    }
    try {
      if (processDebugMsg(outputStream, t, originator, message, timeSync, ptiFormat, filter))
        nReceived++;
    } catch (final IOException ioe) {
      PtiLog.error("Can't write output file", ioe);
    }
  }

  @Override
  public void connectionStateChanged(final boolean isConnected) {
  }

  public void setFilter(final IDebugMessageFilter debugMessageFilter) {
    this.filter = debugMessageFilter;
  }

  private static void timeCorrection(final TimeSynchronizer timeSync, final DebugMessage message) {
    final long actualTime = timeSync.synchronizedTime(message.originatorId(), message.networkTime());
    message.setNetworkTime(actualTime);
  }

  /**
   * Takes a raw bytes and formats them.
   *
   * @return String
   */
  private static <T> boolean processDebugMsg(final IDebugChannelExportOutput<T> outputStream,
                                             final long timeMs,
                                             final String originator,
                                             final byte[] bytes,
                                             final TimeSynchronizer timeSync,
                                             final IDebugChannelExportFormat<T> format,
                                             final IDebugMessageFilter filter) throws IOException {
    if (format.isUsingRawBytes()) {

      // If we have a filter, we need to create a debug message just to deal with filtering.
      if ( filter != null ) {
        final DebugMessage dm = DebugMessage.make("", bytes, timeMs);
        if ( !filter.isMessageKept(dm))
          return false;
      }

      return format.formatRawBytes(outputStream.writer(), timeMs, bytes, 0, bytes.length);
    } else {
      final DebugMessage dm = DebugMessage.make("", bytes, timeMs);

      // If we have a filter, apply it.
      if ( filter != null && !filter.isMessageKept(dm))
        return false;

      final DebugMessageType dmt = DebugMessageType.get(dm.debugType());
      final EventType type = EventType.fromDebugMessage(dmt);
      // time correction
      DebugMessageConnectionListener.timeCorrection(timeSync, dm);
      return format.formatDebugMessage(outputStream.writer(), originator, dm, type);
    }
  }
}