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

package com.silabs.pti.adapter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 *
 * Used to send a message to an {@link IConnection} for which the sender wishes
 * to await a response. The sender should instantiate an instance of this class
 * by passing the {@link IConnection} they wish to use to the constructor. Once
 * this has been done, the sender may send a blocking message to the

 * @author ezra
 *
 */
public class ExpectConnection {

  private static final boolean LOG = false;

  private static int DEFAULT_TIMEOUT = 2000;
  private IConnection connection = null;
  private StringBuffer collectedOutput = null;
  private String currentOutput = null;
  private final Object expectMonitor = new Object();
  private Pattern pattern = null;
  private boolean complete = false;

  public ExpectConnection(final IConnection connection) {
    this.connection = connection;
  }

  /**
   * This method is functionally the same as
   * expect where 2000ms is used as the
   * timeout and the collect flag is set to false.
   *
   * @param message
   *          the message to send.
   * @param regex
   *          the regular expression to look for.
   * @return the matching message, or a concatenation of all incoming messages
   *         up to and including the match if <code>collect</code> is true.
   *         Returns <code>null</code> if nothing matched.
   * @see #expect(String, String, int, boolean)
   */
  public String expect(final String message, final String regex) {
    return expect(message, regex, DEFAULT_TIMEOUT, false);
  }

  /**
   * Sends a message to the device and waits <code>timeout</code> milliseconds
   * to receive an incoming message matching <code>regex</code>. Returns the
   * matching message. If <code>collect</code> is true, concatenates and
   * returns all incoming messages up to and including the match. If
   * <code>regex</code> is null, no matching is attempted. This is useful when
   * collecting all responses to a message without knowing in advance what they
   * are.
   * <p>
   * For the purpose of this method, incoming messages are converted to Strings
   * using the {@link IFramer#toString(byte[])} method of the current
   * <code>IFramer</code> within the <code>IConnection</code> passed at the
   * time this object was created. Messages are concatenated with "\r\n". See
   * {@link String#matches(String)} for details on matching.
   *
   * @param message
   *          the message to send.
   * @param regex
   *          the regular expression to look for.
   * @param timeout
   *          milliseconds to wait before giving up.
   * @param collect
   *          set to true to also return messages preceeding the match.
   * @return the matching message, or a concatenation of all incoming messages
   *         up to and including the match if <code>collect</code> is true.
   *         Returns <code>null</code> if nothing matched.
   * @see #expect(String, String)
   */
  public String expect(final String message,
                       final String regex,
                       final int timeout,
                       final boolean collect) {
    ExpectResponse r = expect(message, regex, timeout);
    if (r.succeeded())
      if (collect)
        return r.collectedOutput();
      else
        return r.matchedOutput();
    else
      return null;
  }

  /**
   * This is the preferred method of using the expect connection. It is
   * basically the same as {@link #expect(String, String, int, boolean)} with
   * the added functionality of returning an {@link ExpectResponse} object which
   * contains complete information about what happened with the sent message.
   *
   * @param message
   *          The message to send to the connection provided when this
   *          {@link #ExpectConnection(IConnection)} was created.
   * @param regex
   *          The regular expression used to match against the response to the
   *          message provided.
   * @param timeout
   *          The amount of time to wait before returning either a failed
   *          response or (in the case where regex is null), the collected
   *          output heard back from the message sent.
   * @return An instance of {@link ExpectResponse} which contains information
   *         about action's outcome.
   */
  public synchronized ExpectResponse expect(final String message,
                                            final String regex,
                                            final int timeout) {
    if(LOG)System.err.println(Thread.currentThread().hashCode() + ": EXPECT: " + message + " / " + regex);
    // Param verify.
    if (!connection.isConnected()) {
      return new ExpectResponse(false, "No connection.", null, null);
    }

    // Initialize.
    IConnectionListener listener = new ExpectListener(this);
    long startTime = System.currentTimeMillis();
    int waitTime = timeout - (int) (System.currentTimeMillis() - startTime);
    if (regex != null)
      pattern = Pattern.compile("(?sm).*^(" + regex + ")$.*");
    else
      pattern = null;
    collectedOutput = new StringBuffer();
    currentOutput = null;
    complete = false;

    // Send.
    connection.addConnectionListener(listener);
    try {
      synchronized (expectMonitor) {
        connection.send(message);
        expectMonitor.wait(waitTime);
      }
    } catch (InterruptedException e) {
      // Hmmm? Why do we do nothing here?
    } catch (IOException ioe) {
      // Hmmm? Why do we do nothing here?
    } finally {
      connection.removeConnectionListener(listener);
    }

    // Evaluate response.
    if(LOG)System.err.println(Thread.currentThread().hashCode() + ": COLLECTED: " + collectedOutput.toString());
    ExpectResponse response;
    if (pattern == null
        || pattern.matcher(collectedOutput.toString()).matches() ) {
      if(LOG)System.err.println(Thread.currentThread().hashCode() + ": MATCHES!!!!!!!!!!");
      response = new ExpectResponse(true,
                                    null,
                                    currentOutput,
                                    collectedOutput.toString());
    } else {
      if(LOG)System.err.println(Thread.currentThread().hashCode() + ": NO MATCH... :(");
      response = new ExpectResponse(false,
                                    "No match.",
                                    null,
                                    collectedOutput.toString());
    }

    return response;
  }
  /**
   * This method is used by the {@link IConnection} to pass data received back
   * to be processed by the blocking thread which originally called
   * {@link #expect(String, String, int, boolean)}).
   *
   * @param messageBytes
   *          The bytes which were received by the {@link IConnection}
   */
  protected void receive(final byte[] messageBytes) {
    if (!complete) {
      String message;
      if (connection.incomingFramer() != null)
        message = connection.incomingFramer().toString(messageBytes);
      else
        message = new String(messageBytes);
      currentOutput = new String(message);
      collectedOutput.append(message).append("\r\n");
      if (pattern != null &&
          pattern.matcher(collectedOutput.toString()).matches()) {
        complete = true;
        synchronized (expectMonitor) {
          expectMonitor.notify();
        }
      }
    }
  }
}


class ExpectListener implements IConnectionListener {
  private ExpectConnection expect = null;

  public ExpectListener(final ExpectConnection expect) {
    this.expect = expect;
  }

  @Override
  public void connectionStateChanged(final boolean isConnected) {}

  @Override
  public void messageReceived(final byte[] message, long pcTime) {
    expect.receive(message);
  }
}
