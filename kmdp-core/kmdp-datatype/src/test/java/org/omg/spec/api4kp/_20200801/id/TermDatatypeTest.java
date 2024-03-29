/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.omg.spec.api4kp._20200801.id;



import static edu.mayo.kmdp.registry.Registry.MAYO_ASSETS_BASE_URI;
import static edu.mayo.kmdp.registry.Registry.MAYO_ASSETS_BASE_URI_URI;
import static edu.mayo.kmdp.registry.Registry.UUID_URN;
import static edu.mayo.kmdp.registry.Registry.UUID_URN_URI;
import static edu.mayo.kmdp.util.CatalogBasedURIResolver.catalogResolver;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.id.Term.newTerm;

import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;


class TermDatatypeTest {

  @Test
  void testID() {
    // this should at least compile
    assertNotNull(new ConceptIdentifier()
        .withUuid(UUID.randomUUID())
        .withNamespaceUri(MAYO_ASSETS_BASE_URI_URI)
        .withName("Test ResourceId")
        .withVersionTag("LATEST"));
  }

  @Test
  void testConceptIdentifierTagVersion() {
    String tag = "1.6.4.3";
    Version version = Version.valueOf("1.0.0");
    URI expectedResourceId = URI.create(UUID_URN + tag);
    URI expectedVersionId = URI.create(UUID_URN + tag + ":" + version.toString());
    ConceptIdentifier cid = newTerm(tag, version).asConceptIdentifier();
    assertNotNull(cid);
    assertEquals(tag, cid.getTag());
    assertEquals(version.toString(), cid.getVersionTag());
    assertEquals(expectedResourceId, cid.getResourceId());
    assertEquals(expectedVersionId, cid.getVersionId());
  }

  @Test
  void testConceptIdentifierUUID() {
    UUID uid = UUID.randomUUID();
    URI expectedResourceId = URI.create(UUID_URN + uid.toString());
    ConceptIdentifier cid = newTerm(uid).asConceptIdentifier();
    assertNotNull(cid);
    // uuid, resourceId and tag are required on ConceptIdentifier; confirm all are there
    assertEquals(uid, cid.getUuid());
    assertEquals(uid.toString(), cid.getTag());
    assertEquals(expectedResourceId, cid.getResourceId());
  }

  @Test
  void testResourceIdAll() {
    String tag = "5.4.3.2";
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("5.0.1");
    String name = "TestResource";
    Date established = DateTimeUtil.today();
    URI referentId = URI.create("http://foo.bar/baz");
    URI conceptId = URI.create("http://foo.bar#1323");
    URI versionId = URI.create("http://foo.bar/versions/5.0.1#1323");

    ConceptIdentifier cid = (ConceptIdentifier) newTerm(conceptId, tag, uuid, MAYO_ASSETS_BASE_URI_URI, referentId,
            version.toString(), name, established);
    assertNotNull(cid);
    // uuid, tag and resourceId are required; confirm all are there
    assertEquals(uuid, cid.getUuid());
    assertEquals(tag, cid.getTag());
    assertEquals(conceptId, cid.getResourceId());
    assertEquals(versionId, cid.getVersionId());
    assertEquals(name, cid.getName());
    assertEquals(version.toString(), cid.getVersionTag());
    assertEquals(established, cid.getEstablishedOn());
    assertEquals(MAYO_ASSETS_BASE_URI_URI, cid.getNamespaceUri());
  }

  @Test
  void testConceptIdentifierTag() {
    String tag = "1.6.4.3";
    URI expectedResourceId = URI.create(UUID_URN + tag);
    ConceptIdentifier cid = (ConceptIdentifier) newTerm(tag);
    assertNotNull(cid);
    // uuid, resourceId and tag are required on ConceptIdentifier; confirm all are there
    assertEquals(tag, cid.getTag());
    assertEquals(UUID.nameUUIDFromBytes(expectedResourceId.toString().getBytes()), cid.getUuid());
    assertEquals(expectedResourceId, cid.getResourceId());
  }

  @Test
  void testSimpleID() {
    Identifier id = newTerm("thisId");

    assertEquals("thisId", id.getTag());
    assertEquals(IdentifierTagType.STRING_VALUE, id.getTagFormat());
    assertNotNull(id.getResourceId());
  }

  @Test
  void testVersionedID() {
    String tag = "1.3.6.1";
    String version = "42";
    VersionIdentifier vid = newTerm(tag, version);

    assertEquals("42", vid.getVersionTag());
    assertEquals("1.3.6.1", vid.getTag());
  }

  @Test
  void testQNameID() {
    String tag = "1.3.6.1";
    String expectedQname = "{" + MAYO_ASSETS_BASE_URI + "}_" + tag;
    ScopedIdentifier qid = newTerm(MAYO_ASSETS_BASE_URI_URI, tag);

    assertEquals(MAYO_ASSETS_BASE_URI_URI, qid.getNamespaceUri());
    assertEquals("1.3.6.1", qid.getTag());
    assertEquals(expectedQname, qid.getQName().toString());
    assertEquals("_1.3.6.1", qid.getQName().getLocalPart());
    assertEquals(MAYO_ASSETS_BASE_URI, qid.getQName().getNamespaceURI());
    assertNotNull(qid.getUuid());
    assertNotNull(qid.getResourceId());
  }

  @Test
  void testQNameID_URN() {
    String tag = "1.3.6.1";
    String expectedQname = "{" + UUID_URN + "}_" + tag;
    ScopedIdentifier qid = newTerm(UUID_URN_URI, tag);
    assertEquals(UUID_URN_URI, qid.getNamespaceUri());
    assertEquals("1.3.6.1", qid.getTag());
    assertEquals("_1.3.6.1", qid.getQName().getLocalPart());
    assertEquals(expectedQname, qid.getQName().toString());
    assertEquals(UUID_URN, qid.getQName().getNamespaceURI());
    assertNotNull(qid.getUuid());
    assertNotNull(qid.getResourceId());
  }

  @Test
  void testExceptionNoTag() {
    Exception exception = assertThrows(IllegalStateException.class,
        () -> newTerm(""));
    assertEquals("Missing required tag for Identifier", exception.getMessage());
  }


  @Test
  void testTerm() {
    Term term = newTerm(URI.create("http://terms.bar/kinda"),"123456", UUID.randomUUID(), URI.create("http://foo.bar/kinda"),
        URI.create("http://foo.bar/kinda/123456"), "1981", null, null);

    assertEquals("123456", term.getTag());
    assertEquals("http://foo.bar/kinda/123456", term.getReferentId().toString());
    // TODO: what is label expected to be? It is a derived attribute
//    assertEquals("Foo bar and baz", term.getLabel());

    assertEquals("http://foo.bar/kinda", term.getNamespaceUri().toString());
    assertEquals("123456", term.getTag());
    assertEquals("1981", term.getVersionTag());
  }

  @Test
  void testConceptIdentifier() {
    ConceptIdentifier trm = (ConceptIdentifier) newTerm(URI.create("http://foo.bar/term/234"),"234", UUID.randomUUID(), URI.create("http://id/1"),
            URI.create("http://foo.bar/234"), null, "1", null);

    ObjectFactory of = new ObjectFactory();
    String xml = JaxbUtil.marshall(Collections.singleton(of.getClass()),
        trm,
        of::createConceptIdentifier,
        JaxbUtil.defaultProperties())
        .map(ByteArrayOutputStream::toByteArray)
        .map(String::new)
        .orElse("");

//    System.out.println(xml);
    Optional<Schema> schema = getSchemas(
        TermDatatypeTest.class.getResource("/xsd/API4KP/api4kp/id/id.xsd"),
        catalogResolver("/xsd/api4kp-catalog.xml"));
    assertTrue(schema.isPresent());

    assertTrue(XMLUtil.validate(xml, schema.get()));
  }


  @Test
  void testIDComposition() {
    ConceptIdentifier uid = (ConceptIdentifier) newTerm(URI.create("http://foo.bar/"), "baz", Version.valueOf("1.1.0"));
    assertEquals(URI.create("http://foo.bar/baz"), uid.getResourceId());
    assertEquals(URI.create("http://foo.bar/baz/versions/1.1.0"), uid.getVersionId());
  }

  @Test
  void testIdentifierFormats() {
    String oidString = "1.5.6.3";
    UUID uuid = UUID.randomUUID();
    String random = "15-4";
    ConceptIdentifier cidOid = (ConceptIdentifier) newTerm(oidString);
    ConceptIdentifier cidUuid = (ConceptIdentifier) newTerm(uuid);
    ConceptIdentifier cidString = (ConceptIdentifier) newTerm(random);

    assertEquals(IdentifierTagType.OID_VALUE, cidOid.getTagFormat());
    assertNotEquals(IdentifierTagType.UUID_VALUE, cidOid.getTagFormat());
    assertNotEquals(IdentifierTagType.STRING_VALUE, cidOid.getTagFormat());

    assertEquals(IdentifierTagType.UUID_VALUE, cidUuid.getTagFormat());
    assertNotEquals(IdentifierTagType.OID_VALUE, cidUuid.getTagFormat());
    assertNotEquals(IdentifierTagType.STRING_VALUE, cidUuid.getTagFormat());

    assertEquals(IdentifierTagType.STRING_VALUE, cidString.getTagFormat());
    assertNotEquals(IdentifierTagType.UUID_VALUE, cidString.getTagFormat());
    assertNotEquals(IdentifierTagType.OID_VALUE, cidString.getTagFormat());

  }

}
