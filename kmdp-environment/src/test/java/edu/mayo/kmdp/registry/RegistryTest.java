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

import org.junit.jupiter.api.Test;

import javax.xml.XMLConstants;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class RegistryTest {


  @Test
  public void testNamespaceMap() {

    assertEquals("http://kmdp.mayo.edu/metadata/annotations", getNS("ann"));
    assertEquals("http://kmdp.mayo.edu/metadata/surrogate", getNS("surr"));
    assertEquals(XMLConstants.XML_NS_URI, getNS(XMLConstants.XMLNS_ATTRIBUTE));
    assertEquals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, getNS("xsi"));
    assertEquals(XMLConstants.W3C_XML_SCHEMA_NS_URI, getNS("xsd"));
    assertEquals("urn:hl7-org:knowledgeartifact:r1", getNS("knart"));
    assertEquals("urn:hl7-org:elm:r1", getNS("elm"));
    assertEquals("http://www.w3.org/1999/xhtml", getNS("xhtml"));
    assertEquals("urn:hl7-org:cdsdt:r2", getNS("dt"));

  }

  @Test
  public void testSchemas() {

    assertEquals("http://www.omg.org/spec/DMN/20151101/dmn.xsd",
        getSchema("https://www.omg.org/spec/DMN/1.1"));
    assertEquals("http://www.omg.org/spec/CMMN/20151109/MODEL",
        getSchema("https://www.omg.org/spec/CMMN/1.1"));
    assertEquals("urn:hl7-org:knowledgeartifact:r1", getSchema("http://hl7.org/KNART/1.3"));
    assertEquals("urn:hl7-org:elm:r1", getSchema("http://hl7.org/ELM/1.2"));

  }

  private String getNS(String pfx) {
    return Registry.getNamespaceURIForPrefix(pfx).orElse("NOT FOUND");
  }

  private String getSchema(String lang) {
    return Registry.getValidationSchema(URI.create(lang)).orElse("NOT FOUND");
  }

}
