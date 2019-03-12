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

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import edu.mayo.kmdp.util.JSonLDUtil;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.Util;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static edu.mayo.kmdp.id.helper.DatatypeHelper.vuri;
import static edu.mayo.kmdp.util.JenaUtil.obj_a;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    assertTrue(m.contains(obj_a("http://foo.com/skos/concept-001", RDF.type, SKOS.Concept)));
    assertTrue(m.contains(obj_a("http://foo.com/skos/concept-001", SKOS.inScheme.getURI(),
        "http://foo.com/skos/scheme1")));
    assertTrue(m.contains(obj_a("http://foo.com/skos/scheme1", RDF.type, SKOS.ConceptScheme)));

  }


}
