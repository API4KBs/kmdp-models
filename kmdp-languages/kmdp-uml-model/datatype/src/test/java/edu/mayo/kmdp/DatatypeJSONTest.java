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
import edu.mayo.kmdp.util.JSonUtil;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DatatypeJSONTest {

  @Test
  public void testJSON() {
    URI foo = URI.create("http://foo.bar");
    Pointer ptr = new Pointer();
    ptr.setHref(foo);
    ptr.setName("Name");
    ptr.setType(foo);
    ptr.setEntityRef(uri("uri:urn:faa"));

    Pointer ptr2 = new Pointer();
    ptr2.setHref(foo);
    ptr2.setName("Name2");
    ptr2.setType(foo);
    ptr2.setEntityRef(uri("uri:urn:fbb"));

    JSonUtil.printOutJson(Arrays.asList(ptr, ptr2));

    XmlMapper mapper = new XmlMapper();
    JaxbAnnotationModule module = new JaxbAnnotationModule();
    mapper.registerModule(module);
    mapper.registerModule(new JacksonXmlModule());

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      mapper.writerWithDefaultPrettyPrinter().writeValue(baos, ptr);
      String str = new String(baos.toByteArray());

      System.out.println(str);
      assertTrue(str.contains("name=\"Name\""));
      assertTrue(str.contains("uri=\"uri:urn:faa\""));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testJsonID() {
    ConceptIdentifier c1 = new ConceptIdentifier().withConceptId(URI.create("http://foo.bar"));
    ConceptIdentifier c2 = new ConceptIdentifier().withRef(URI.create("http://foo.bar"));

    assertTrue(JSonUtil.printJson(c1).filter((s) -> s.contains("@id")).isPresent());
    assertFalse(JSonUtil.printJson(c2).filter((s) -> s.contains("@id")).isPresent());

    NamespaceIdentifier nsId = new NamespaceIdentifier().withId(URI.create("http://foo.bar"))
        .withLabel("NS")
        .withTag("foo");
    JSonUtil.printOutJson(nsId);
  }


}
