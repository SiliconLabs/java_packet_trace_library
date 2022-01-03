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

/**
 * Simple enum that deals with severities where you need them.
 *
 * Created on Sep 15, 2014
 * 
 * @author timotej
 */
public enum PtiSeverity {
  // These are sorted by decreasing severity order. Don't change that!!
  ERROR, WARNING, INFO, NONE;

}