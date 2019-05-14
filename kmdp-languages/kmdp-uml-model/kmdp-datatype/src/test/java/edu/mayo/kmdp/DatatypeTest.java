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


import edu.mayo.kmdp.id.IDFormats;
import edu.mayo.kmdp.id.Identifier;
import edu.mayo.kmdp.id.ScopedIdentifier;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.ObjectFactory;
import org.omg.spec.api4kp._1_0.identifiers.QualifiedIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.SimpleIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.VersionIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.VersionTagType;

import javax.xml.namespace.QName;
import javax.xml.validation.Schema;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatatypeTest {

  @Test
  public void testID() {
    // this should at least compile
    assertNotNull(new URIIdentifier()
        .withUri(URI.create("http://foo.com/bar/1")));
  }

  @Test
  public void testSimpleID() {
    Identifier id = new SimpleIdentifier().withTag("thisId");

    assertEquals("thisId", id.getTag());
    assertNull(id.getFormat());
  }

  @Test
  public void testVersionedID() {
    VersionedIdentifier vid = new VersionIdentifier()
        .withTag("1.3.6.1")
        .withFormat(IDFormats.OID.asURI())
        .withVersion("42");

    assertEquals("42", vid.getVersion());
    assertEquals(IDFormats.OID.asURI(), vid.getFormat());
    assertEquals("1.3.6.1", vid.getTag());

  }

  @Test
  public void testQNameID() {
    ScopedIdentifier qid = new QualifiedIdentifier()
        .withQName(new QName("http://foo.com", "bar"));
    assertEquals("http://foo.com", qid.getNamespace().getTag());
    assertEquals(IDFormats.URI.asURI(), qid.getNamespace().getFormat());
    assertEquals("bar", qid.getTag());
    assertEquals(IDFormats.QNAME.asURI(), qid.getFormat());
  }

  @Test
  public void testTerm() {
    Term term = new ConceptIdentifier().withLabel("Foo bar and baz")
        .withRef(URI.create("http://foo.bar/kinda/123456"))
        .withTag("123456")
        .withNamespace(new NamespaceIdentifier()
            .withId(URI.create("http://foo.bar/kinda"))
            .withTag("kinda")
            .withVersion("1981").withVersioning(VersionTagType.GENERIC));

    assertEquals("123456", term.getTag());
    assertEquals("http://foo.bar/kinda/123456", term.getRef().toString());
    assertEquals("Foo bar and baz", term.getLabel());

    NamespaceIdentifier nsId = ((NamespaceIdentifier) term.getNamespace());

    assertEquals("http://foo.bar/kinda", nsId.getId().toString());
    assertEquals("kinda", nsId.getTag());
    assertEquals("1981", nsId.getVersion());
  }

  @Test
  public void testConceptIdentifier() {
    ConceptIdentifier trm = new ConceptIdentifier()
        .withRef(URI.create("http://foo.bar/234"))
        .withLabel("aaaa")
        .withTag("234")
        .withNamespace(new NamespaceIdentifier().withId(URI.create("http://id/1"))
            .withTag("id")
            .withVersion("1").withVersioning(VersionTagType.SEQUENTIAL));

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
        DatatypeTest.class.getResource("/xsd/API4KP/api4kp/identifiers/identifiers.xsd"),
        catalogResolver("/xsd/api4kp-catalog.xml"));
    assertTrue(schema.isPresent());

    assertTrue(XMLUtil.validate(xml, schema.get()));
  }


  @Test
  public void testIDComposition() {
    URIIdentifier uid = DatatypeHelper.uri("http://foo.bar", "baz", "1");
    assertEquals(URI.create("http://foo.bar/baz"), uid.getUri());
  }
}
