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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DateTimeUtil {

  public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
  public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  protected DateTimeUtil() {

  }

  public static Date today() {
    return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
  }

  public static LocalDateTime localToday() {
    return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        .atZone(ZoneId.systemDefault()).toLocalDateTime();
  }

  public static Optional<Date> tryParseDate(String date) {
    return tryParseDate(date, DEFAULT_DATE_PATTERN);
  }

  public static Date parseDateOrNow(String date, String pattern) {
    return tryParseDate(date, pattern)
        .orElse(new Date());
  }

  public static Date parseDate(String date, String pattern) {
    return tryParseDate(date, pattern)
        .orElse(null);
  }

  public static Date parseDate(String date) {
    return tryParseDate(date,DEFAULT_DATE_PATTERN)
        .orElse(null);
  }

  public static Optional<Date> tryParseDate(String dateStr, String pattern) {
    if (Util.isEmpty(dateStr)) {
      return Optional.empty();
    }
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
      LocalDate localDate = LocalDate.parse(dateStr, formatter);
      return Optional.of(
          Date.from(
              localDate.atStartOfDay(ZoneId.systemDefault())
                  .toInstant()));
    } catch (DateTimeParseException dtpe) {
      return Optional.empty();
    }
  }


  public static String formatDateTime(Date date) {
    return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
        date.toInstant().atZone(ZoneId.systemDefault()));
  }

  public static String format(Date date) {
    return format(date, DEFAULT_DATE_PATTERN);
  }
  public static String format(List<Date> dates) {
    return format(dates, DEFAULT_DATE_PATTERN);
  }

  public static String format(Date date, String pattern) {
    if (date == null) {
      return null;
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()));
  }

  public static String format(List<Date> dates, String pattern) {
    return dates.stream()
        .map(d -> format(d,pattern))
        .collect(Collectors.joining(","));
  }

  public static boolean isDate(String dateStr) {
    return tryParseDate(dateStr).isPresent();
  }


  public static List<Date> parseDates(List<String> asList) {
    return parseDates(asList, DEFAULT_DATE_PATTERN);
  }

  public static List<Date> parseDates(List<String> asList, String pattern) {
    if (asList == null) {
      return Collections.emptyList();
    }
    return asList.stream()
        .map(d -> parseDate(d, pattern))
        .collect(Collectors.toList());
  }

  public static Optional<Date> tryParseDateTime(String dateStr, String pattern) {
    if (Util.isEmpty(dateStr)) {
      return Optional.empty();
    }
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
      LocalDateTime localDate = LocalDateTime.parse(dateStr, formatter);
      Date date = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
      return Optional.of(date);
    } catch (DateTimeParseException dtpe) {
      return Optional.empty();
    }
  }

  public static Date parseDateTime(String dateStr, String pattern) {
    return tryParseDateTime(dateStr,pattern)
        .orElse(null);
  }

  public static Date parseDateTime(String dateStr) {
    return tryParseDateTime(dateStr,DEFAULT_DATETIME_PATTERN)
        .orElse(null);
  }

  public static Date fromEpochTimestamp(long timestamp) {
    return Date.from(Instant.ofEpochMilli(timestamp));
  }

  public static Date toDate(LocalDateTime ldt) {
    return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
  }
}
