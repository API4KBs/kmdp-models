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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import edu.mayo.kmdp.util.JSonUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.Pointer;

public class DatatypeJSONTest {

  @Test
  public void testJSON() {
    URI foo = URI.create("http://foo.bar");
    Pointer ptr = new Pointer();
    ptr.setHref(foo);
    ptr.setName("Name");
    ptr.setType(foo);
    ptr.setResourceId(URI.create("uri:urn:faa"));

    Pointer ptr2 = new Pointer();
    ptr2.setHref(foo);
    ptr2.setName("Name2");
    ptr2.setType(foo);
    ptr2.setResourceId(URI.create("uri:urn:fbb"));

//    JSonUtil.printOutJson(Arrays.asList(ptr, ptr2));

    XmlMapper mapper = new XmlMapper();
    JaxbAnnotationModule module = new JaxbAnnotationModule();
    mapper.registerModule(module);
    mapper.registerModule(new JacksonXmlModule());

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      mapper.writerWithDefaultPrettyPrinter().writeValue(baos, ptr);
      String str = new String(baos.toByteArray());

      assertTrue(str.contains("name=\"Name\""));
      assertTrue(str.contains("resourceId=\"uri:urn:faa\""));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testJsonID() {
    ConceptIdentifier c1 = new ConceptIdentifier().withResourceId(URI.create("http://foo.bar"));
    ConceptIdentifier c2 = new ConceptIdentifier().withReferentId(URI.create("http://foo.bar/ref"));

    JSonUtil.printJson(c1).ifPresent(System.out::println);
    JSonUtil.printJson(c2).ifPresent(System.out::println);

    assertTrue(JSonUtil.printJson(c1).filter((s) -> s.contains("resourceId")).isPresent());
    assertFalse(JSonUtil.printJson(c2).filter((s) -> s.contains("resourceId")).isPresent());

    assertTrue(JSonUtil.printJson(c1)
        .filter((s) -> s.contains("\"resourceId\" : \"http://foo.bar\"")).isPresent());
  }

  @Test
  public void testRoundTrip() {
    ConceptIdentifier c1 = new ConceptIdentifier()
        .withResourceId(URI.create("http://foo.bar"))
        .withTag("bar");
    Optional<String> s = JSonUtil.writeJsonAsString(c1);
    assertTrue(s.isPresent());

    Optional<ConceptIdentifier> c2 = JSonUtil.parseJson(s.get(),ConceptIdentifier.class);
    assertTrue(c2.isPresent());

    assertEquals(c1,c2.get());

    Optional<String> s2 = JSonUtil.writeJsonAsString(c2.get());
    assertTrue(s2.isPresent());

    assertEquals(s.get(),s2.get());
  }

}
