// Copyright (c) 2015 Silicon Labs. All rights reserved.

package com.silabs.pti.util;

import java.text.ParseException;

/**
 * Simple interface that converts string into an object.
 *
 * Created on Jan 19, 2016
 * @author timotej
 */
public interface IStringConverter<T> {

  /**
   * Given a data, object will be created.
   * If something goes wrong, parse exception is to be thrown.
   */
  public T convert(String data) throws ParseException;
}
