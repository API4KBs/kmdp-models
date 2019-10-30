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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTimeUtil {

  private static final Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);

  public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

  public static Date now() {
    return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
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
      //logger.warn(dtpe.getMessage(),dtpe);
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

  public static String format(Date date, String pattern) {
    if (date == null) {
      return null;
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()));
  }

  public static boolean isDate(String dateStr) {
    return tryParseDate(dateStr).isPresent();
  }


}
