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
