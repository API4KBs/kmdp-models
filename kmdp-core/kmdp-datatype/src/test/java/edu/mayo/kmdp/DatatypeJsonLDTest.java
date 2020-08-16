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
package edu.mayo.kmdp;

import static edu.mayo.kmdp.util.JenaUtil.objA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.JenaUtil;
import java.net.URI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.identifiers.NamespaceIdentifier;

public class DatatypeJsonLDTest {

  @Test
  public void testSemanticConceptSerialization() {
    ConceptIdentifier cid = new ConceptIdentifier()
        .withConceptId(URI.create("http://foo.com/skos/concept-001"))
        .withRef(URI.create("http://foo.com/referent-001"))
        .withLabel("Foo Ref")
        .withTag("f00-1")
        .withNamespace(new NamespaceIdentifier()
            .withId(URI.create("http://foo.com/skos/scheme1"))
            .withLabel("My Scheme")
            .withVersion("0.1.0"));

    Model m = JenaUtil.toTriples(cid)
        .orElse(null);

    assertNotNull(m);
    assertTrue(m.contains(objA("http://foo.com/skos/concept-001", RDF.type, SKOS.Concept)));
    assertTrue(m.contains(objA("http://foo.com/skos/concept-001", SKOS.inScheme.getURI(),
        "http://foo.com/skos/scheme1")));
    assertTrue(m.contains(objA("http://foo.com/skos/scheme1", RDF.type, SKOS.ConceptScheme)));

  }


}
