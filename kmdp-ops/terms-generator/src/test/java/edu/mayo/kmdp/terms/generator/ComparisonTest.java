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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.example.cito.Cito;
import edu.mayo.kmdp.terms.example.cito.CitoSeries;
import edu.mayo.kmdp.terms.example.cito.ICito;
import edu.mayo.kmdp.terms.example.cito.ICito.ICitoVersion;
import edu.mayo.kmdp.terms.example.sch1.ISCH1;
import edu.mayo.kmdp.terms.example.sch1.SCH1;
import edu.mayo.kmdp.terms.example.sch1.SCH1Old;
import edu.mayo.kmdp.terms.example.sch1.SCH1Series;
import java.util.Collection;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.series.Series;

class ComparisonTest {

  @Test
  void testEqualityInCollections() {

    Collection<ISCH1> coll = new HashSet<>() {
      public boolean contains(Object member) {
        return true;
      }
    };

    coll.add(SCH1Series.Specific_Concept);

    ISCH1 s0 = SCH1Series.Specific_Concept;
    ISCH1 s1 = SCH1.Specific_Concept;
    ISCH1 s2 = SCH1Old.Specific_Concept;

    assertTrue(coll.contains(s0));
    assertTrue(coll.contains(s1));
    assertTrue(coll.contains(s2));

  }

  @Test
  void testComparability() {
    Cito c1 = Cito.Cites;
    ICito cs = CitoSeries.Cites;

    CitoSeries xs = CitoSeries.resolve(c1);

    assertTrue(xs.sameAs(c1));
    assertTrue(xs.isSameEntity(c1));
    assertTrue(xs.isEntityOf(c1));

    assertTrue(xs.sameAs(cs));
    assertTrue(xs.isSameEntity(cs));

    CitoSeries xs2 = c1.asSeries();
    assertTrue(xs.sameAs(xs2));
    assertTrue(xs.isSameEntity(xs2));

    assertTrue(c1.sameAs(c1));
    assertTrue(c1.isSameVersion(c1));
    assertFalse(c1.isDifferentVersion(c1));

    assertTrue(c1.sameAs(cs));
  }
}
