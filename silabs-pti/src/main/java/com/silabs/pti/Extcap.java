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

package com.silabs.pti;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * When this jar file is used within wireshark, the extcap wireshark
 * functionality will pass 'extcap' as the first argument. If that happens then
 * we end up here.
 * 
 * @author timotej
 *
 */
public class Extcap {

  private static final String EC_INTERFACES = "--extcap-interfaces";
  
  private File logFile = null;
  private PrintWriter logWriter;
  private PrintStream extcapOut;
  private List<String> extcapArgs = new ArrayList<String>();
  
  /**
   * Execute extcap function. Args will contain 'extcap' as the first argument.
   * 
   * @param args
   * @return
   */
  public static final int run(String[] args) {
    return new Extcap(args).run();
  }

  private Extcap(String[] args) {
    String extcapLocation = System.getenv("EXTCAP_LOC");
    if ( extcapLocation != null ) {
      logFile = new File(extcapLocation, "silabs-pti.log");
    }
    extcapOut = System.out;
    for ( int i = 0; i<args.length; i++ ) {
      if ( i > 0 ) {
        extcapArgs.add(args[i]);
      }
    }
  }
  
  private void log(String s) {
    String d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
    logWriter.println(d + ": " + s);
  }
  
  private void extcapPrintln(String s) {
    log("extcap <= " + s);
    extcapOut.println(s);
  }
  
  private int run() {
    try (PrintWriter l = (logFile != null ? new PrintWriter(new FileWriter(logFile, true), true) : new PrintWriter(System.out))) {
      logWriter = l;
      StringBuilder sb = new StringBuilder("extcap => ");
      for ( String s: extcapArgs ) sb.append(" ").append(s);
      log(sb.toString());
      if ( extcapArgs.size() > 0 ) {
        switch(extcapArgs.get(0)) {
        case EC_INTERFACES:
          return extcapInterfaces();
        }
      }
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
    return 0;
  }
  
  private int extcapInterfaces() {
    extcapPrintln("extcap {version=1.0}{help=http://silabs.com}");
    extcapPrintln("interface {value=wstk1}{display=Silabs 1 display}");
    return 0;
  }
}
