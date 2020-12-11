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
package edu.mayo.kmdp.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import javax.xml.XMLConstants;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Test;


public class RegistryTest {

  @Test
  public void testKnownXMLNamespaces() {
    assertFalse(Registry.listPrefixes().stream().anyMatch(p -> p.length() == 0));
    assertEquals(55, Registry.listPrefixes().size());
  }

  @Test
  public void testNamespaceMap() {

    assertEquals(XMLConstants.XML_NS_URI, getNS(XMLConstants.XMLNS_ATTRIBUTE));
    assertEquals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, getNS("xsi"));
    assertEquals("http://kmdp.mayo.edu/metadata/annotations", getNS("ann"));
    assertEquals(SKOS.getURI(), getNS("skos"));
  }

  @Test
  public void testVersionedNamespaces() {
    assertEquals("http://www.omg.org/spec/DMN/20151101/dmn.xsd", getNS("dmn-v11"));
    assertEquals("http://www.omg.org/spec/DMN/20180521/MODEL/", getNS("dmn-v12"));
    assertEquals("http://www.omg.org/spec/DMN/20180521/MODEL/", getNS("dmn"));
    assertEquals("urn:hl7-org:knowledgeartifact:r1", getNS("knart-v13"));
    assertEquals("urn:hl7-org:elm:r1", getNS("elm-v1"));
    assertEquals("http://www.w3.org/2001/XMLSchema", getNS("xsd"));
    assertEquals("http://kmdp.mayo.edu/metadata/surrogate", getNS("surr-v1"));
    assertEquals("https://www.omg.org/spec/API4KP/20200801/surrogate", getNS("surr"));
  }

  @Test
  public void testSchemas() {

    assertEquals("https://www.omg.org/spec/API4KP/20200801/surrogate",
        getSchema("https://www.omg.org/spec/API4KP/20200801/metadata"));
    assertEquals("http://www.omg.org/spec/DMN/20151101/dmn.xsd",
        getSchema("https://www.omg.org/spec/DMN/1.1/"));
    assertEquals("http://www.omg.org/spec/CMMN/20151109/MODEL",
        getSchema("https://www.omg.org/spec/CMMN/1.1/"));
    assertEquals("urn:hl7-org:knowledgeartifact:r1", getSchema("http://hl7.org/KNART/1.3"));
    assertEquals("urn:hl7-org:elm:r1", getSchema("http://cql.hl7.org/ELM"));

  }

  private String getNS(String pfx) {
    return Registry.getNamespaceURIForPrefix(pfx).orElse("NOT FOUND");
  }

  private String getSchema(String lang) {
    return Registry.getValidationSchema(URI.create(lang)).orElse("NOT FOUND");
  }

}
