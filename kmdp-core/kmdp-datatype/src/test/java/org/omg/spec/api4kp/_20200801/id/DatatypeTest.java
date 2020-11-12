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


import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN;
import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN_URI;
import static edu.mayo.kmdp.registry.Registry.MAYO_ASSETS_BASE_URI;
import static edu.mayo.kmdp.registry.Registry.MAYO_ASSETS_BASE_URI_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSIONS;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newId;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newIdAsPointer;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newNamespaceId;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newVersionId;

import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;


public class DatatypeTest {

  @Test
  void testID() {
    // this should at least compile
    assertNotNull(new ResourceIdentifier()
        .withUuid(UUID.randomUUID())
        .withNamespaceUri(MAYO_ASSETS_BASE_URI_URI)
        .withName("Test ResourceId")
        .withVersionTag("LATEST"));
  }

  @Test
  void testSemanticBuilderAssetId() {
    UUID uuid = UUID.randomUUID();
    URI expectedId = URI.create(MAYO_ASSETS_BASE_URI + uuid);
    URI versionId = URI.create(
        MAYO_ASSETS_BASE_URI + uuid + VERSIONS + Version.valueOf("1.0.0"));
    ResourceIdentifier id = newId(MAYO_ASSETS_BASE_URI_URI, uuid, Version.valueOf("1.0.0"), "testing");

    assertNotNull(id);
    assertEquals("testing", id.getName());
    assertEquals(MAYO_ASSETS_BASE_URI_URI, id.getNamespaceUri());
    assertEquals(uuid, id.getUuid());
    assertEquals(expectedId, id.getResourceId());
    assertEquals(versionId, id.getVersionId());
  }

  @Test
  void testSemanticBuilderAssetIdStringVersion() {
    UUID uuid = UUID.randomUUID();
    URI expectedId = URI.create(MAYO_ASSETS_BASE_URI + uuid);
    URI versionId = URI.create(
        MAYO_ASSETS_BASE_URI + uuid + VERSIONS + "1.0.0");
    ResourceIdentifier id = newId(MAYO_ASSETS_BASE_URI_URI, uuid, "1.0.0", "testing");

    assertNotNull(id);
    assertEquals("testing", id.getName());
    assertEquals(MAYO_ASSETS_BASE_URI_URI, id.getNamespaceUri());
    assertEquals(uuid, id.getUuid());
    assertEquals(expectedId, id.getResourceId());
    assertEquals(versionId, id.getVersionId());
  }

  @Test
  void testSemanticBuilderResourceIdURN() {
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("1.0.0");
    String name = "Testing";
    URI expectedId = URI.create(BASE_UUID_URN + uuid);
    URI versionId = URI.create(BASE_UUID_URN + uuid + ":" + version.toString());
    ResourceIdentifier id = newId(uuid, version, name);

    assertNotNull(id);
    assertEquals(name, id.getName());
    // uuid, tag and resourceId are required
    assertEquals(uuid, id.getUuid());
    assertEquals(uuid.toString(), id.getTag());
    assertEquals(expectedId, id.getResourceId());
    assertEquals(versionId, id.getVersionId());
    assertEquals(version.toString(), id.getVersionTag());
    assertNotNull(id.getEstablishedOn());
    assertNull(id.getNamespaceUri());
  }

  @Test
  void testSemanticBuilderResourceIdVersionStringURN() {
    UUID uuid = UUID.randomUUID();
    String version = "1.0.0";
    String name = "Testing";
    URI expectedId = URI.create(BASE_UUID_URN + uuid);
    URI versionId = URI.create(BASE_UUID_URN + uuid + ":" + version);
    ResourceIdentifier id = newId(uuid, version, name);

    assertNotNull(id);
    assertEquals(name, id.getName());
    // uuid, tag and resourceId are required
    assertEquals(uuid, id.getUuid());
    assertEquals(uuid.toString(), id.getTag());
    assertEquals(expectedId, id.getResourceId());
    assertEquals(versionId, id.getVersionId());
    assertEquals(version, id.getVersionTag());
    assertNotNull(id.getEstablishedOn());
    assertNull(id.getNamespaceUri());
  }


  @Test
  void testResourceIdentifierTagVersion() {
    String tag = "1.6.4.3";
    Version version = Version.valueOf("1.0.0");
    URI expectedResourceId = URI.create(BASE_UUID_URN + tag);
    URI expectedVersionId = URI.create(BASE_UUID_URN + tag + ":" + version.toString());
    ResourceIdentifier rid = (ResourceIdentifier) newId(tag, version);
    assertNotNull(rid);
    assertEquals(tag, rid.getTag());
    assertEquals(version.toString(), rid.getVersionTag());
    assertEquals(expectedResourceId, rid.getResourceId());
    assertEquals(expectedVersionId, rid.getVersionId());
  }

  @Test
  void testResourceIdentifierUUID() {
    UUID uid = UUID.randomUUID();
    URI expectedResourceId = URI.create(BASE_UUID_URN + uid.toString());
    ResourceIdentifier rid = newId(uid);
    assertNotNull(rid);
    // uuid, resourceId and tag are required on ResourceIdentifier; confirm all are there
    assertEquals(uid, rid.getUuid());
    assertEquals(uid.toString(), rid.getTag());
    assertEquals(expectedResourceId, rid.getResourceId());
  }

  @Test
  void testResourceIdentifierUIDTagNamespaceVersion() {
    String tag = "5.4.3.2";
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("5.0.1");
    String name = "TestResource";
    // TODO: confirm: tag is used if provided, else uuid
    URI expectedId = URI.create(MAYO_ASSETS_BASE_URI + tag);
    URI versionId = URI
        .create(MAYO_ASSETS_BASE_URI + tag + VERSIONS + version.toString());

    ResourceIdentifier rid = newId(MAYO_ASSETS_BASE_URI_URI, tag, uuid, version, name);
    assertNotNull(rid);
    // uuid, resourceId and tag are required on ResourceIdentifier; confirm all are there
    assertEquals(expectedId, rid.getResourceId());
    assertEquals(versionId, rid.getVersionId());
    assertEquals(tag, rid.getTag());
    assertEquals(uuid, rid.getUuid());
    assertEquals(name, rid.getName());
  }

  @Test
  void testResourceIdentifierUIDTagNamespaceVersionAsURN() {
    String tag = "5.4.3.2";
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("5.0.1");
    String name = "TestResource";
    // TODO: confirm: tag is used if provided, else uuid
    URI expectedId = URI.create(BASE_UUID_URN + tag);
    URI versionId = URI.create(BASE_UUID_URN + tag + ":" + version.toString());

    ResourceIdentifier rid = newId(BASE_UUID_URN_URI, tag, uuid, version, name);
    assertNotNull(rid);
    // uuid, resourceId and tag are required on ResourceIdentifier; confirm all are there
    assertEquals(expectedId, rid.getResourceId());
    assertEquals(versionId, rid.getVersionId());
    assertEquals(tag, rid.getTag());
    assertEquals(uuid, rid.getUuid());
    assertEquals(name, rid.getName());
  }


  @Test
  void testResourceIdAll() {
    String tag = "5.4.3.2";
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("5.0.1");
    String name = "TestResource";
    Date established = DateTimeUtil.today();
    URI expectedId = URI.create(MAYO_ASSETS_BASE_URI + tag);
    URI versionId = URI
        .create(MAYO_ASSETS_BASE_URI + tag + VERSIONS + version.toString());

    ResourceIdentifier rid = newId(MAYO_ASSETS_BASE_URI_URI, tag, uuid, version, name, established);
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
  void testResourceIdentifierTag() {
    String tag = "1.6.4.3";
    URI expectedResourceId = URI.create(BASE_UUID_URN + tag);
    ResourceIdentifier rid = newId(tag);
    assertNotNull(rid);
    // uuid, resourceId and tag are required on ResourceIdentifier; confirm all are there
    assertEquals(tag, rid.getTag());
    assertEquals(UUID.nameUUIDFromBytes(expectedResourceId.toString().getBytes()), rid.getUuid());
    assertEquals(expectedResourceId, rid.getResourceId());
  }

  @Test
  void testSemanticBuilderUrn() {
    UUID uuid = UUID.randomUUID();
    Version version = Version.valueOf("5.1.6");
    String name = "TestURN";
    URI expectedResourceId = URI.create(BASE_UUID_URN + uuid);
    URI expectedVersionId = URI.create(BASE_UUID_URN + uuid + ":" + version.toString());
    ResourceIdentifier id = newId(BASE_UUID_URN_URI, uuid, version, name);

    assertNotNull(id);
    assertEquals(name, id.getName());
    assertEquals(uuid, id.getUuid());
    assertEquals(version.toString(), id.getVersionTag());
    assertEquals(expectedResourceId, id.getResourceId());
    assertEquals(expectedVersionId, id.getVersionId());
  }

  @Test
  void testSimpleID() {
    Identifier id = newId("thisId");

    assertEquals("thisId", id.getTag());
    assertEquals(IdentifierTagType.STRING_VALUE, id.getTagFormat());
  }

  @Test
  void testVersionedID() {
    String tag = "1.3.6.1";
    String version = "42";
    VersionIdentifier vid = newId(tag, version);

    assertEquals("42", vid.getVersionTag());
    assertEquals("1.3.6.1", vid.getTag());
  }

  @Test
  void testQNameID() {
    String tag = "1.3.6.1";
    ScopedIdentifier qid = newId(MAYO_ASSETS_BASE_URI_URI, tag);

    assertEquals(MAYO_ASSETS_BASE_URI_URI, qid.getNamespaceUri());
    assertEquals("1.3.6.1", qid.getTag());
    assertEquals("_1.3.6.1", qid.getQName().getLocalPart());
    assertEquals(MAYO_ASSETS_BASE_URI, qid.getQName().getNamespaceURI());
    assertNotNull(qid.getUuid());
    assertNotNull(qid.getResourceId());
  }

  @Test
  void testQNameNullNamespace() {
    String tag = "1.3.6.1";
    ScopedIdentifier qid = newId(tag);

    assertNull(qid.getNamespaceUri());
    assertEquals("1.3.6.1", qid.getTag());
    assertNotNull(qid.getUuid());
    assertNotNull(qid.getResourceId());
    assertEquals("_1.3.6.1", qid.getQName().getLocalPart());
    assertEquals("", qid.getQName().getNamespaceURI());
  }


  @Test
  void testQNameID_URN() {
    String tag = "1.3.6.1";
    String expectedQname = BASE_UUID_URN + ":" + tag;
    ScopedIdentifier qid = newId(BASE_UUID_URN_URI, tag);
    assertEquals(BASE_UUID_URN_URI, qid.getNamespaceUri());
    assertEquals("1.3.6.1", qid.getTag());
    assertEquals("_1.3.6.1", qid.getQName().getLocalPart());
    assertEquals(BASE_UUID_URN, qid.getQName().getNamespaceURI());
    assertNotNull(qid.getUuid());
    assertNotNull(qid.getResourceId());
  }

  @Test
  void testExceptionNoTag() {
    Exception exception = assertThrows(IllegalStateException.class,
        () -> newId(""));
    assertEquals("Missing required tag for Identifier", exception.getMessage());
  }

  @Test
  void testIDComposition() {
    ResourceIdentifier uid = newId(URI.create("http://foo.bar/"), "baz", Version.valueOf("1.1.0"));
    assertEquals(URI.create("http://foo.bar/baz"), uid.getResourceId());
    assertEquals(URI.create("http://foo.bar/baz/versions/1.1.0"), uid.getVersionId());
  }

  @Test
  void testPointerComposition() {
    URI expectedResourceId = URI.create("http://foo.bar/baz/5.6.4.3");
    Pointer pid = newIdAsPointer(URI.create("http://foo.bar/baz/"),
        "5.6.4.3");
    assertNotNull(pid);
    assertEquals(expectedResourceId, pid.getResourceId());
    assertEquals("5.6.4.3", pid.getTag());
    assertEquals(UUID.nameUUIDFromBytes(pid.getResourceId().toString().getBytes()), pid.getUuid());
  }

  @Test
  void testPointer() {
    UUID uuid = UUID.randomUUID();
    URI expectedResourceId = URI.create("http://foo.bar/baz/"+uuid.toString());
    Pointer pid = newIdAsPointer(URI.create("http://foo.bar/baz/"),
        uuid.toString(), "Resource Description", "1.3.4",
        URI.create("https://internal/locator/"));
    assertNotNull(pid);
    assertEquals(expectedResourceId, pid.getResourceId());
    assertEquals(uuid.toString(), pid.getTag());
    assertEquals(uuid, pid.getUuid());
    assertEquals("1.3.4", pid.getVersionTag());
    assertEquals("https://internal/locator/", pid.getHref().toString());
    assertEquals("Resource Description", pid.getDescription());
  }


  @Test
  void testPointerTagOnly() {
    UUID uuid = UUID.randomUUID();
    URI expectedResourceId = URI.create(BASE_UUID_URN+uuid.toString());
    Pointer pid = newIdAsPointer(uuid.toString());
    assertNotNull(pid);
    assertEquals(expectedResourceId, pid.getResourceId());
    assertEquals(uuid.toString(), pid.getTag());
    assertEquals(uuid, pid.getUuid());
  }

  @Test
  void testIdentifierFormats() {
    String oidString = "1.5.6.3";
    UUID uuid = UUID.randomUUID();
    String random = "15-4";
    ResourceIdentifier ridOid = newId(oidString);
    ResourceIdentifier ridUuid = newId(uuid);
    ResourceIdentifier ridString = newId(random);

    assertEquals(IdentifierTagType.OID_VALUE, ridOid.getTagFormat());
    assertNotEquals(IdentifierTagType.UUID_VALUE, ridOid.getTagFormat());
    assertNotEquals(IdentifierTagType.STRING_VALUE, ridOid.getTagFormat());

    assertEquals(IdentifierTagType.UUID_VALUE, ridUuid.getTagFormat());
    assertNotEquals(IdentifierTagType.OID_VALUE, ridUuid.getTagFormat());
    assertNotEquals(IdentifierTagType.STRING_VALUE, ridUuid.getTagFormat());

    assertEquals(IdentifierTagType.STRING_VALUE, ridString.getTagFormat());
    assertNotEquals(IdentifierTagType.UUID_VALUE, ridString.getTagFormat());
    assertNotEquals(IdentifierTagType.OID_VALUE, ridString.getTagFormat());

  }

  @Test
  void testVersionFormatsVersion() {
    String oidString = "1.5.6.3";
    Version semVer = Version.valueOf("1.0.0");
    String semVerString = "3.5.1";
    String sequential = "1";
    // Is there a standard date format?
    String dateFormat = "2020-03-20";
    Date timestamp = DateTimeUtil.today();
    String other = "v3";

    ResourceIdentifier semanticVersion = newId(oidString, semVer);
    ResourceIdentifier semStringVersion = newId(oidString, semVerString);
    ResourceIdentifier sequentialVersion = newId(oidString, sequential);
    ResourceIdentifier dateFormatVersion = newId(oidString, dateFormat);
    ResourceIdentifier timestampVersion = newId(oidString, Long.toString(timestamp.getTime()));
    ResourceIdentifier genericVersion = newId(oidString, other);

    assertEquals(VersionTagType.SEM_VER, semanticVersion.getVersionFormat());
    assertNotNull(semanticVersion.getSemanticVersionTag());
    assertEquals("1.0.0", semanticVersion.getSemanticVersionTag().toString());
    assertNotEquals(VersionTagType.SEQUENTIAL, semanticVersion.getVersionFormat());
    assertNotEquals(VersionTagType.TIMESTAMP, semanticVersion.getVersionFormat());
    assertNotEquals(VersionTagType.GENERIC, semanticVersion.getVersionFormat());

    assertEquals(VersionTagType.SEM_VER, semStringVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEQUENTIAL, semStringVersion.getVersionFormat());
    assertNotEquals(VersionTagType.TIMESTAMP, semStringVersion.getVersionFormat());
    assertNotEquals(VersionTagType.GENERIC, semStringVersion.getVersionFormat());

    assertEquals(VersionTagType.SEQUENTIAL, sequentialVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEM_VER, sequentialVersion.getVersionFormat());
    assertNotEquals(VersionTagType.TIMESTAMP, sequentialVersion.getVersionFormat());
    assertNotEquals(VersionTagType.GENERIC, sequentialVersion.getVersionFormat());

    assertEquals(VersionTagType.TIMESTAMP, dateFormatVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEQUENTIAL, dateFormatVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEM_VER, dateFormatVersion.getVersionFormat());
    assertNotEquals(VersionTagType.GENERIC, dateFormatVersion.getVersionFormat());

    assertEquals(VersionTagType.SEQUENTIAL, timestampVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEM_VER, timestampVersion.getVersionFormat());
    assertNotEquals(VersionTagType.TIMESTAMP, timestampVersion.getVersionFormat());

    assertEquals(VersionTagType.GENERIC, genericVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEQUENTIAL, genericVersion.getVersionFormat());
    assertNotEquals(VersionTagType.TIMESTAMP, genericVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEM_VER, genericVersion.getVersionFormat());

  }

  @Test
  void testVersionFormatsString() {
    String oidString = "1.5.6.3";
    String semVer = "1.0.0";
    String sequential = "1";
    // Is there a standard date format?
    String dateFormat = "2020-03-20";
    // Should these formats also test to date?
    String dateFormatGeneric = "20200320-101092";
    Date timestamp = DateTimeUtil.today();
    String other = "v3";

    ResourceIdentifier semanticVersion = newId(oidString, semVer);
    ResourceIdentifier sequentialVersion = newId(oidString, sequential);
    ResourceIdentifier dateFormatVersion = newId(oidString, dateFormat);
    ResourceIdentifier dateFormatGenericVersion = newId(oidString, dateFormatGeneric);
    ResourceIdentifier timestampVersion = newId(oidString, Long.toString(timestamp.getTime()));
    ResourceIdentifier genericVersion = newId(oidString, other);

    assertEquals(VersionTagType.SEM_VER, semanticVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEQUENTIAL, semanticVersion.getVersionFormat());
    assertNotEquals(VersionTagType.TIMESTAMP, semanticVersion.getVersionFormat());
    assertNotEquals(VersionTagType.GENERIC, semanticVersion.getVersionFormat());

    assertEquals(VersionTagType.SEQUENTIAL, sequentialVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEM_VER, sequentialVersion.getVersionFormat());
    assertNotEquals(VersionTagType.TIMESTAMP, sequentialVersion.getVersionFormat());
    assertNotEquals(VersionTagType.GENERIC, sequentialVersion.getVersionFormat());

    assertEquals(VersionTagType.TIMESTAMP, dateFormatVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEQUENTIAL, dateFormatVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEM_VER, dateFormatVersion.getVersionFormat());
    assertNotEquals(VersionTagType.GENERIC, dateFormatVersion.getVersionFormat());

    assertEquals(VersionTagType.GENERIC, dateFormatGenericVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEQUENTIAL, dateFormatGenericVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEM_VER, dateFormatGenericVersion.getVersionFormat());
    assertNotEquals(VersionTagType.TIMESTAMP, dateFormatGenericVersion.getVersionFormat());

    assertEquals(VersionTagType.SEQUENTIAL, timestampVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEM_VER, timestampVersion.getVersionFormat());
    assertNotEquals(VersionTagType.TIMESTAMP, timestampVersion.getVersionFormat());

    assertEquals(VersionTagType.GENERIC, genericVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEQUENTIAL, genericVersion.getVersionFormat());
    assertNotEquals(VersionTagType.TIMESTAMP, genericVersion.getVersionFormat());
    assertNotEquals(VersionTagType.SEM_VER, genericVersion.getVersionFormat());

  }

  @Test
  void testKeyIdentifiers() {
    SemanticIdentifier id1 = newId("thisId", Version.valueOf("0.0.0"));
    SemanticIdentifier id2 = newId("thisId", "0.0.0");
    SemanticIdentifier id3 = newId("thisId", "0.0.1");

    assertEquals(id1.asKey(), id2.asKey());
    assertEquals(id1.hashCode(), id2.hashCode());

    assertNotEquals(id1.hashCode(), id3.hashCode());
    assertNotEquals(id1.asKey(), id3.asKey());
  }

  @Test
  void testVersionedUriWithQualifiedVersions() {
    URI uri = URI.create("http://foo.bar/blah/test/123/versions/2131");
    ResourceIdentifier rid = newVersionId(uri);

    assertEquals("123", rid.getTag());
    assertEquals("2131", rid.getVersionTag());
    assertEquals(URI.create("http://foo.bar/blah/test/"), rid.getNamespaceUri());
  }

  @Test
  void testVersionedUriWithUUIDandVersion() {
    UUID uuid = UUID.nameUUIDFromBytes("mock".getBytes());
    URI uri = URI.create("urn:uuid:" + uuid + ":0");
    ResourceIdentifier rid = newVersionId(uri);

    assertEquals(uuid, rid.getUuid());
    assertEquals(uuid.toString(), rid.getTag());
    assertEquals("0", rid.getVersionTag());
    assertEquals(BASE_UUID_URN_URI, rid.getNamespaceUri());
  }

  @Test
  void testVersionedUriWithUUIDandQualifiedVersions() {
    UUID uuid = UUID.nameUUIDFromBytes("mock".getBytes());
    URI uri = URI.create("http://foo.bar/blah/test/" + uuid + "/versions/2131");
    ResourceIdentifier rid = newVersionId(uri);

    assertEquals(uuid, rid.getUuid());
    assertEquals(uuid.toString(), rid.getTag());
    assertEquals("2131", rid.getVersionTag());
    assertEquals(URI.create("http://foo.bar/blah/test/"), rid.getNamespaceUri());
  }

  @Test
  void testVersionedUriWithDateTimePattern() {
    URI uri = URI.create("http://foo.bar/blah/20200301/example");
    ResourceIdentifier rid =
        newVersionId(uri,
            Pattern.compile("(.*)/(\\d+)/(\\w+)"),2,3);

    assertEquals("20200301", rid.getVersionTag());
    assertEquals("example", rid.getTag());
    assertEquals(URI.create("http://foo.bar/blah"), rid.getNamespaceUri());
  }

  @Test
  void testNamespaceOnlyURIFailure() {
    assertThrows(IllegalArgumentException.class,
        () -> newId(URI.create("http://foo.bar")));
  }

  @Test
  void testNamespaceOnlyURI() {
    URI uri = URI.create("http://foo.bar");
    ResourceIdentifier rid = newNamespaceId(uri);
    assertEquals(uri, rid.getNamespaceUri());
    assertEquals("foo.bar", rid.getTag());
  }

  @Test
  void testURIwithVersionURI() {
    URI uri = URI.create("http://foo.bar/x");
    URI vuri = URI.create("http://foo.bar/x/2020");
    ResourceIdentifier rid = SemanticIdentifier.newVersionId(uri,vuri);
    assertEquals(uri,rid.getResourceId());
    assertEquals(vuri,rid.getVersionId());

    assertEquals("x", rid.getTag());
    assertEquals("2020", rid.getVersionTag());
  }


  @Test
  void testURIwithVersionURI2() {
    URI uri = URI.create("http://foo.bar/x");
    URI vuri = URI.create("http://foo.bar/2020/x");
    ResourceIdentifier rid = SemanticIdentifier.newVersionId(uri,vuri);
    assertEquals(uri,rid.getResourceId());
    assertEquals(vuri,rid.getVersionId());

    assertEquals("x", rid.getTag());
    assertEquals("2020", rid.getVersionTag());
  }

  @Test
  void testVersionIdWithFragments() {
    URI uri = URI.create("http://foo.bar#x");
    ResourceIdentifier rid = SemanticIdentifier.newVersionId(uri,"CURRENT");
    assertEquals("http://foo.bar/versions/CURRENT#x", rid.getVersionId().toString());
    assertEquals(uri, rid.getResourceId());
  }

  @Test
  void testVersionIdWithURN() {
    URI uri = URI.create("urn:uuid:x");
    ResourceIdentifier rid = SemanticIdentifier.newVersionId(uri,"CURRENT");
    assertEquals("urn:uuid:x:CURRENT", rid.getVersionId().toString());
    assertEquals(uri, rid.getResourceId());
  }

  @Test
  void testNamespaceWithURN() {
    URI uri = URI.create("urn:uuid");
    ResourceIdentifier nsId = SemanticIdentifier.newNamespaceId(uri);
    assertEquals("urn:uuid", nsId.getResourceId().toString());
  }
}
