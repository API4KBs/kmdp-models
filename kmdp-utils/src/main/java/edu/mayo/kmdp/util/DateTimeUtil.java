/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.util;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;
import static java.util.Date.from;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class DateTimeUtil {

  public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
  public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  static final DateTimeFormatter dateFormatter =
      new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE).toFormatter();

  static final DateTimeFormatter dateTimeFormatter =
      new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_OFFSET_DATE_TIME).toFormatter();

  static final DateTimeFormatter localTimeFormatter =
      new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_TIME).toFormatter();

  static final DateTimeFormatter localDateTimeFormatter =
      new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
          .optionalStart().appendOffsetId().toFormatter();


  private DateTimeUtil() {
    // functions only
  }

  public static boolean validateDate(String dtStr) {
    return canParse(dtStr, dateFormatter);
  }

  public static boolean validateDateTime(String timeStr) {
    return canParse(timeStr, dateTimeFormatter);
  }

  public static boolean validateLocalDateTime(String dateTimeStr) {
    return canParse(dateTimeStr, localDateTimeFormatter);
  }

  public static boolean validateLocalTime(String instStr) {
    return canParse(instStr, localTimeFormatter);
  }


  public static String serializeAsDateTime(Date dt) {
    if (dt == null) {
      return null;
    }
    return serializeAsDateTime(toZonedDateTime(dt));
  }

  public static String serializeAsDateTime(LocalDateTime dt) {
    if (dt == null) {
      return null;
    }
    return serializeAsDateTime(toZonedDateTime(dt));
  }

  public static String serializeAsDateTime(ZonedDateTime dt) {
    if (dt == null) {
      return null;
    }
    return dateTimeFormatter.format(dt);
  }

  public static String serializeAsLocalDateTime(Date dt) {
    if (dt == null) {
      return null;
    }
    return serializeAsLocalDateTime(toLocalDateTime(dt));
  }

  public static String serializeAsLocalDateTime(LocalDateTime dt) {
    if (dt == null) {
      return null;
    }
    return serializeAsLocalDateTime(toZonedDateTime(dt));
  }

  public static String serializeAsLocalDateTime(ZonedDateTime dt) {
    if (dt == null) {
      return null;
    }
    return localDateTimeFormatter.format(dt.truncatedTo(ChronoUnit.SECONDS));
  }

  public static String serializeAsDate(Date dt) {
    if (dt == null) {
      return null;
    }
    return serializeAsDate(toLocalDateTime(dt));
  }

  public static String serializeAsDate(LocalDateTime dt) {
    if (dt == null) {
      return null;
    }
    return dateFormatter.format(dt);
  }

  public static String serializeAsDate(ZonedDateTime dt) {
    if (dt == null) {
      return null;
    }
    return dateFormatter.format(dt);
  }


  public static String serializeAsTime(Date dt) {
    if (dt == null) {
      return null;
    }
    return serializeAsTime(toLocalDateTime(dt));
  }

  public static String serializeAsTime(LocalDateTime dt) {
    if (dt == null) {
      return null;
    }
    return localTimeFormatter.format(dt.truncatedTo(ChronoUnit.SECONDS));
  }

  public static String serializeAsTime(ZonedDateTime dt) {
    if (dt == null) {
      return null;
    }
    return serializeAsTime(dt.toLocalDateTime());
  }

  public static String serializeZonedDateTime(ZonedDateTime dt, String format) {
    return serializeZonedDateTime(dt, DateTimeFormatter.ofPattern(format));
  }

  public static String serializeZonedDateTime(ZonedDateTime dt, DateTimeFormatter formatter) {
    if (dt == null) {
      return null;
    }
    return formatter.format(dt);
  }

  public static String serializeLocalDateTime(LocalDateTime dt, String format) {
    return serializeLocalDateTime(dt, DateTimeFormatter.ofPattern(format));
  }

  public static String serializeLocalDateTime(LocalDateTime dt, DateTimeFormatter formatter) {
    if (dt == null) {
      return null;
    }
    return formatter.format(dt);
  }

  public static String serializeDate(Date dt, String format) {
    return serializeDate(dt, DateTimeFormatter.ofPattern(format));
  }

  public static String serializeDate(Date dt, DateTimeFormatter formatter) {
    if (dt == null) {
      return null;
    }
    return formatter.format(toLocalDateTime(dt));
  }



  public static Date parseDateTime(String dateTimeStr) {
    return fromZonedDateTime(ZonedDateTime.parse(dateTimeStr, dateTimeFormatter));
  }

  public static Date parseLocalDateTime(String localDateTimeStr) {
    return fromZonedDateTime(ZonedDateTime.parse(localDateTimeStr, localDateTimeFormatter));
  }

  public static Date parseDate(String localDateStr) {
    return fromLocalDate(LocalDate.parse(localDateStr, dateFormatter));
  }

  public static Date parseLocalTime(String localTimeStr) {
    LocalTime time = LocalTime.parse(localTimeStr);
    return calendarToTime(time);
  }

  public static Date dateToTime(Date date) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(date);
    return calendarToTime(cal);
  }

  public static Date parseDate(String dateStr, String pattern) {
    return tryParseDate(dateStr, pattern).orElse(null);
  }

  public static List<Date> parseDates(List<String> dateStr) {
    return dateStr.stream().map(DateTimeUtil::parseDate).collect(Collectors.toList());
  }

  public static List<Date> parseDates(List<String> dateStr, String pattern) {
    return dateStr.stream().map(d -> parseDate(d, pattern)).collect(Collectors.toList());
  }

  public static Date parseDateOrNow(String dateStr, String pattern) {
    return tryParseDate(dateStr, pattern).orElseGet(Date::new);
  }

  public static Date parseDateTime(String dateStr, String pattern) {
    return tryParseDateTime(dateStr, pattern).orElse(null);
  }

  public static List<Date> parseDateTimes(List<String> dateStr, String pattern) {
    return dateStr.stream().map(d -> parseDateTime(d, pattern)).collect(Collectors.toList());
  }

  public static List<Date> parseDateTimes(List<String> dateStr) {
    return dateStr.stream().map(DateTimeUtil::parseDateTime).collect(Collectors.toList());
  }

  public static Date parseDateTimeOrNow(String dateStr, String pattern) {
    return tryParseDateTime(dateStr, pattern).orElseGet(Date::new);
  }

  public static LocalDateTime toLocalDateTime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

  public static ZonedDateTime toZonedDateTime(LocalDateTime dt) {
    return ZonedDateTime.ofLocal(dt, ZoneId.systemDefault(), null);
  }

  public static ZonedDateTime toZonedDateTime(Date dt) {
    return toZonedDateTime(toLocalDateTime(dt));
  }

  public static LocalDate toLocalDate(Date date) {
    java.time.Instant dateInstant = java.time.Instant.ofEpochMilli(date.getTime());
    return LocalDateTime.ofInstant(dateInstant, ZoneId.systemDefault()).toLocalDate();
  }

  public static LocalTime toLocalTime(Date date) {
    java.time.Instant dateInstant = java.time.Instant.ofEpochMilli(date.getTime());
    return LocalDateTime.ofInstant(dateInstant, ZoneId.systemDefault()).toLocalTime();
  }



  public static Date fromLocalDateTime(LocalDateTime localDateTime) {
    return from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  public static Date fromZonedDateTime(ZonedDateTime zonedDateTime) {
    return from(zonedDateTime.toInstant());
  }

  public static Date fromLocalDate(LocalDate localDate) {
    return fromLocalDateTime(localDate.atStartOfDay());
  }

  public static Date fromLocalTime(LocalTime localTime) {
    return fromLocalDateTime(toLocalDateTime(localTime));
  }

  private static LocalDateTime toLocalDateTime(LocalTime localTime) {
    return localTime.atDate(LocalDate.of(0,1,1));
  }



  public static XMLGregorianCalendar dateToCalendar(Date date) {
    return dateToCalendar(toLocalDateTime(date));
  }

  public static XMLGregorianCalendar dateToCalendar(LocalDateTime localDateTime) {
    GregorianCalendar cal = GregorianCalendar.from(localDateTime.atZone(ZoneId.systemDefault()));
    return toXMLCalendar(cal);
  }

  public static XMLGregorianCalendar localDateToCalendar(LocalDate localDate) {
    return dateToCalendar(localDate.atStartOfDay());
  }

  public static XMLGregorianCalendar localTimeToCalendar(LocalTime localTime) {
    return dateToCalendar(toLocalDateTime(localTime));
  }

  public static XMLGregorianCalendar toXMLCalendar(GregorianCalendar cal) {
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
    } catch (DatatypeConfigurationException e) {
      return null;
    }
  }

  public static Date calendarToDate(XMLGregorianCalendar xmlCal) {
    return xmlCal.toGregorianCalendar().getTime();
  }

  public static Date calendarToTime(Calendar sourceCal) {
    Calendar calendar = getCalendarZero();
    calendar.set(HOUR_OF_DAY, sourceCal.get(HOUR_OF_DAY));
    calendar.set(MINUTE, sourceCal.get(MINUTE));
    calendar.set(SECOND, sourceCal.get(SECOND));
    return calendar.getTime();
  }

  public static Date calendarToTime(LocalTime time) {
    Calendar calendar = getCalendarZero();
    calendar.set(HOUR_OF_DAY, time.getHour());
    calendar.set(MINUTE, time.getMinute());
    calendar.set(SECOND, time.getSecond());
    return calendar.getTime();
  }

  private static Calendar getCalendarZero() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.YEAR, 1970);
    calendar.set(Calendar.MONTH, 0);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return calendar;
  }


  public static Date today() {
    return from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
  }

  public static LocalDateTime localToday() {
    return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        .atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  public static Date fromEpochTimestamp(long timestamp) {
    return from(Instant.ofEpochMilli(timestamp));
  }

  private static boolean canParse(String dtStr, DateTimeFormatter formatter) {
    try {
      if (dtStr == null) {
        return false;
      }
      formatter.parse(dtStr);
      return true;
    } catch (DateTimeParseException dte) {
      return false;
    }
  }

  public static Optional<Date> tryParseDate(String dateStr, String pattern) {
    return tryParseDate(dateStr,DateTimeFormatter.ofPattern(pattern));
  }

  public static Optional<Date> tryParseDate(String dateStr, DateTimeFormatter formatter) {
    if (Util.isEmpty(dateStr)) {
      return Optional.empty();
    }
    try {
      LocalDate localDate = LocalDate.parse(dateStr, formatter);
      Date date = from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
      return Optional.of(date);
    } catch (DateTimeParseException dtpe) {
      return Optional.empty();
    }
  }

  public static Optional<Date> tryParseDateTime(String dateStr, String pattern) {
    return tryParseDateTime(dateStr, DateTimeFormatter.ofPattern(pattern));
  }

  public static Optional<Date> tryParseDateTime(String dateStr, DateTimeFormatter formatter) {
    if (Util.isEmpty(dateStr)) {
      return Optional.empty();
    }
    try {
      LocalDateTime localDate = LocalDateTime.parse(dateStr, formatter);
      Date date = from(localDate.atZone(ZoneId.systemDefault()).toInstant());
      return Optional.of(date);
    } catch (DateTimeParseException dtpe) {
      return Optional.empty();
    }
  }

  public static int compare(Date d1, Date d2, TemporalUnit unit) {
    return Comparator
        .comparing(d -> toLocalDateTime((Date)d).truncatedTo(unit))
        .compare(d1,d2);
  }

  public static TemporalUnit toTemporalUnit(java.lang.String code) {
    switch (code) {
      case "a":
        return ChronoUnit.YEARS;
      case "mo":
        return ChronoUnit.MONTHS;
      case "wk":
        return ChronoUnit.WEEKS;
      case "d":
        return ChronoUnit.DAYS;
      case "h":
        return ChronoUnit.HOURS;
      case "min":
        return ChronoUnit.MINUTES;
      case "s":
        return ChronoUnit.SECONDS;
      case "ms":
      default:
        return ChronoUnit.MILLIS;
    }
  }

  public static boolean isSameDay(Date d1, Date d2) {
    if (d1 == null || d2 == null) {
      return false;
    }
    return toLocalDate(d1).atStartOfDay().equals(toLocalDate(d2).atStartOfDay());
  }

}
