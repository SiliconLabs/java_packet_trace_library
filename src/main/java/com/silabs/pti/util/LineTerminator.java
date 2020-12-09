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

package com.silabs.pti.util;

/**
 * Common line terminator enum for all supported line terminations.
 * We need to support different formats from the adapter, so this 
 * centralizes all the line-terminator handling in ISD.
 * 
 * Created on Aug 28, 2008 
 * @author Timotej (timotej@ember.com)
 */
public enum LineTerminator {
  NONE ("",     "None.", null),
  LF   ("\n",   "LF",    "Linux, Unix-like, Mac OS X"),
  CRLF ("\r\n", "CR-LF", "DOS, OS/2, MS Windows"),
  CR   ("\r",   "CR",    "Mac OS up to 9, C64"),
  NEL  ("\u0085", "NEL", "Unicode: Next-Line" ),
  FF   ("\u000C", "FF",  "Unicode: Form-Feed" ),
  LS   ("\u2028", "LS",  "Unicode: Line-separator" ),
  PS   ("\u2029", "PS",  "Unicode: Paragraph-separator" );

  private String terminator;
  private String shortDescription;
  private String platforms;
  private LineTerminator(String terminator, 
                         String shortDescription,
                         String platforms) {
    this.terminator = terminator;
    this.shortDescription = shortDescription;
    this.platforms = platforms;
  }

  private static String localTerminatorS = System.getProperty("line.separator");
  private static LineTerminator localTerminator = null; 
  
  /** Returns a local platform-specific line terminator */
  public static String local() { return localTerminatorS; }
  
  public static LineTerminator localTerminator() {
    if ( localTerminator == null ) {
      for ( LineTerminator lt: values() ) {
        if ( lt.terminator().equals(localTerminatorS)) {
          localTerminator = lt;
          break;
        }
      }
    }
    return localTerminator;
  }
  
  /** Returns a line terminator that we use for code generation */
  public static LineTerminator forCodeGeneration() { return LF; }
  
  /** Returns multiple consecutive platform-specific line terminators */
  public static String local(int n) { 
    StringBuilder sb = new StringBuilder();
    for ( int i=0; i<n; i++ ) {
      sb.append(localTerminatorS);
    }
    return sb.toString();
  }
  
  /** creates n consecutive line terminations */
  public String times(int n) {
    StringBuilder sb = new StringBuilder();
    for ( int i=0; i<n; i++ ) {
      sb.append(terminator);
    }
    return sb.toString();
  }
  /** returns single terminator */
  public String terminator() { return terminator; }
  
  /** Simple description, such as "CR", or "CR-LF" */
  public String shortDescription() { return shortDescription; }
  
  /** Longer description, which looks like: "LF (Linux, Unix-like)" or so */
  public String longDescription() {
    if ( platforms == null ) 
      return shortDescription;
    else 
      return shortDescription + "  (" + platforms + ")";
  }
  @Override
  public String toString() { return terminator; }
}
