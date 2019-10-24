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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.terms.example.sch1.ISCH1;
import edu.mayo.kmdp.terms.example.sch1.SCH1;
import edu.mayo.kmdp.terms.example.sch1.SCH1Series;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VersionedTermsTest {

  @Test
  public void testVersions() {
    SCH1Series s1 = SCH1Series.Specific_Concept;

    assertFalse(s1.getVersions().isEmpty());

    assertEquals(2, s1.getVersions().size() );

    assertNotNull(s1.getDescription());
    assertEquals("specific concept", s1.getDescription().getLabel());

    assertSame(SCH1.Specific_Concept,s1.getLatest());
  }

  @Test
  public void testIdentity() {
    ISCH1 v0 = SCH1Series.Specific_Concept.getVersion(0).orElse(null);
    ISCH1 v1 = SCH1Series.Specific_Concept.getVersion(1).orElse(null);

    assertNotNull(v0);
    assertNotNull(v1);

    assertTrue(Series.isSame(v0,v1));
    assertTrue(Series.isDifferentVersion(v0,v1));
    assertFalse(Series.isDifferentVersion(v0,v0));
  }

  @Test
  public void testResolve() {

    Optional<? extends ISCH1> x = SCH1.resolve("6789");
    assertTrue(x.isPresent());

    Optional<? extends ISCH1> y = SCH1Series.resolve("6789");
    assertTrue(y.isPresent());

  }

}
