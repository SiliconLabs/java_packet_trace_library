//Copyright 2005 Ember Corporation. All rights reserved.

package com.silabs.pti.util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * @author ezra
 *
 * This class contains all utilities related with time, particularly
 * the formatting
 *
 */
public class Time {
  public enum Format {
    MICROSECOND_LONG(false),
    MICROSECOND_SHORT(false),
    MICROSECOND_DURATION(false),
    MICROSECOND_FRIENDLY(false),
    MILLISECOND_DAYTIME(true),
    MILLISECOND_SHORT(true),
    MILLISECOND_DURATION(false),
    MILLISECOND_FRIENDLY(true);
    private final boolean realTime; // True if expected value is real time in millis
    Format(final boolean realTime) {
      this.realTime = realTime;
    }
    public boolean isRealTime() { return realTime; }
  };

  private static DateFormat realTimeFormatter
    = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
  private static NumberFormat timeFormatter = NumberFormat.getInstance();
  private static NumberFormat shortTimeFormatter = NumberFormat.getInstance();
  private static NumberFormat durationFormatter = NumberFormat.getInstance();
  private static DateFormat preciseTimeFormatter
    = new SimpleDateFormat("HH:mm:ss.SSS");
  private static SimpleDateFormat fullFormat
    = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
  private static SimpleDateFormat yearlessFormat
    = new SimpleDateFormat("MMM d, HH:mm:ss");
  private static SimpleDateFormat datelessFormat
    = new SimpleDateFormat("HH:mm:ss");
  private static NumberFormat packetRateFormatter = NumberFormat.getInstance();
  private static SimpleDateFormat dateOnlyFormat
    = new SimpleDateFormat("MMM d, yyyy");
  private static SimpleDateFormat yearlessDateOnlyFormat
    = new SimpleDateFormat("MMM d");

  static {
    timeFormatter.setMaximumFractionDigits(6);
    timeFormatter.setMinimumFractionDigits(6);
    timeFormatter.setParseIntegerOnly(false);
    shortTimeFormatter.setMaximumFractionDigits(3);
    shortTimeFormatter.setMinimumFractionDigits(3);
    shortTimeFormatter.setParseIntegerOnly(false);
    durationFormatter.setMaximumFractionDigits(3);
    durationFormatter.setMinimumFractionDigits(3);
    durationFormatter.setParseIntegerOnly(false);
    packetRateFormatter.setMinimumFractionDigits(2);
  }

  /** Formater that uses the format to format. */
  public static String format(final long time, final Format format) {
    switch(format) {
    case MICROSECOND_LONG: return formatMicrosecondTime(time);
    case MICROSECOND_SHORT: return formatShortMicrosecondTime(time);
    case MICROSECOND_FRIENDLY: return formatFriendlyMicrosecondTime(time);
    case MICROSECOND_DURATION: return formatMicrosecondDuration(time);
    case MILLISECOND_DAYTIME: return formatMillisecondDayTime(time);
    case MILLISECOND_SHORT: return formatRealTime(time);
    case MILLISECOND_DURATION: return formatMillisecondDuration(time);
    case MILLISECOND_FRIENDLY: return formatFriendlyMillisecondTime(time);
    }
    return "";
  }
  /**
   * Shows the time as useful in testing. H:M:S.millisec
   */
  public static String formatMillisecondDayTime(final long millisecondTime) {
    return preciseTimeFormatter.format(new Date(millisecondTime));
  }

  // Formats the real time
  public static String formatRealTime(final long systemTimeMillis) {
    return realTimeFormatter.format(new Date(systemTimeMillis));
  }

  public static String formatPacketRate(final double pps) {
    if ( pps == Double.NaN ) {
      return "? p/s";
    } else {
      return packetRateFormatter.format(pps) + " p/s";
    }
  }

  // Formats the long time as we use it, into a string for display
  public static String formatMicrosecondTime(final long microsecondTime) {
    if ( microsecondTime == Long.MIN_VALUE )
      return "-" + SpecialCharacter.INFINITY;
    else if ( microsecondTime == Long.MAX_VALUE )
      return "+" + SpecialCharacter.INFINITY;
    else
      return timeFormatter.format(microsecondTime / 1000000.0);
  }

  // Formats the long time as we use it, into a string for display
  // where we need only 3 digits
  public static String formatShortMicrosecondTime(final long microsecondTime) {
    if ( microsecondTime == Long.MIN_VALUE )
      return "-" + SpecialCharacter.INFINITY;
    else if ( microsecondTime == Long.MAX_VALUE )
      return "+" + SpecialCharacter.INFINITY;
    else
    return shortTimeFormatter.format(microsecondTime / 1000000.0);
  }

  // Formats the long time as we use it, into a string for display
  // where we need only 3 digits, but we strip out minutes here.
  public static String formatFriendlyMicrosecondTime(final long microsecondTime) {
    if ( microsecondTime == Long.MIN_VALUE )
      return "-" + SpecialCharacter.INFINITY;
    else if ( microsecondTime == Long.MAX_VALUE )
      return "+" + SpecialCharacter.INFINITY;
    else {
      if ( microsecondTime > 60000000 ) {
        long m = microsecondTime / 60000000;
        long r = microsecondTime % 60000000;
        return m + "m," + shortTimeFormatter.format(r / 1000000.0);
      } else {
        return shortTimeFormatter.format(microsecondTime / 1000000.0);
      }
    }
  }

  /**
   * Create humanly readable time string out of milliseconds.
   * Example: 171857 (ms) -> to "2 Minutes 51 Seconds 875 Milliseconds" String
   *
   * @param
   * @returns String
   */
  public static String formatMillisecondsToFrendlyRead(final long duration) {
    long durationMsec = duration;
    int daysInYear = 365;
    int hoursInDay = 24;
    int minutesInHour = 60;
    int secondsInMinute = 60;
    int millisecondsInSeconds = 1000;

    long buffer = (long)daysInYear * hoursInDay * minutesInHour * secondsInMinute * millisecondsInSeconds;

    int mYear = (int)(durationMsec / buffer); // number of years
    durationMsec %= buffer; // removed the years
    buffer /= daysInYear;
    int mDay = (int)(durationMsec / buffer); // number of days
    durationMsec %= buffer; // removed the days
    buffer /= hoursInDay;
    int hr = (int)(durationMsec / buffer); // number of hours
    durationMsec %= buffer; // removed the hours
    buffer /= minutesInHour;
    int min = (int)(durationMsec / buffer); // number of minutes
    durationMsec %= buffer; // removed the minutes
    buffer /= secondsInMinute;
    int sec = (int)(durationMsec / buffer); // number of seconds
    durationMsec %= buffer; // removed the seconds
    buffer /= millisecondsInSeconds;

    StringBuilder timeStringBuilder = new StringBuilder();
    boolean timeBuildStarted = false;
    if (mYear > 0) {
      timeStringBuilder.append(mYear + " Year" + (mYear == 1 ? "" : "s") + " ");
      timeBuildStarted = true;
    }

    if (mDay > 0 || timeBuildStarted) {
      timeStringBuilder.append(mDay + " Day" + (mDay == 1 ? "" : "s") + " ");
      timeBuildStarted = true;
    }

    if (hr > 0 || timeBuildStarted) {
      timeStringBuilder.append(hr + " Hour" + (hr == 1 ? "" : "s") + " ");
      timeBuildStarted = true;
    }

    if (min > 0 || timeBuildStarted) {
      timeStringBuilder.append(min + " Minute" + (min == 1 ? "" : "s") + " ");
      timeBuildStarted = true;
    }

    if (sec > 0 || timeBuildStarted) {
      timeStringBuilder.append(sec + " Second" + (sec == 1 ? "" : "s") + " ");
      timeBuildStarted = true;
    }

    timeStringBuilder.append(durationMsec + " Millisecond" + (durationMsec == 1 ? "" : "s") + " ");
    return timeStringBuilder.toString().trim();
  }

  // Formats the time difference (long time 1 - long time 0) into gui format
  public static String formatMicrosecondDuration(final long duration) {
    return durationFormatter.format(duration / 1000000.0);
  }

  // Formats the time difference (long time 1 - long time 0) into gui format
  public static String formatMillisecondDuration(final long duration) {
    return durationFormatter.format(duration / 1000.0);
  }

  // Parses the time from string to long. String is in the same format as
  // formatTime prints it out.
  public static long parseMicrosecondTime(final String microsecondTime)
    throws ParseException {
    if ( microsecondTime.equals("-INF") ) return Long.MIN_VALUE;
    if ( microsecondTime.equals("-" + SpecialCharacter.INFINITY) ) return Long.MIN_VALUE;
    if ( microsecondTime.equals("+INF") ) return Long.MAX_VALUE;
    if ( microsecondTime.equals("+" + SpecialCharacter.INFINITY) ) return Long.MAX_VALUE;
    long t;
    Object o = timeFormatter.parse(microsecondTime);
    if ( o instanceof Long )
      t = (((Long)o).longValue() * 1000000);
    else if ( o instanceof Double )
      t = (long)(((Double)o).doubleValue()*1000000);
    else
      throw new ParseException("Parsed into neither long nor double", 0);
    return t;
  }

  /** Shows date only in a friendly format */
  public static String formatFriendlyMillisecondDate(final long realMillisecondTime) {
    // We put "Today", "Yesterday",
    // instead of date for today and yesterday.
    // Golden rules of GUI development. :)
    Calendar resultTime = Calendar.getInstance();
    resultTime.setTime(new Date(realMillisecondTime));
    Calendar now = Calendar.getInstance();
    Calendar midnight = Calendar.getInstance();
    midnight.set(Calendar.HOUR_OF_DAY, 0);
    midnight.set(Calendar.MINUTE, 0);
    midnight.set(Calendar.SECOND, 0);
    Calendar yesterdayMidnight = Calendar.getInstance();
    yesterdayMidnight.set(Calendar.HOUR_OF_DAY, 0);
    yesterdayMidnight.set(Calendar.MINUTE, 0);
    yesterdayMidnight.set(Calendar.SECOND, 0);
    yesterdayMidnight.add(Calendar.DAY_OF_YEAR, -1);

    StringBuffer sb = new StringBuffer();
    if ( resultTime.after(midnight) ) {
      sb.append("Today");
    } else if ( resultTime.after(yesterdayMidnight) ) {
      sb.append("Yesterday");
    } else if ( resultTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
      sb.append(yearlessDateOnlyFormat.format(resultTime.getTime()));
    } else {
      sb.append(dateOnlyFormat.format(resultTime.getTime()));
    }
    return sb.toString();

  }

  /**
   * Returns a friendly date string. "Friendly" because it
   * replaces dates with "Today" and "Yesterday" where fitting.
   */
  public static String formatFriendlyMillisecondTime(final long realMillisecondTime) {
    // We put "Today", "Yesterday",
    // instead of date for today and yesterday.
    // Golden rules of GUI development. :)
    Calendar resultTime = Calendar.getInstance();
    resultTime.setTime(new Date(realMillisecondTime));
    Calendar now = Calendar.getInstance();
    Calendar midnight = Calendar.getInstance();
    midnight.set(Calendar.HOUR_OF_DAY, 0);
    midnight.set(Calendar.MINUTE, 0);
    midnight.set(Calendar.SECOND, 0);
    Calendar yesterdayMidnight = Calendar.getInstance();
    yesterdayMidnight.set(Calendar.HOUR_OF_DAY, 0);
    yesterdayMidnight.set(Calendar.MINUTE, 0);
    yesterdayMidnight.set(Calendar.SECOND, 0);
    yesterdayMidnight.add(Calendar.DAY_OF_YEAR, -1);

    StringBuffer sb = new StringBuffer();
    if ( resultTime.after(midnight) ) {
      sb.append("Today, ");
      sb.append(datelessFormat.format(resultTime.getTime()));
    } else if ( resultTime.after(yesterdayMidnight) ) {
        sb.append("Yesterday, ");
        sb.append(datelessFormat.format(resultTime.getTime()));
    } else if ( resultTime.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
      sb.append(yearlessFormat.format(resultTime.getTime()));
    } else {
      sb.append(fullFormat.format(resultTime.getTime()));
    }
    return sb.toString();
  }

  /**
   * Returns the current year as a simple integer.
   *
   * @returns int
   */
  public static int year() {
    Calendar c = Calendar.getInstance();
    return c.get(Calendar.YEAR);
  }
}
