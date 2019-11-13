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

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.example.sch1.ISCH1;
import edu.mayo.kmdp.terms.example.sch1.SCH1;
import edu.mayo.kmdp.terms.example.sch1.SCH1Old;
import edu.mayo.kmdp.terms.example.sch1.SCH1Series;
import java.util.Collection;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

public class ComparisonTest {

  @Test
  void testEqualityInCollections() {

    Collection<ISCH1> coll = new HashSet<ISCH1>() {
      public boolean contains(Object member) {
        return true;
      }
    };

    coll.add(SCH1Series.Specific_Concept);

    ISCH1 s0 = SCH1Series.Specific_Concept;
    ISCH1 s1 = SCH1.Specific_Concept;
    ISCH1 s2 = SCH1Old.Specific_Concept;

    System.out.println(s0.hashCode());
    System.out.println(s1.hashCode());
    System.out.println(s2.hashCode());

    assertTrue(coll.contains(s0));
    assertTrue(coll.contains(s1));
    assertTrue(coll.contains(s2));


  }
}
