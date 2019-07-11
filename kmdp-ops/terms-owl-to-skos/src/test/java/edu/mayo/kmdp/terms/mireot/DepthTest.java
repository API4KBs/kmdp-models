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

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.mireot.MireotConfig.MireotParameters;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Test;

public class DepthTest extends BaseMireotTest {

  @Test
  public void testClassDepth() {
    String base = "http://test.org";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.CLASS)
        .with(MireotParameters.MIN_DEPTH, 3)
        .with(MireotParameters.MAX_DEPTH, 4);

    Set<Resource> c = new MireotExtractor().fetch(stream("/ontology/deepHier.owl"),
        URI.create(base + "#A"),
        cfg)
        .map((m) -> getResources(base,m))
        .orElse(new HashSet<>());

    assertTrue(c.contains(h(base, "G")));
    assertTrue(c.contains(h(base, "H")));
    assertTrue(c.contains(h(base, "I")));
    assertTrue(c.contains(h(base, "K")));

    assertFalse(c.contains(h(base, "A")));
    assertFalse(c.contains(h(base, "B")));
    assertFalse(c.contains(h(base, "C")));
    assertFalse(c.contains(h(base, "D")));
    assertFalse(c.contains(h(base, "E")));
    assertFalse(c.contains(h(base, "F")));
    assertFalse(c.contains(h(base, "J")));

  }

  @Test
  public void testClassDepthNoLimit() {

    String base = "http://test.org#";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.CLASS);

    Set<Resource> c = new MireotExtractor().fetch(stream("/ontology/deepHier.owl"),
        URI.create(base + "A"),
        cfg)
        .map((m) -> getResources(base,m))
        .orElse(new HashSet<>());

    assertTrue(c.contains(h(base, "G")));
    assertTrue(c.contains(h(base, "H")));
    assertTrue(c.contains(h(base, "I")));
    assertTrue(c.contains(h(base, "K")));

    assertTrue(c.contains(h(base, "A")));
    assertTrue(c.contains(h(base, "B")));
    assertTrue(c.contains(h(base, "C")));
    assertTrue(c.contains(h(base, "D")));
    assertTrue(c.contains(h(base, "E")));
    assertTrue(c.contains(h(base, "F")));
    assertTrue(c.contains(h(base, "J")));

  }


  @Test
  public void testPropDepth() {

    String base = "http://test.org";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.OBJ_PROP)
        .with(MireotParameters.MIN_DEPTH, 0)
        .with(MireotParameters.MAX_DEPTH, 1);

    Set<Resource> c = new MireotExtractor().fetch(stream("/ontology/deepProp.owl"),
        URI.create(base + "#propA"),
        cfg)
        .map((m) -> getResources(base,m))
        .orElse(new HashSet<>());

    assertTrue(c.contains(h(base, "propA")));
    assertTrue(c.contains(h(base, "propB")));
    assertTrue(c.contains(h(base, "propF")));

    assertFalse(c.contains(h(base, "propC")));
    assertFalse(c.contains(h(base, "propD")));
    assertFalse(c.contains(h(base, "propE")));

  }


  @Test
  public void testDataPropDepth() {

    String base = "http://test.org";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.DATA_PROP)
        .with(MireotParameters.MIN_DEPTH, 1)
        .with(MireotParameters.MAX_DEPTH, 2);

    Set<Resource> c = new MireotExtractor().fetch(stream("/ontology/deepDataProp.owl"),
        URI.create(base + "#propA"),
        cfg)
        .map((m) -> getResources(base, m))
        .orElse(new HashSet<>());

    assertTrue(c.contains(h(base, "propB")));
    assertTrue(c.contains(h(base, "propF")));
    assertTrue(c.contains(h(base, "propE")));
    assertTrue(c.contains(h(base, "propC")));

    assertFalse(c.contains(h(base, "propA")));
    assertFalse(c.contains(h(base, "propD")));
    assertFalse(c.contains(h(base, "propG")));

  }

}
