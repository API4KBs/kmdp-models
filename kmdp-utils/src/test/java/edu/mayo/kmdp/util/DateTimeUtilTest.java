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
  public void testParseDateDefault() {
    Date d = DateTimeUtil.tryParseDate("2015-08-14").orElse(null);
    assertNotNull(d);

    LocalDate date = LocalDate.from(d.toInstant().atZone(ZoneId.systemDefault()));
    assertEquals(2015, date.getYear());
    assertEquals(14, date.getDayOfMonth());
    assertEquals(Month.AUGUST, date.getMonth());
  }

  @Test
  public void testParseWithCustomFormat() {
    Date d = DateTimeUtil.tryParseDate("20190801","yyyyMMdd")
        .orElse(null);
    assertNotNull(d);

    LocalDate date = LocalDate.from(d.toInstant().atZone(ZoneId.systemDefault()));
    assertEquals(2019, date.getYear());
    assertEquals(01, date.getDayOfMonth());
    assertEquals(Month.AUGUST, date.getMonth());
  }

  @Test
  public void testXMLFormat() {
    Date d = DateTimeUtil.tryParseDate("2019-08-01").orElse(null);
    assertNotNull(d);

    String s = DateAdapter.instance().write(d);
    assertNotNull(s);

    assertEquals("2019-08-01T12:00:00", s);
  }

}
