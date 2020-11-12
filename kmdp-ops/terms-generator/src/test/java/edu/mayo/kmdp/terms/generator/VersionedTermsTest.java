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
package edu.mayo.kmdp.terms.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.example.sch1.ISCH1;
import edu.mayo.kmdp.terms.example.sch1.ISCH1.ISCH1Version;
import edu.mayo.kmdp.terms.example.sch1.SCH1;
import edu.mayo.kmdp.terms.example.sch1.SCH1Old;
import edu.mayo.kmdp.terms.example.sch1.SCH1Series;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VersionedTermsTest {

  @Test
  void testVersions() {
    SCH1Series s1 = SCH1Series.Specific_Concept;

    assertFalse(s1.getVersions().isEmpty());

    assertEquals(2, s1.getVersions().size() );

    assertNotNull(s1.getDescription());
    assertEquals("specific concept", s1.getDescription().getLabel());

    assertSame(SCH1.Specific_Concept,s1.getLatest());
  }

  @Test
  void testIdentity() {
    ISCH1Version v0 = SCH1Series.Specific_Concept.getVersion(0).orElse(null);
    ISCH1Version v1 = SCH1Series.Specific_Concept.getVersion(1).orElse(null);

    assertNotNull(v0);
    assertNotNull(v1);

    assertTrue(v0.asSeries().isSameEntity(v0));
    assertTrue(v0.isDifferentVersion(v1));
    assertFalse(v0.isDifferentVersion(v0));
  }

  @Test
  void testResolve() {

    Optional<? extends ISCH1> x = SCH1.resolveTag("6789");
    assertTrue(x.isPresent());

    Optional<? extends ISCH1> y = SCH1Series.resolve("6789");
    assertTrue(y.isPresent());

  }

  @Test
  void testVersionEstablished() {
    assertEquals(
        SCH1Series.count(),
        (int) Arrays.stream(SCH1Series.values())
            .map(SCH1Series::getEstablishedOn)
            .filter(Objects::nonNull).count());
  }

  @Test
  void testDateAvailablilty() {
    SCH1Series con = SCH1Series.Specific_Concept;
    SCH1Series dep = SCH1Series.Deprecated_Concept;
    assertSame(SCH1Old.Deprecated_Concept, dep.getLatest());
    assertSame(SCH1.Specific_Concept, con.getLatest());

    Date d0 = DateTimeUtil.parseDate("1981-12-01");
    Date d1 = DateTimeUtil.parseDate("2019-08-01");
    assertEquals(SCH1Series.schemeReleases, Arrays.asList(d1, d0));

    assertEquals(d0,
        dep.getLatest().getVersionEstablishedOn());
    assertEquals(d0,
        dep.getSeriesEstablishedOn().orElse(null));
    assertEquals(d0,
        dep.getVersionEstablishedOn());

    assertEquals(d0,con.getSeriesEstablishedOn().orElse(null));
    assertEquals(d0,dep.getSeriesEstablishedOn().orElse(null));
    assertEquals(d1,con.getLatest().getVersionEstablishedOn());
    assertEquals(d0,dep.getLatest().getVersionEstablishedOn());

    Date epoch = DateTimeUtil.parseDate("1900-01-01");
    Date x1 = DateTimeUtil.parseDate("1990-06-03");
    Date x2 = DateTimeUtil.parseDate("2020-01-01");

    assertFalse(con.asOf(epoch).isPresent());
    assertFalse(dep.asOf(epoch).isPresent());

    assertTrue(con.asOf(x1).isPresent());
    assertSame(con.getVersions().get(1),con.asOf(x1).get());
    assertTrue(dep.asOf(x1).isPresent());

    assertFalse(con.isSeriesExpired());
    assertNull(con.getSeriesExpiredOn().orElse(null));
    assertTrue(dep.isSeriesExpired());
    assertEquals(d1,dep.getSeriesExpiredOn().orElse(null));

    assertTrue(con.asOf(x2).isPresent());
    assertFalse(dep.asOf(x2).isPresent());

  }

  @Test
  void testIdentityWithVersion() {
    SCH1 s1new = SCH1.Specific_Concept;
    SCH1Old s1old = SCH1Old.Specific_Concept;


    SCH1Series s1 = SCH1Series.Specific_Concept;

    assertTrue(s1old.ofSameAs(s1new));
    assertTrue(s1new.ofSameAs(s1old));
    assertTrue(s1new.isDifferentVersion(s1old));

    assertTrue(s1.isSameEntity(s1new));
    assertTrue(s1.isSameEntity(s1old));
    assertTrue(s1.isSameEntity(s1));
    assertTrue(s1.isEntityOf(s1new));
    assertTrue(s1.isEntityOf(s1old));
  }

  @Test
  void testIdentityWithVersionInterfaces() {
    ISCH1Version s1new = SCH1.Specific_Concept;
    ISCH1Version s1old = SCH1Old.Specific_Concept;

    s1new.ofSameAs(s1new);
    s1new.isSameVersion(s1new);
    s1new.isDifferentVersion(s1old);

    SCH1Series s1 = SCH1Series.Specific_Concept;

    ISCH1 x1 = s1new;
    ISCH1 x2 = s1new;





  }

}
