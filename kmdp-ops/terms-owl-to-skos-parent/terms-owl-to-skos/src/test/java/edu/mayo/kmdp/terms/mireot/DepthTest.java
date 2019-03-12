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
package edu.mayo.kmdp.terms.mireot;

import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DepthTest extends BaseMireotTest {

  @Test
  public void testClassDepth() {

    this.baseUri = "http://test.org";

    MireotExtractor extractor = newExtractor("/ontology/deepHier.owl");
    Set<Resource> c = extractor.extract(baseUri + "#A", EntityTypes.CLASS, 3, 4);

    assertTrue(c.contains(h("G")));
    assertTrue(c.contains(h("H")));
    assertTrue(c.contains(h("I")));
    assertTrue(c.contains(h("K")));

    assertFalse(c.contains(h("A")));
    assertFalse(c.contains(h("B")));
    assertFalse(c.contains(h("C")));
    assertFalse(c.contains(h("D")));
    assertFalse(c.contains(h("E")));
    assertFalse(c.contains(h("F")));
    assertFalse(c.contains(h("J")));

  }

  @Test
  public void testClassDepthNoLimit() {

    this.baseUri = "http://test.org";

    MireotExtractor extractor = newExtractor("/ontology/deepHier.owl");
    Set<Resource> c = extractor.extract(baseUri + "#A", EntityTypes.CLASS);

    assertTrue(c.contains(h("G")));
    assertTrue(c.contains(h("H")));
    assertTrue(c.contains(h("I")));
    assertTrue(c.contains(h("K")));

    assertTrue(c.contains(h("A")));
    assertTrue(c.contains(h("B")));
    assertTrue(c.contains(h("C")));
    assertTrue(c.contains(h("D")));
    assertTrue(c.contains(h("E")));
    assertTrue(c.contains(h("F")));
    assertTrue(c.contains(h("J")));

  }


  @Test
  public void testPropDepth() {

    this.baseUri = "http://test.org";

    MireotExtractor extractor = newExtractor("/ontology/deepProp.owl");
    Set<Resource> c = extractor.extract(baseUri + "#propA", EntityTypes.OBJ_PROP, 0, 1);

    assertTrue(c.contains(h("propA")));
    assertTrue(c.contains(h("propB")));
    assertTrue(c.contains(h("propF")));

    assertFalse(c.contains(h("propC")));
    assertFalse(c.contains(h("propD")));
    assertFalse(c.contains(h("propE")));

  }


  @Test
  public void testDataPropDepth() {

    this.baseUri = "http://test.org";

    MireotExtractor extractor = newExtractor("/ontology/deepDataProp.owl");
    Set<Resource> c = extractor.extract(baseUri + "#propA", EntityTypes.DATA_PROP, 1, 2);

    assertTrue(c.contains(h("propB")));
    assertTrue(c.contains(h("propF")));
    assertTrue(c.contains(h("propE")));
    assertTrue(c.contains(h("propC")));

    assertFalse(c.contains(h("propA")));
    assertFalse(c.contains(h("propD")));
    assertFalse(c.contains(h("propG")));

  }

}
