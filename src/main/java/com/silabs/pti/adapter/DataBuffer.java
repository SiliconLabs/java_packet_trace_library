// Copyright (c) 2011 Ember Corporation. All rights reserved.

package com.silabs.pti.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.silabs.pti.log.PtiLog;

/**
 * This is a producer/consumer class that is used by the peek data source
 * to speed up flushing of IP queues.
 *
 * If you do not use this class, then each reading thread is responsible for
 * pushing the debug message all the way through the synthesizer chain, thus
 * causing TCP/IP congestion, as it may not load the data from the incoming
 * TCP queues fast enough.
 *
 * If do DO use this class, then each reading thread quickly dumps their
 * messages into this buffer, then goes back to reading from the socket.
 * A separate thread is used to push the messages from here into the
 * data source.
 *
 * Created on Mar 13, 2012
 * @author timotej
 */
class DataBuffer<T> implements Runnable {

  private final List<T> buffer = new ArrayList<>(1000);
  private Thread fetchThread = null;
  private ThreadGroup threadGroup = null;

  private boolean stopThread;
  private final Consumer<T> listener;
  private final String consumerThreadName;

  public DataBuffer(final String consumerThreadName,
                    final Consumer<T> listener) {
    this(null, consumerThreadName, listener);
  }

  public DataBuffer(final ThreadGroup tg,
                    final String consumerThreadName,
                    final Consumer<T> listener) {
    this.threadGroup = tg;
    this.consumerThreadName = consumerThreadName;
    if ( listener == null )
      throw new IllegalArgumentException("Listener can't be null");
    this.listener = listener;
  }

  /** Starts the fetch thread */
  public void startThread() {
    if ( fetchThread != null ) return;
    stopThread = false;
    fetchThread = new Thread(threadGroup,
                             this,
                             consumerThreadName);
    fetchThread.start();
  }

  /** Stops the fetch thread */
  public void stopThread() {
    if ( fetchThread != null && fetchThread.isAlive() ) {
      stopThread = true;
      synchronized(buffer) {
        buffer.notifyAll();
      }
      try {
        if ( fetchThread != null )
          fetchThread.join();
      } catch (NullPointerException npe) {
        // It is possible for fetchThread to become null before join() is called
        // If it happens, no big deal, because the thread is dead anyway.
      } catch (InterruptedException ie) {
        PtiLog.warning("Could not wait for thread to die.", ie);
      }
    }
  }

  /**
   * This is the entry point for the listening threads.
   * Here we simply quickly dump message into the queue and get out.
   */
  public void addObject(final T object) {
    synchronized(buffer) {
      buffer.add(object);
      buffer.notify();
    }
  }

  // Returns null if thread was to be stopped and no data is there any more.
  private T fetchObject() throws InterruptedException {
    synchronized(buffer) {
      while(true) {
        // If there is stuff in the buffer, keep working
        if ( buffer.size() > 0 ) {
          return buffer.remove(0);
        }

        if ( stopThread )
          return null;

        buffer.wait();
      }
    }
  }

  @Override
  public void run() {
    try {
      while(true) {
        T dm = fetchObject();
        if ( dm == null ) {
          // No more data and stop thread, so we're done
          fetchThread = null;
          return;
        } else {
          listener.accept(dm);
        }
      }
    } catch (InterruptedException ie) {
      PtiLog.warning("Data buffer thread is interrupted", ie);
    }
  }
}
