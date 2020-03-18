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
package org.omg.spec.api4kp._1_0.id;


import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN;
import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN_URI;
import static edu.mayo.kmdp.registry.Registry.MAYO_ASSETS_BASE_URI;
import static edu.mayo.kmdp.registry.Registry.MAYO_ASSETS_BASE_URI_URI;
import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;


public class DatatypeTest {

  @Test
  public void testID() {
    // this should at least compile
    assertNotNull(new ResourceIdentifier()
        .withUuid(UUID.randomUUID())
        .withNamespace(MAYO_ASSETS_BASE_URI_URI)
        .withName("Test ResourceId")
        .withVersionTag("LATEST"));
  }

  @Test
  public void testSemanticBuilderAssetId() {
    UUID uuid = UUID.randomUUID();
    URI expectedId = URI.create(MAYO_ASSETS_BASE_URI + uuid);
    URI versionId = URI.create(
        MAYO_ASSETS_BASE_URI + uuid + VersionIdentifier.VERSIONS + Version.valueOf("1.0.0"));
    ResourceIdentifier id = (ResourceIdentifier) SemanticIdentifier
        .newId(MAYO_ASSETS_BASE_URI_URI, uuid, Version.valueOf("1.0.0"), "testing");

    assertNotNull(id);
    assertEquals("testing", id.getName());
    assertEquals(MAYO_ASSETS_BASE_URI_URI, id.getNamespace());
    assertEquals(uuid, id.getUuid());
    assertEquals(expectedId, id.getResourceId());
    assertEquals(versionId, id.getVersionId());
  }

  @Test
  public void testSemanticBuilderResourceIdURN() {
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("1.0.0");
    String name = "Testing";
    URI expectedId = URI.create(BASE_UUID_URN + uuid);
    URI versionId = URI.create(BASE_UUID_URN + uuid + ":" + version.toString());
    ResourceIdentifier id = (ResourceIdentifier) SemanticIdentifier.newId(uuid, version, name);

    assertNotNull(id);
    assertEquals(name, id.getName());
    // uuid, tag and resourceId are required
    assertEquals(uuid, id.getUuid());
    assertEquals(uuid.toString(), id.getTag());
    assertEquals(expectedId, id.getResourceId());
    assertEquals(versionId, id.getVersionId());
    assertEquals(version.toString(), id.getVersionTag());
    assertNull(id.getEstablishedOn());
    assertNull(id.getNamespace());
  }


  @Test
  public void testResourceIdentifierTagVersion() {
    String tag = "1.6.4.3";
    Version version = Version.valueOf("1.0.0");
    URI expectedResourceId = URI.create(BASE_UUID_URN + tag);
    URI expectedVersionId = URI.create(BASE_UUID_URN + tag + ":" + version.toString());
    ResourceIdentifier rid = (ResourceIdentifier) SemanticIdentifier.newId(tag, version);
    assertNotNull(rid);
    assertEquals(tag, rid.getTag());
    assertEquals(version.toString(), rid.getVersionTag());
    assertEquals(expectedResourceId, rid.getResourceId());
    assertEquals(expectedVersionId, rid.getVersionId());
  }

  @Test
  public void testResourceIdentifierUUID() {
    UUID uid = UUID.randomUUID();
    URI expectedResourceId = URI.create(BASE_UUID_URN + uid.toString());
    ResourceIdentifier rid = (ResourceIdentifier) SemanticIdentifier.newId(uid);
    assertNotNull(rid);
    // uuid, resourceId and tag are required on ResourceIdentifier; confirm all are there
    assertEquals(uid, rid.getUuid());
    assertEquals(uid.toString(), rid.getTag());
    assertEquals(expectedResourceId, rid.getResourceId());
  }

  @Test
  public void testResourceIdentifierUIDTagNamespaceVersion() {
    String tag = "5.4.3.2";
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("5.0.1");
    String name = "TestResource";
    // TODO: confirm: tag is used if provided, else uuid
    URI expectedId = URI.create(MAYO_ASSETS_BASE_URI + tag);
    URI versionId = URI
        .create(MAYO_ASSETS_BASE_URI + tag + VersionIdentifier.VERSIONS + version.toString());

    ResourceIdentifier rid = (ResourceIdentifier) SemanticIdentifier
        .newId(MAYO_ASSETS_BASE_URI_URI, tag, uuid, version, name);
    assertNotNull(rid);
    // uuid, resourceId and tag are required on ResourceIdentifier; confirm all are there
    assertEquals(expectedId, rid.getResourceId());
    assertEquals(versionId, rid.getVersionId());
    assertEquals(tag, rid.getTag());
    assertEquals(uuid, rid.getUuid());
    assertEquals(name, rid.getName());
  }

  @Test
  public void testResourceIdentifierUIDTagNamespaceVersionAsURN() {
    String tag = "5.4.3.2";
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("5.0.1");
    String name = "TestResource";
    // TODO: confirm: tag is used if provided, else uuid
    URI expectedId = URI.create(BASE_UUID_URN + tag);
    URI versionId = URI.create(BASE_UUID_URN + tag + ":" + version.toString());

    ResourceIdentifier rid = (ResourceIdentifier) SemanticIdentifier
        .newId(BASE_UUID_URN_URI, tag, uuid, version, name);
    assertNotNull(rid);
    // uuid, resourceId and tag are required on ResourceIdentifier; confirm all are there
    assertEquals(expectedId, rid.getResourceId());
    assertEquals(versionId, rid.getVersionId());
    assertEquals(tag, rid.getTag());
    assertEquals(uuid, rid.getUuid());
    assertEquals(name, rid.getName());
  }


  @Test
  public void testResourceIdAll() {
    String tag = "5.4.3.2";
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("5.0.1");
    String name = "TestResource";
    Date established = DateTimeUtil.now();
    URI expectedId = URI.create(MAYO_ASSETS_BASE_URI + tag);
    URI versionId = URI
        .create(MAYO_ASSETS_BASE_URI + tag + VersionIdentifier.VERSIONS + version.toString());

    ResourceIdentifier rid = (ResourceIdentifier) SemanticIdentifier
        .newId(MAYO_ASSETS_BASE_URI_URI, tag, uuid, version, name, established);
    assertNotNull(rid);
    // uuid, tag and resourceId are required; confirm all are there
    assertEquals(uuid, rid.getUuid());
    assertEquals(tag, rid.getTag());
    assertEquals(expectedId, rid.getResourceId());
    assertEquals(versionId, rid.getVersionId());
    assertEquals(name, rid.getName());
    assertEquals(version.toString(), rid.getVersionTag());
    assertEquals(established, rid.getEstablishedOn());
    assertEquals(MAYO_ASSETS_BASE_URI_URI, rid.getNamespace());
  }

  @Test
  public void testResourceIdentifierTag() {
    String tag = "1.6.4.3";
    URI expectedResourceId = URI.create(BASE_UUID_URN + tag);
    ResourceIdentifier rid = (ResourceIdentifier) SemanticIdentifier.newId(tag);
    assertNotNull(rid);
    // uuid, resourceId and tag are required on ResourceIdentifier; confirm all are there
    assertEquals(tag, rid.getTag());
    assertEquals(UUID.nameUUIDFromBytes(expectedResourceId.toString().getBytes()), rid.getUuid());
    assertEquals(expectedResourceId, rid.getResourceId());
  }

  @Test
  public void testSemanticBuilderUrn() {
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("5.1.6");
    String name = "TestURN";
    URI expectedResourceId = URI.create(BASE_UUID_URN + uuid);
    URI expectedVersionId = URI.create(BASE_UUID_URN + uuid + ":" + version.toString());
    ResourceIdentifier id = (ResourceIdentifier) SemanticIdentifier
        .newId(BASE_UUID_URN_URI, uuid, version, name);

    assertNotNull(id);
    assertEquals(name, id.getName());
    assertEquals(uuid, id.getUuid());
    assertEquals(version.toString(), id.getVersionTag());
    assertEquals(expectedResourceId, id.getResourceId());
    assertEquals(expectedVersionId, id.getVersionId());
  }

  @Test
  public void testSimpleID() {
    Identifier id = SemanticIdentifier.newId("thisId");

    assertEquals("thisId", id.getTag());
    // todo: fix format test
    assertNull(id.getFormat());
  }

  @Test
  public void testVersionedID() {
    String tag = "1.3.6.1";
    String version = "42";
    VersionIdentifier vid = SemanticIdentifier.newId(tag, version);

    assertEquals("42", vid.getVersionTag());
    assertEquals("1.3.6.1", vid.getTag());
  }

  @Test
  public void testQNameID() {
    String tag = "1.3.6.1";
    String expectedQname = MAYO_ASSETS_BASE_URI + ":" + tag;
    ScopedIdentifier qid = SemanticIdentifier.newId(MAYO_ASSETS_BASE_URI_URI, tag);

    assertEquals(MAYO_ASSETS_BASE_URI_URI, qid.getNamespace());
    assertEquals("1.3.6.1", qid.getTag());
    assertEquals(QName.valueOf(expectedQname), qid.getQName());
    assertNotNull(qid.getUuid());
    assertNotNull(qid.getResourceId());
  }

  @Test
  public void testQNameID_URN() {
    String tag = "1.3.6.1";
    String expectedQname = BASE_UUID_URN + ":" + tag;
    ScopedIdentifier qid = SemanticIdentifier.newId(BASE_UUID_URN_URI, tag);
    assertEquals(BASE_UUID_URN_URI, qid.getNamespace());
    assertEquals("1.3.6.1", qid.getTag());
    assertEquals(QName.valueOf(expectedQname), qid.getQName());
    assertNotNull(qid.getUuid());
    assertNotNull(qid.getResourceId());
  }

  @Test
  public void testExceptionNoTag() {
    Exception exception = assertThrows(IllegalStateException.class,
        () -> SemanticIdentifier.newId(""));
    assertEquals("Missing required tag for Identifier", exception.getMessage());
  }


  @Test
  public void testTerm() {
    Term term = new ConceptIdentifier().withLocale("en-us")
        .withReferentId(URI.create("http://foo.bar/kinda/123456"))
        .withTag("123456")
        .withNamespace(URI.create("http://foo.bar/kinda"))
        .withVersionTag("1981");

    assertEquals("123456", term.getTag());
    assertEquals("http://foo.bar/kinda/123456", term.getReferentId().toString());
    // TODO: what is label expected to be? It is a derived attribute
//    assertEquals("Foo bar and baz", term.getLabel());

    assertEquals("http://foo.bar/kinda", term.getNamespace().toString());
    assertEquals("123456", term.getTag());
    assertEquals("1981", term.getVersionTag());
  }

  @Test
  public void testConceptIdentifier() {
    ConceptIdentifier trm =
        new ConceptIdentifier()
            .withReferentId(URI.create("http://foo.bar/234"))
//        .withLabel("aaaa")
            .withTag("234")
            .withNamespace(URI.create("http://id/1"))
            .withTag("id")
            .withVersionTag("1")
            .withUuid(UUID.randomUUID())
            .withResourceId(SemanticIdentifier.getResourceId("234", URI.create("http://id/1")));

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
        DatatypeTest.class.getResource("/xsd/API4KP/api4kp/id/id.xsd"),
        catalogResolver("/xsd/api4kp-catalog.xml"));
    assertTrue(schema.isPresent());

    assertTrue(XMLUtil.validate(xml, schema.get()));
  }


  @Test
  public void testIDComposition() {
    ResourceIdentifier uid = (ResourceIdentifier) SemanticIdentifier
        .newId(URI.create("http://foo.bar/"), "baz", Version.valueOf("1.1.0"));
    assertEquals(URI.create("http://foo.bar/baz"), uid.getResourceId());
    assertEquals(URI.create("http://foo.bar/baz/versions/1.1.0"), uid.getVersionId());
  }

}
