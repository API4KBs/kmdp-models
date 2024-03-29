/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.adapters.DateAdapter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;

class DateTimeUtilTest {

  @Test
  void testDateFallback() {
    Date d = DateTimeUtil.parseDateOrNow("sadas","");
    Date now = new Date();
    // 10 seconds should be enough...
    assertTrue(now.getTime() - d.getTime() < 10000);
  }

  @Test
  void testParseDateDefault() {
    Date d = DateTimeUtil.parseDate("2015-08-14");
    assertNotNull(d);

    LocalDate date = LocalDate.from(d.toInstant().atZone(ZoneId.systemDefault()));
    assertEquals(2015, date.getYear());
    assertEquals(14, date.getDayOfMonth());
    assertEquals(Month.AUGUST, date.getMonth());
  }

  @Test
  void testParseWithCustomFormat() {
    Date d = DateTimeUtil.tryParseDate("20190801","yyyyMMdd")
        .orElse(null);
    assertNotNull(d);

    LocalDate date = LocalDate.from(d.toInstant().atZone(ZoneId.systemDefault()));
    assertEquals(2019, date.getYear());
    assertEquals(01, date.getDayOfMonth());
    assertEquals(Month.AUGUST, date.getMonth());
  }

  @Test
  void testXMLFormat() {
    Date d = DateTimeUtil.parseDate("2019-08-01");
    assertNotNull(d);

    String s = DateAdapter.instance().write(d);
    assertNotNull(s);

    assertEquals("2019-08-01T00:00:00", s);
  }

  @Test
  void testFullDate() {
    Date d = DateTimeUtil.parseDateTime(
        "2019-08-06T22:16:54Z",
        "yyyy-MM-dd'T'HH:mm:ss'Z'");

    assertNotNull(d);

    String s = DateAdapter.instance().write(d);
    assertNotNull(s);

    assertEquals("2019-08-06T22:16:54", s);
  }

  @Test
  void testIsDate() {
    assertTrue(DateTimeUtil.validateDate("2019-08-06"));
    Date d = DateTimeUtil.parseDate("2019-08-06");
    assertNotNull(d);
  }

  @Test
  void testIsSameDay() {
    Date d1 = DateTimeUtil.parseDateTime(
        "2019-08-06T22:16:54Z",
        "yyyy-MM-dd'T'HH:mm:ss'Z'");
    Date d2 = DateTimeUtil.parseDate(
        "2019-08-06");

    assertTrue(DateTimeUtil.isSameDay(d1,d2));
  }
  
  @Test
  void testToEpochMillis() {
    Date d = new Date();
    long millis = d.getTime();

    String dateStr = DateTimeUtil.serializeAsDateTime(d);
    String millStr = DateTimeUtil.dateTimeStrToMillis(dateStr);

    assertEquals(Long.toString(millis),millStr);
  }


  @Test
  void testSerializeInstant() {
    Instant now = Instant.ofEpochMilli(100000);

    String localDT = DateTimeUtil.serializeAsLocalDateTime(now);
    String zonedDT = DateTimeUtil.serializeAsDateTime(now);
    String localD = DateTimeUtil.serializeAsDate(now);
    String localT = DateTimeUtil.serializeAsTime(now);

    System.out.println(localD);
    System.out.println(localT);
    System.out.println(localDT);
    System.out.println(zonedDT);

    assertTrue(localDT.contains(localD));
    assertTrue(localDT.contains(localT));
    assertTrue(zonedDT.contains(localDT));
  }


}
