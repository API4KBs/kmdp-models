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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.junit.jupiter.api.Test;


public class DatatypeTest {

  @Test
  public void testID() {
    // this should at least compile
    assertNotNull(new ResourceIdentifier()
        .withUuid(UUID.randomUUID())
        .withNamespaceUri(MAYO_ASSETS_BASE_URI_URI)
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
    assertEquals(MAYO_ASSETS_BASE_URI_URI, id.getNamespaceUri());
    assertEquals(uuid, id.getUuid());
    assertEquals(expectedId, id.getResourceId());
    assertEquals(versionId, id.getVersionId());
  }

  @Test
  public void testSemanticBuilderAssetIdStringVersion() {
    UUID uuid = UUID.randomUUID();
    URI expectedId = URI.create(MAYO_ASSETS_BASE_URI + uuid);
    URI versionId = URI.create(
        MAYO_ASSETS_BASE_URI + uuid + VersionIdentifier.VERSIONS + "1.0.0");
    ResourceIdentifier id = (ResourceIdentifier) SemanticIdentifier
        .newId(MAYO_ASSETS_BASE_URI_URI, uuid, "1.0.0", "testing");

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
    assertNull(id.getNamespaceUri());
  }

  @Test
  public void testSemanticBuilderResourceIdVersionStringURN() {
    UUID uuid = UUID.randomUUID();
    String version = "1.0.0";
    String name = "Testing";
    URI expectedId = URI.create(BASE_UUID_URN + uuid);
    URI versionId = URI.create(BASE_UUID_URN + uuid + ":" + version);
    ResourceIdentifier id = (ResourceIdentifier) SemanticIdentifier.newId(uuid, version, name);

    assertNotNull(id);
    assertEquals(name, id.getName());
    // uuid, tag and resourceId are required
    assertEquals(uuid, id.getUuid());
    assertEquals(uuid.toString(), id.getTag());
    assertEquals(expectedId, id.getResourceId());
    assertEquals(versionId, id.getVersionId());
    assertEquals(version, id.getVersionTag());
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
    assertEquals(MAYO_ASSETS_BASE_URI_URI, rid.getNamespaceUri());
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
    assertEquals(IdentifierTagType.STRING_VALUE, id.getTagFormat());
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
    ScopedIdentifier qid = SemanticIdentifier.newId(MAYO_ASSETS_BASE_URI_URI, tag);

    assertEquals(MAYO_ASSETS_BASE_URI_URI, qid.getNamespaceUri());
    assertEquals("1.3.6.1", qid.getTag());
    assertEquals("_1.3.6.1", qid.getQName().getLocalPart());
    assertEquals(MAYO_ASSETS_BASE_URI, qid.getQName().getNamespaceURI());
    assertNotNull(qid.getUuid());
    assertNotNull(qid.getResourceId());
  }

  @Test
  public void testQNameNullNamespace() {
    String tag = "1.3.6.1";
    ScopedIdentifier qid = SemanticIdentifier.newId(tag);

    assertNull(qid.getNamespace());
    assertEquals("1.3.6.1", qid.getTag());
    assertNotNull(qid.getUuid());
    assertNotNull(qid.getResourceId());
    assertEquals("_1.3.6.1", qid.getQName().getLocalPart());
    assertEquals("", qid.getQName().getNamespaceURI());
  }


  @Test
  public void testQNameID_URN() {
    String tag = "1.3.6.1";
    String expectedQname = BASE_UUID_URN + ":" + tag;
    ScopedIdentifier qid = SemanticIdentifier.newId(BASE_UUID_URN_URI, tag);
    assertEquals(BASE_UUID_URN_URI, qid.getNamespaceUri());
    assertEquals("1.3.6.1", qid.getTag());
    assertEquals("_1.3.6.1", qid.getQName().getLocalPart());
    assertEquals(BASE_UUID_URN, qid.getQName().getNamespaceURI());
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
  public void testIDComposition() {
    ResourceIdentifier uid = (ResourceIdentifier) SemanticIdentifier
        .newId(URI.create("http://foo.bar/"), "baz", Version.valueOf("1.1.0"));
    assertEquals(URI.create("http://foo.bar/baz"), uid.getResourceId());
    assertEquals(URI.create("http://foo.bar/baz/versions/1.1.0"), uid.getVersionId());
  }

  @Test
  public void testPointerComposition() {
    URI expectedResourceId = URI.create("http://foo.bar/baz/5.6.4.3");
    Pointer pid = (Pointer) SemanticIdentifier.newIdAsPointer(URI.create("http://foo.bar/baz/"),
        "5.6.4.3");
    assertNotNull(pid);
    assertEquals(expectedResourceId, pid.getResourceId());
    assertEquals("5.6.4.3", pid.getTag());
    assertEquals(UUID.nameUUIDFromBytes(pid.getResourceId().toString().getBytes()), pid.getUuid());
  }

  @Test
  public void testPointer() {
    UUID uuid = UUID.randomUUID();
    URI expectedResourceId = URI.create("http://foo.bar/baz/"+uuid.toString());
    Pointer pid = (Pointer) SemanticIdentifier.newIdAsPointer(URI.create("http://foo.bar/baz/"),
        uuid.toString(), "Resource Description", URI.create("https://internal/locator/"));
    assertNotNull(pid);
    assertEquals(expectedResourceId, pid.getResourceId());
    assertEquals(uuid.toString(), pid.getTag());
    assertEquals(uuid, pid.getUuid());
    assertEquals("https://internal/locator/", pid.getHref().toString());
    assertEquals("Resource Description", pid.getDescription());
  }

  @Test
  public void testIdentifierFormats() {
    String oidString = "1.5.6.3";
    UUID uuid = UUID.randomUUID();
    String random = "15-4";
    ResourceIdentifier ridOid = (ResourceIdentifier) SemanticIdentifier.newId(oidString);
    ResourceIdentifier ridUuid = (ResourceIdentifier) SemanticIdentifier.newId(uuid);
    ResourceIdentifier ridString = (ResourceIdentifier) SemanticIdentifier.newId(random);


    assertTrue(IdentifierTagType.OID_VALUE.equals(ridOid.getTagFormat()));
    assertFalse(IdentifierTagType.UUID_VALUE.equals(ridOid.getTagFormat()));
    assertFalse(IdentifierTagType.STRING_VALUE.equals(ridOid.getTagFormat()));

    assertTrue(IdentifierTagType.UUID_VALUE.equals(ridUuid.getTagFormat()));
    assertFalse(IdentifierTagType.OID_VALUE.equals(ridUuid.getTagFormat()));
    assertFalse(IdentifierTagType.STRING_VALUE.equals(ridUuid.getTagFormat()));


    assertTrue(IdentifierTagType.STRING_VALUE.equals(ridString.getTagFormat()));
    assertFalse(IdentifierTagType.UUID_VALUE.equals(ridString.getTagFormat()));
    assertFalse(IdentifierTagType.OID_VALUE.equals(ridString.getTagFormat()));

  }

  @Test
  public void testVersionFormatsVersion() {
    String oidString = "1.5.6.3";
    Version semVer = Version.valueOf("1.0.0");
    String semVerString = "3.5.1";
    String sequential = "1";
    // Is there a standard date format?
    String dateFormat = "2020-03-20";
    Date timestamp = DateTimeUtil.now();
    String other = "v3";

    ResourceIdentifier semanticVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, semVer);
    ResourceIdentifier semStringVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, semVerString);
    ResourceIdentifier sequentialVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, sequential);
    ResourceIdentifier dateFormatVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, dateFormat);
    ResourceIdentifier timestampVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, timestamp.toString());
    ResourceIdentifier genericVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, other);


    assertTrue(VersionTagType.SEM_VER.equals(semanticVersion.getVersionFormat()));
    assertNotNull(semanticVersion.getSemanticVersionTag());
    assertEquals("1.0.0", semanticVersion.getSemanticVersionTag().toString());
    assertFalse(VersionTagType.SEQUENTIAL.equals(semanticVersion.getVersionFormat()));
    assertFalse(VersionTagType.TIMESTAMP.equals(semanticVersion.getVersionFormat()));
    assertFalse(VersionTagType.GENERIC.equals(semanticVersion.getVersionFormat()));

    assertTrue(VersionTagType.SEM_VER.equals(semStringVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEQUENTIAL.equals(semStringVersion.getVersionFormat()));
    assertFalse(VersionTagType.TIMESTAMP.equals(semStringVersion.getVersionFormat()));
    assertFalse(VersionTagType.GENERIC.equals(semStringVersion.getVersionFormat()));

    assertTrue(VersionTagType.SEQUENTIAL.equals(sequentialVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEM_VER.equals(sequentialVersion.getVersionFormat()));
    assertFalse(VersionTagType.TIMESTAMP.equals(sequentialVersion.getVersionFormat()));
    assertFalse(VersionTagType.GENERIC.equals(sequentialVersion.getVersionFormat()));

    assertTrue(VersionTagType.TIMESTAMP.equals(dateFormatVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEQUENTIAL.equals(dateFormatVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEM_VER.equals(dateFormatVersion.getVersionFormat()));
    assertFalse(VersionTagType.GENERIC.equals(dateFormatVersion.getVersionFormat()));

    assertTrue(VersionTagType.GENERIC.equals(timestampVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEQUENTIAL.equals(timestampVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEM_VER.equals(timestampVersion.getVersionFormat()));
    assertFalse(VersionTagType.TIMESTAMP.equals(timestampVersion.getVersionFormat()));

    assertTrue(VersionTagType.GENERIC.equals(genericVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEQUENTIAL.equals(genericVersion.getVersionFormat()));
    assertFalse(VersionTagType.TIMESTAMP.equals(genericVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEM_VER.equals(genericVersion.getVersionFormat()));

  }

  @Test
  public void testVersionFormatsString() {
    String oidString = "1.5.6.3";
    String semVer = "1.0.0";
    String sequential = "1";
    // Is there a standard date format?
    String dateFormat = "2020-03-20";
    // Should these formats also test to date?
    String dateFormatGeneric = "20200320-101092";
    Date timestamp = DateTimeUtil.now();
    String other = "v3";

    ResourceIdentifier semanticVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, semVer);
    ResourceIdentifier sequentialVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, sequential);
    ResourceIdentifier dateFormatVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, dateFormat);
    ResourceIdentifier dateFormatGenericVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, dateFormatGeneric);
    ResourceIdentifier timestampVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, timestamp.toString());
    ResourceIdentifier genericVersion = (ResourceIdentifier) SemanticIdentifier.newId(oidString, other);

    assertTrue(VersionTagType.SEM_VER.equals(semanticVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEQUENTIAL.equals(semanticVersion.getVersionFormat()));
    assertFalse(VersionTagType.TIMESTAMP.equals(semanticVersion.getVersionFormat()));
    assertFalse(VersionTagType.GENERIC.equals(semanticVersion.getVersionFormat()));

    assertTrue(VersionTagType.SEQUENTIAL.equals(sequentialVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEM_VER.equals(sequentialVersion.getVersionFormat()));
    assertFalse(VersionTagType.TIMESTAMP.equals(sequentialVersion.getVersionFormat()));
    assertFalse(VersionTagType.GENERIC.equals(sequentialVersion.getVersionFormat()));

    assertTrue(VersionTagType.TIMESTAMP.equals(dateFormatVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEQUENTIAL.equals(dateFormatVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEM_VER.equals(dateFormatVersion.getVersionFormat()));
    assertFalse(VersionTagType.GENERIC.equals(dateFormatVersion.getVersionFormat()));


    assertTrue(VersionTagType.GENERIC.equals(dateFormatGenericVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEQUENTIAL.equals(dateFormatGenericVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEM_VER.equals(dateFormatGenericVersion.getVersionFormat()));
    assertFalse(VersionTagType.TIMESTAMP.equals(dateFormatGenericVersion.getVersionFormat()));


    assertTrue(VersionTagType.GENERIC.equals(timestampVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEQUENTIAL.equals(timestampVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEM_VER.equals(timestampVersion.getVersionFormat()));
    assertFalse(VersionTagType.TIMESTAMP.equals(timestampVersion.getVersionFormat()));


    assertTrue(VersionTagType.GENERIC.equals(genericVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEQUENTIAL.equals(genericVersion.getVersionFormat()));
    assertFalse(VersionTagType.TIMESTAMP.equals(genericVersion.getVersionFormat()));
    assertFalse(VersionTagType.SEM_VER.equals(genericVersion.getVersionFormat()));

  }

}
