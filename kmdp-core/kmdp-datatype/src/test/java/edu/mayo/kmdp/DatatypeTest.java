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


import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.IdentifierTagType;
import org.omg.spec.api4kp._20200801.id.ObjectFactory;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;

public class DatatypeTest {

  @Test
  public void testID() {
    // this should at least compile
    assertNotNull(SemanticIdentifier.newId(URI.create("http://foo.com/bar/1")));
  }

  @Test
  public void testSimpleID() {
    SemanticIdentifier id = SemanticIdentifier.newId("thisId");

    assertEquals("thisId", id.getTag());
    assertSame(IdentifierTagType.STRING_VALUE,id.getTagFormat());
  }

  @Test
  public void testVersionedID() {
    SemanticIdentifier vid = new ResourceIdentifier()
        .withTag("1.3.6.1")
        .withVersionTag("42");

    assertEquals("42", vid.getVersionTag());
    assertSame(IdentifierTagType.OID_VALUE, vid.getTagFormat());
    assertEquals("1.3.6.1", vid.getTag());
  }

  @Test
  public void testTerm() {
    Term term = new ConceptIdentifier()
        .withName("Foo bar and baz")
        .withReferentId(URI.create("http://foo.bar/kinda/123456"))
        .withTag("123456")
        .withNamespaceUri(URI.create("http://foo.bar/kinda/versions/1981"));

    assertEquals("123456", term.getTag());
    assertEquals("http://foo.bar/kinda/123456", term.getReferentId().toString());
    assertEquals("Foo bar and baz", term.getLabel());

    SemanticIdentifier nsId = SemanticIdentifier.newVersionId(term.getNamespaceUri());

    assertEquals("http://foo.bar/kinda", nsId.getResourceId().toString());
    assertEquals("kinda", nsId.getTag());
    assertEquals("1981", nsId.getVersionTag());
  }

  @Test
  public void testConceptIdentifier() {
    ConceptIdentifier trm = new ConceptIdentifier()
        .withResourceId(URI.create("http://foo.bar/234"))
        .withUuid(UUID.randomUUID())
        .withName("aaaa")
        .withTag("234")
        .withNamespaceUri(URI.create("http://id/versions/1"));

    ObjectFactory of = new ObjectFactory();
    String xml = JaxbUtil.marshall(Collections.singleton(of.getClass()),
        trm,
        of::createConceptIdentifier,
        JaxbUtil.defaultProperties())
        .map(ByteArrayOutputStream::toByteArray)
        .map(String::new)
        .orElse("");

    //System.out.println(xml);
    Optional<Schema> schema = getSchemas(
        DatatypeTest.class.getResource("/xsd/API4KP/api4kp/id/id.xsd"),
        catalogResolver("/xsd/api4kp-catalog.xml"));
    assertTrue(schema.isPresent());

    assertTrue(XMLUtil.validate(xml, schema.get()));
  }


  @Test
  public void toURIIDentifier() {
    String versionedId = "http://foo.bar/baz/versions/1.2.0";
    String unversionedId = "http://foo.bar/baz";
    ResourceIdentifier uid = SemanticIdentifier.newVersionId(URI.create(versionedId));

    assertEquals("http://foo.bar/baz", uid.getResourceId().toString());
    assertEquals("1.2.0", uid.getVersionTag());
    assertEquals(versionedId, uid.getVersionId().toString());

    uid = SemanticIdentifier.newId(URI.create(unversionedId));
    assertEquals("http://foo.bar/baz", uid.getResourceId().toString());
    assertNull(uid.getVersionId());
  }
}
