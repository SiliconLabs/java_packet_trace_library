// Copyright 2016 Silicon Laboratories, Inc.

package com.silabs.pti.log;

import java.io.PrintStream;

/**
 * Default logger implementation if there is no eclipse environment.
 *
 * Created on Feb 13, 2017
 * @author timotej
 * @since 4.6
 */
class DefaultLogger implements IPtiLogger {

  @Override
  public void log(final PtiSeverity severity,
                  final String message,
                  final Throwable throwable) {
    PrintStream ps = null;
    switch(severity) {
    case ERROR: ps = System.err; break;
    case WARNING: ps = System.out; break;
    case INFO: ps = System.out; break;
    case NONE: ps = null; break;
    }
    if ( ps != null ) {
      ps.println(severity.name() + ": " + message);
      if ( throwable != null ) {
        ps.println("Exception: " + throwable.getMessage());
        throwable.printStackTrace(ps);
      }
    }
  }

}
