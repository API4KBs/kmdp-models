package org.omg.spec.api4kp._20200801.id;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.getDefaultVersionId;
import static edu.mayo.kmdp.registry.Registry.DID_URN;
import static edu.mayo.kmdp.registry.Registry.DID_URN_URI;
import static java.util.Arrays.copyOfRange;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSIONS_FRAG_RX;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSIONS_RX;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_LATEST;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_ZERO;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_ZERO_SNAPSHOT;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.versionSeparator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for generating ResourceIdentifiers. resourceIdentifier will be composed of parts
 * provided uuid or tag must be provided at a minimum
 * <p>
 * •	an explicit uuid can be the tag, if no other tag is provided •	resourceId can be constructed
 * from either tag or uuid, plus the namespace/base uri •	if a uuid is not provided explicitly, we
 * can use UUID.namedUUIDfromBytes(). using resourceId.toString.getBytes since resourceId is a URI
 */
public interface SemanticIdentifier extends VersionIdentifier, ScopedIdentifier,
    UniversalIdentifier {

  Logger logger = LoggerFactory.getLogger(SemanticIdentifier.class);

  /**
   * Create ResourceIdentifier for the URI provided. Will generate tag and resourceId as required
   * values.
   *
   * @return ResourceIdentifier
   */
  static ResourceIdentifier newId(URI uri) {
    String uriStr = uri.toString();
    String tag = URIUtil.detectLocalName(uri);
    URI nsUri = URI.create(uriStr.substring(0, uriStr.lastIndexOf(tag) - 1));
    if (!Registry.isGlobalIdentifier(nsUri) && nsUri.getAuthority() == null) {
      throw new IllegalArgumentException("Unable to split the URI into a namespace and a tag, "
          + "consider using newNamespaceId instead? " + nsUri);
    }
    return new ResourceIdentifier()
        // generate required tag from uuid
        // set required fields
        .withTag(tag)
        .withNamespaceUri(nsUri)
        .withResourceId(uri)
        .withEstablishedOn(DateTimeUtil.today())
        .withUuid(Util.isUUID(tag)
            ? UUID.fromString(tag)
            : Util.uuid(tag));
  }

  /**
   * Create ResourceIdentifier for the URI provided. Will generate tag and resourceId as required
   * values.
   *
   * @return ResourceIdentifier
   */
  static UUID newIdAsUUID(String uriStr) {
    String tag = URIUtil.detectLocalName(uriStr);
    return Util.isUUID(tag)
        ? UUID.fromString(tag)
        : Util.uuid(tag);
  }

  /**
   * Create ResourceIdentifier for the URI provided, which is expected to be a Namespace URI
   *
   * @param uri A Namespace URI
   * @return ResourceIdentifier
   */
  static ResourceIdentifier newNamespaceId(URI uri) {
    return newNamespaceId(uri, null);
  }

  /**
   * Create ResourceIdentifier for the URI provided, which is expected to be a Namespace URI
   *
   * @param uri A Namespace URI
   * @param versionTag A Namespace version tag (assuming Namespaces are versioned)
   * @return ResourceIdentifier
   */
  static ResourceIdentifier newNamespaceId(URI uri, String versionTag) {
    String tag = URIUtil.detectLocalName(uri);
    return new ResourceIdentifier()
        // generate required tag from uuid
        // set required fields
        .withTag(tag)
        .withVersionTag(versionTag)
        .withNamespaceUri(uri)
        .withResourceId(uri)
        .withEstablishedOn(DateTimeUtil.today())
        .withUuid(Util.isUUID(tag)
            ? UUID.fromString(tag)
            : Util.uuid(tag))
        .withVersionId(getDefaultVersionId(uri,versionTag));
  }


  /**
   * Generate a ResourceIdentifier from a URN
   * that follows the pattern [base_urn]:{uuid}:{versionTag}
   *
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newUrnVersionId(String versionUrn) {
    String[] segments = versionUrn.split(":");
    int n = segments.length;
    if (n < 2) {
      throw new IllegalArgumentException("Unable to construct a URN-based versioned URI from " + versionUrn);
    }
    String versionTag = segments[n-1];
    String tag = segments[n-2];
    URI base = n > 2
        ? URI.create(String.join(":", copyOfRange(segments, 0, n - 2)))
        : DID_URN_URI;
    return newId(base, tag, versionTag);
  }

  /**
   * Generate resourceIdentifier for URI that includes version information according to known
   * patterns
   *
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newVersionId(URI versionUri) {
    return newVersionId(versionUri, VERSIONS_RX, 3, 2);
  }

  /**
   * Tentatively generate a resourceIdentifier for URI that includes version information
   * according to known patterns
   *
   * @return ResourceIdentifier with required values set
   */
  static Optional<ResourceIdentifier> tryNewVersionId(URI versionUri) {
    return tryNewVersionId(versionUri, VERSIONS_RX, 3, 2);
  }

  /**
   * Generate resourceIdentifier from a series URI and a version Tag
   *
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newVersionId(URI seriesUri, String versionTag) {
    if (seriesUri.getFragment() == null) {
      return newVersionId(
          URI.create(seriesUri + versionSeparator(seriesUri) + versionTag));
    } else {
      String versionedURI = URIUtil.normalizeURIString(seriesUri) + versionSeparator(seriesUri) + versionTag + "#" + seriesUri.getFragment();
      return newVersionId( URI.create(versionedURI), VERSIONS_FRAG_RX, 2, 3);
    }
  }

  /**
   * Generate resourceIdentifier from a series URI and a version Tag
   *
   * @return ResourceIdentifier with required values set
   */
  static Optional<ResourceIdentifier> tryNewVersionId(URI seriesUri, String versionTag) {
    if (seriesUri.getFragment() == null) {
      return tryNewVersionId(
          URI.create(seriesUri + versionSeparator(seriesUri) + versionTag));
    } else {
      String versionedURI = URIUtil.normalizeURIString(seriesUri) + versionSeparator(seriesUri) + versionTag + "#" + seriesUri.getFragment();
      return tryNewVersionId( URI.create(versionedURI), VERSIONS_FRAG_RX, 2, 3);
    }
  }

  /**
   * Generate resourceIdentifier for URI that includes version information according to a
   * user-specified Pattern
   * <p>
   * The Pattern can specify two groups (base URI + version) or three groups (base URI + tag +
   * version)
   *
   * @param versionUri      the URI that contains version information
   * @param versionPattern  the Pattern that allows to deconstruct the URI
   * @param versionGroupIdx the index of the pattern group that contains the version information
   * @param tagGroupIdx     the index of the pattern group that contains the base and/or tag
   *                        information
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newVersionId(URI versionUri, Pattern versionPattern,
      int versionGroupIdx, int tagGroupIdx) {
    if (Registry.isGlobalIdentifier(versionUri)) {
      // assume pattern urn:uuid:tag:version
      String str = versionUri.toString();
      StringTokenizer tok = new StringTokenizer(str, ":");
      if (tok.countTokens() >= 4) {
        int idx = str.lastIndexOf(':');
        return newId(DID_URN_URI,
            str.substring(DID_URN.length(), idx),
            str.substring(idx+1));
      }
    }
    Matcher m = versionPattern.matcher(versionUri.toString());
    if (m.matches()) {
      switch (m.groupCount()) {
        case 2:
          return newVersionId(URI.create(m.group(tagGroupIdx)), versionUri);
        case 3:
          return newId(URI.create(m.group(1)), m.group(tagGroupIdx), m.group(versionGroupIdx));
        default:
      }
    }
    return newId(versionUri);
  }

  static Optional<ResourceIdentifier> tryNewVersionId(URI versionUri, Pattern versionPattern,
      int versionGroupIdx, int tagGroupIdx) {
    if (Registry.isGlobalIdentifier(versionUri)) {
      StringTokenizer tok = new StringTokenizer(versionUri.toString(), ":");
      if (tok.countTokens() < 4) {
        return Optional.empty();
      }
    } else {
      Matcher m = versionPattern.matcher(versionUri.toString());
      if (!m.matches()) {
        return Optional.empty();
      }
    }
    return Optional.of(newVersionId(versionUri,versionPattern,versionGroupIdx,tagGroupIdx));
  }

  /**
   * Generate resourceIdentifier for URI that includes version information according to a
   * user-specified Pattern Further assumes that the tag is date-based, and can be parsed as a date
   * using a given pattern. The date will be used as the identifier's establishing date
   *
   * @param versionUri      the URI that contains version information
   * @param versionPattern  the Pattern that allows to deconstruct the URI
   * @param versionGroupIdx the index of the pattern group that contains the version information
   * @param tagGroupIdx     the index of the pattern group that contains the base and/or tag
   *                        information
   * @param datePattern     the pattern used to parse the tag into the establishedDate
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newDateVersionId(URI versionUri, Pattern versionPattern,
      int versionGroupIdx, int tagGroupIdx, Pattern datePattern) {
    ResourceIdentifier id = newVersionId(versionUri, versionPattern, versionGroupIdx, tagGroupIdx);
    Matcher m = datePattern.matcher(id.getTag());
    if (m.matches()) {
      id.withEstablishedOn(DateTimeUtil.parseDate(id.getTag(), datePattern.pattern()));
    }
    return id;
  }

  /**
   * Generate resourceIdentifier from a series URI and a version URI
   *
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newVersionId(URI seriesUri, URI versionUri) {
    return newVersionId(seriesUri,versionUri,null);
  }

  /**
   * Generate resourceIdentifier from a series URI and a version URI, and a given name/label
   *
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newVersionId(URI seriesUri, URI versionUri, String name) {
    String uriStr = seriesUri.toString();
    String tag = URIUtil.detectLocalName(seriesUri);
    URI nsUri = URI.create(uriStr.substring(0, uriStr.lastIndexOf(tag)));
    if (!Registry.isGlobalIdentifier(nsUri) && nsUri.getAuthority() == null) {
      throw new IllegalArgumentException("Unable to split the URI into a namespace and a tag, "
          + "consider using newNamespaceId instead? " + nsUri);
    }
    String vTag = NameUtils.strip(uriStr, versionUri.toString());
    if (vTag.startsWith("/")) {
      vTag = vTag.substring(1);
    }
    if (vTag.endsWith("/")) {
      vTag = vTag.substring(0, vTag.length() - 1);
    }
    ResourceIdentifier rid = new ResourceIdentifier()
        // generate required tag from uuid
        // set required fields
        .withTag(tag)
        .withNamespaceUri(nsUri)
        .withResourceId(seriesUri)
        .withVersionId(versionUri)
        .withVersionTag(vTag)
        .withUuid(Util.isUUID(tag)
            ? UUID.fromString(tag)
            : Util.uuid(tag))
        .withName(name);
    if (rid.getVersionFormat() == VersionTagType.TIMESTAMP) {
      rid.withEstablishedOn(DateTimeUtil.parseDate(vTag));
    } else {
      rid.withEstablishedOn(DateTimeUtil.today());
    }
    return rid;
  }

  /**
   * Creates an Identifier for a named entity
   *
   * @param resourceId the URI that identifies the entity
   * @param tag        the entity's tag
   * @param label      the entity's name
   * @return an Identifier of the entity
   */
  static ResourceIdentifier newNamedId(URI resourceId, String tag, String label) {
    return newId(resourceId)
        .withTag(tag)
        .withName(label);
  }

  /**
   * Creates an Identifier for a named entity
   *
   * @param resourceId the URI that identifies the entity
   * @param tag        the entity's tag
   * @param label      the entity's name
   * @param versionTag the entity's version tag
   * @param establishedOn the date the identifier was assigned
   * @return an Identifier of the entity
   */
  static ResourceIdentifier newNamedId(URI resourceId, String tag, String label,
      String versionTag, Date establishedOn) {
    return newId(resourceId)
        .withTag(tag)
        .withVersionTag(versionTag)
        .withName(label)
        .withEstablishedOn(establishedOn);
  }

  /**
   * Create ResourceIdentifier for the UUID provided. Will generate tag and resourceId as required
   * values.
   *
   * @return ResourceIdentifier
   */
  static ResourceIdentifier newId(URI base, UUID uuid) {
    checkUUID(uuid);
    return newId(toResourceId(uuid.toString(), base, uuid));
  }

  /**
   * Create ResourceIdentifier for the UUID provided. Will generate tag and resourceId as required
   * values.
   *
   * @return ResourceIdentifier
   */
  static ResourceIdentifier newId(UUID uuid) {
    checkUUID(uuid);
    // compose required resourceId from uuid
    URI resourceId = toResourceId(uuid);
    return new ResourceIdentifier()
        // generate required tag from uuid
        // set required fields
        .withTag(uuid.toString())
        .withResourceId(resourceId)
        .withEstablishedOn(DateTimeUtil.today())
        .withUuid(uuid);
  }

  /**
   * Generate ResourceIdentifier from namespace, tag and version. Required UUID will be generated
   * from composed resourceId
   *
   * @return ResourceIdentifier
   */
  static ResourceIdentifier newId(URI namespace, UUID tag, Version version) {
    return newId(namespace, tag.toString(), version.toString());
  }

  static ResourceIdentifier newId(URI namespace, UUID tag, String version) {
    return newId(namespace, tag.toString(), version);
  }

  static ResourceIdentifier newId(UUID tag, Version version) {
    return newId(DID_URN_URI, tag.toString(), version.toString());
  }

  static ResourceIdentifier newId(UUID tag, String version) {
    return newId(DID_URN_URI, tag.toString(), version);
  }

  static ResourceIdentifier newId(URI namespace, String tag, Version versionTag) {
    return newId(namespace, tag, versionTag.toString());
  }

  static ResourceIdentifier newId(URI namespace, String tag, String versionTag) {
    URI resourceId = toResourceId(tag, namespace);
    return new ResourceIdentifier()
        .withVersionTag(versionTag)
        // set required fields
        .withTag(tag)
        .withResourceId(resourceId)
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withEstablishedOn(DateTimeUtil.today())
        .withNamespaceUri(namespace)
        .withVersionId(getDefaultVersionId(resourceId,versionTag));
  }

  /**
   * Create ResourceIdentifier from namespace, UUID, version and name. Required tag will be
   * generated from UUID.
   *
   * @param namespace namespace used to compose the resourceId
   * @param uuid      used to compose resourceId and create required tag value
   * @param version   the version of the resource
   * @param name      the name for the resource
   * @return ResourceIdentifier with appropriate values set
   */
  static ResourceIdentifier newId(URI namespace, UUID uuid, Version version, String name) {
    return newId(namespace, uuid, version.toString(), name);
  }

  static ResourceIdentifier newId(URI namespace, UUID uuid, String versionTag, String name) {
    checkUUID(uuid);
    // create URI id
    URI uri = toResourceId(uuid.toString(), namespace, uuid);
    return new ResourceIdentifier()
        .withVersionTag(versionTag)
        // set required fields
        .withTag(uuid.toString())
        .withResourceId(uri)
        .withUuid(uuid)
        .withNamespaceUri(namespace)
        .withEstablishedOn(DateTimeUtil.today())
        .withName(name)
        .withVersionId(getDefaultVersionId(uri,versionTag));
  }

  /**
   * Generate ResourceIdentifier from uuid, version and name Will generate required tag from uuid
   * Will default namespace
   *
   * @return ResourceIdentifier
   */
  static ResourceIdentifier newId(UUID uuid, Version version, String name) {
    return newId(uuid, version.toString(), name);
  }

  static ResourceIdentifier newId(UUID uuid, String versionTag, String name) {
    checkUUID(uuid);
    // create URN id
    URI uri = toResourceId(uuid.toString(), uuid);
    return new ResourceIdentifier()
        .withUuid(uuid)
        // generate required tag from UUID
        .withTag(uuid.toString())
        .withResourceId(uri)
        .withVersionTag(versionTag)
        .withEstablishedOn(DateTimeUtil.today())
        .withName(name)
        .withVersionId(getDefaultVersionId(uri,versionTag));
  }

  /**
   * compose ResourceIdentifier from the given parts
   *
   * @return ResourceIdentifier
   */
  static ResourceIdentifier newId(URI namespace, String tag, UUID uuid, Version version,
      String name) {
    return newId(namespace, tag, uuid, version.toString(), name);
  }

  static ResourceIdentifier newId(URI namespace, String tag, UUID uuid, String versionTag,
      String name) {
    checkUUID(uuid);
    checkTag(tag);
    URI uri = toResourceId(tag, namespace, uuid);
    return new ResourceIdentifier()
        .withUuid(uuid)
        .withTag(tag)
        .withVersionTag(versionTag)
        .withNamespaceUri(namespace)
        .withName(name)
        .withEstablishedOn(DateTimeUtil.today())
        .withResourceId(uri)
        .withVersionId(getDefaultVersionId(uri,versionTag));
  }

  /**
   * Generate ResourceIdentifier with all the attributes
   *
   * @return ResourceIdentifier
   */
  static ResourceIdentifier newId(URI namespace, String tag, UUID uuid, Version version,
      String name, Date establishedOn) {
    return newId(namespace, tag, uuid, version.toString(), name, establishedOn);
  }

  static ResourceIdentifier newId(URI namespace, String tag, UUID uuid, String versionTag,
      String name, Date establishedOn) {
    checkUUID(uuid);
    checkTag(tag);
    URI uri = toResourceId(tag, namespace, uuid);
    return new ResourceIdentifier()
        .withUuid(uuid)
        .withTag(tag)
        .withVersionTag(versionTag)
        .withNamespaceUri(namespace)
        .withName(name)
        .withResourceId(uri)
        .withEstablishedOn(establishedOn)
        .withVersionId(getDefaultVersionId(uri,versionTag));
  }

  /**
   * Compose ResourceIdentifier from provided parts UUID will be generated from resourceIdentifier
   *
   * @return ResourceIdentifier with required values
   */
  static ResourceIdentifier newId(URI namespace, String tag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag, namespace);
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        .withNamespaceUri(namespace)
        .withEstablishedOn(DateTimeUtil.today())
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  /**
   * Generate resourceIdentifier for tag Will default to URN will generate uuid from resourceId
   *
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newId(String tag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag);
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        .withEstablishedOn(DateTimeUtil.today())
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  /**
   * Generate resourceIdentifier for tag Will default to URN will generate uuid from resourceId
   *
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newName(String tag) {
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(URI.create(Registry.URN + tag))
        .withEstablishedOn(DateTimeUtil.today())
        // generate required UUID from resourceId
        .withUuid(Util.uuid(tag));
  }

  /**
   * Create a ResourceIdentifier from the parts given. ResourceId will be composed of default URN
   * URI and tag UUID will be generated from resourceId
   *
   * @return ResourceIdentifier with all required attributes set
   */
  static ResourceIdentifier newId(String tag, Version version) {
    return newId(tag, version.toString());
  }

  static ResourceIdentifier newId(String tag, String versionTag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag);
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withEstablishedOn(DateTimeUtil.today())
        .withVersionTag(versionTag)
        .withVersionId(getDefaultVersionId(resourceId,versionTag));
  }

  /**
   * Pointer for client/server interaction.
   *
   * @param namespace
   * @param tag
   * @return Pointer with required resourceId, tag and UUID set
   */
  static Pointer newIdAsPointer(URI namespace, String tag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag, namespace);
    return new org.omg.spec.api4kp._20200801.id.resources.Pointer()
        .withNamespaceUri(namespace)
        .withTag(tag)
        .withResourceId(resourceId)
        .withEstablishedOn(DateTimeUtil.today())
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  /**
   * Pointer for client/server interaction.
   *
   * @param tag
   * @param versionTag
   * @return Pointer with required resourceId, tag and UUID set
   */
  static Pointer newIdAsPointer(UUID tag, String versionTag) {
    URI resourceId = toResourceId(tag.toString(), DID_URN_URI);
    return new org.omg.spec.api4kp._20200801.id.resources.Pointer()
        .withNamespaceUri(resourceId)
        .withTag(tag.toString())
        .withVersionTag(versionTag)
        .withResourceId(resourceId)
        .withEstablishedOn(DateTimeUtil.today())
        .withUuid(tag)
        .withVersionId(getDefaultVersionId(resourceId,versionTag));
  }

  /**
   * Pointer for client/server interaction.
   *
   * @param namespace   resource namespace
   * @param tag         resource tag
   * @param description human readable description of the resource
   * @param locator     use for cases when the resource URI cannot be dereferenced or the server
   *                    wants to point the client to a specific location where the denoted resource
   *                    is available
   * @return Pointer with required resourceid, tag, UUID and all other values provided set
   */
  static Pointer newIdAsPointer(URI namespace, String tag, String description,
      String versionTag, URI locator) {
    checkTag(tag);
    URI resourceId = toResourceId(tag, namespace);
    return new org.omg.spec.api4kp._20200801.id.resources.Pointer()
        .withNamespaceUri(namespace)
        .withTag(tag)
        .withVersionTag(versionTag)
        .withResourceId(resourceId)
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withDescription(description)
        .withEstablishedOn(DateTimeUtil.today())
        .withHref(locator)
        .withVersionId(getDefaultVersionId(resourceId,versionTag));
  }

  /**
   * Pointer for client/server interaction.
   * Constructs (version) URIs from namespace + tag (+ versionTag) elements
   *
   * @param namespace   resource namespace
   * @param resourceUUID  a UUID that denotes the referenced resource
   * @param tag         a tag that, within the scope of the namespace, denotes the referenced resource
   * @param description human readable description of the resource
   * @param name        human readable name of the resource - denotes the resource, but is not an unabmbiguous identifier
   * @param versionTag  version tag that denotes a specific version of the resource (assumed to be Versionable)
   * @param entityType  type that classifies the resource
   * @param locator     use for cases when the resource URI cannot be dereferenced or the server
   *                    wants to point the client to a specific location where the denoted resource
   *                    is available
   * @return Pointer with required resourceid, tag, UUID and all other values provided set
   */
  static Pointer newIdAsPointer(URI namespace, UUID resourceUUID, String tag, String name,
      String versionTag, String description, URI entityType, URI locator) {
    checkTag(tag);
    URI resourceId = toResourceId(tag, namespace);
    return new org.omg.spec.api4kp._20200801.id.resources.Pointer()
        .withNamespaceUri(namespace)
        .withType(entityType)
        .withTag(tag)
        .withName(name)
        .withVersionTag(versionTag)
        .withResourceId(resourceId)
        .withUuid(resourceUUID)
        .withDescription(description)
        .withEstablishedOn(DateTimeUtil.today())
        .withHref(locator)
        .withVersionId(getDefaultVersionId(resourceId,versionTag));
  }

  /**
   * Pointer for client/server interaction.
   * Constructs (version) URIs from namespace + tag (+ versionTag) elements
   * Constructs a UUID using the tag and/or the URI
   *
   * @param namespace   resource namespace
   * @param tag         a tag that, within the scope of the namespace, denotes the referenced resource
   * @param description human readable description of the resource
   * @param name        human readable name of the resource - denotes the resource, but is not an unabmbiguous identifier
   * @param versionTag  version tag that denotes a specific version of the resource (assumed to be Versionable)
   * @param entityType  type that classifies the resource
   * @param locator     use for cases when the resource URI cannot be dereferenced or the server
   *                    wants to point the client to a specific location where the denoted resource
   *                    is available
   * @return Pointer with required resourceid, tag, UUID and all other values provided set
   */
  static Pointer newIdAsPointer(URI namespace, String tag, String name,
      String versionTag, String description, URI entityType, URI locator) {
    URI resourceId = toResourceId(tag, namespace);
    return newIdAsPointer(
        namespace,
        UniversalIdentifier.toUUID(tag, resourceId),
        tag,
        name,
        versionTag,
        description,
        entityType,
        locator);
  }

  /**
   * Create a new Pointer from tag.
   *
   * @param tag value to use to generate resourceId and set tag
   * @return Pointer with required values set
   */
  static Pointer newIdAsPointer(String tag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag);
    return new org.omg.spec.api4kp._20200801.id.resources.Pointer()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  static Pointer newVersionIdAsPointer(URI versionURI) {
    return newVersionId(versionURI)
        .toPointer();
  }

  static Pointer newVersionIdAsPointer(URI namespace, URI resourceId, URI versionId,
      UUID uuid, String tag, String versionTag, String name,
      URI href, URI type, String mime) {
    return new Pointer()
        .withNamespaceUri(namespace)
        .withResourceId(resourceId)
        .withVersionId(versionId)
        .withUuid(uuid)
        .withTag(tag)
        .withVersionTag(versionTag)
        .withName(name)
        .withHref(href)
        .withType(type)
        .withMimeType(mime);
  }

  static ResourceIdentifier randomId() {
    return newId(DID_URN_URI, UUID.randomUUID(), IdentifierConstants.VERSION_LATEST);
  }

  /**
   * Converts a Resource Identifier to a Resource Identifier with a specific version (tag and URI)
   * @param seriesId
   * @param versionTag
   * @return
   */
  static ResourceIdentifier toVersionId(ResourceIdentifier seriesId, String versionTag) {
    return ((ResourceIdentifier) seriesId.clone())
        .withVersionTag(versionTag)
        .withVersionId(getDefaultVersionId(seriesId.getResourceId(),versionTag));
  }

  /**
   * Converts a Resource Identifier to a Resource Identifier with a specific version (tag and URI)
   * and establish date
   * @param seriesId
   * @param versionTag
   * @param establishedOn
   * @return
   */
  static ResourceIdentifier toVersionId(ResourceIdentifier seriesId, String versionTag,
      Date establishedOn) {
    return toVersionId(seriesId,versionTag)
        .withEstablishedOn(establishedOn);
  }

  /**
   * Builds a pointer from a ResourceIdentifier
   *
   * @return A new Pointer derived from this ResourceIdentifier
   */
  default Pointer toPointer() {
    if (this instanceof org.omg.spec.api4kp._20200801.id.resources.Pointer) {
      return (Pointer) this;
    }
    Pointer ptr = new org.omg.spec.api4kp._20200801.id.resources.Pointer()
        .withNamespaceUri(getNamespaceUri())
        .withTag(getTag())
        .withVersionTag(getVersionTag())
        .withResourceId(getResourceId())
        .withUuid(getUuid())
        .withEstablishedOn(getEstablishedOn())
        .withVersionId(getVersionId())
        .withName(getName());
    if (this instanceof Pointer) {
      Pointer thisPtr = (Pointer) this;
      ptr.withHref(thisPtr.getHref());
      ptr.withMimeType(thisPtr.getMimeType());
    }
    return ptr;
  }

  /**
   * Builds a pointer from a ResourceIdentifier
   * <p>
   * Unlike other constructors, this method returns a base Pointer that is not an @XmlRootElement.
   * As such, it cannot be serialized directly, but can be used within serializable resources
   *
   * @return A new Pointer derived from this ResourceIdentifier
   */
  default Pointer toInnerPointer() {
    if (this instanceof Pointer) {
      return (Pointer) this;
    }
    var ptr = new Pointer()
        .withNamespaceUri(getNamespaceUri())
        .withTag(getTag())
        .withVersionTag(getVersionTag())
        .withVersionId(getVersionId())
        .withResourceId(getResourceId())
        .withUuid(getUuid())
        .withEstablishedOn(getEstablishedOn())
        .withName(getName());
    if (this instanceof Pointer) {
      Pointer thisPtr = (Pointer) this;
      ptr.withHref(thisPtr.getHref());
      ptr.withMimeType(thisPtr.getMimeType());
    }
    return ptr;
  }

  default Pointer toPointer(URI locator) {
    return toPointer()
        .withHref(locator);
  }

  default Pointer toPointer(URL locator) {
    Pointer ptr = toPointer();
    try {
      ptr.withHref(locator.toURI());
    } catch (URISyntaxException e) {
      logger.error(e.getMessage(), e);
    }
    return ptr;
  }

  static KeyIdentifier newKey(UUID seriesId, String versionTag) {
    return new ResourceIdentifier()
        .withUuid(seriesId)
        .withVersionTag(versionTag)
        .asKey();
  }

  default KeyIdentifier asKey() {
    final UUID id = getUuid();
    final int vid = getVersionTag() != null
        ? getVersionTag().hashCode()
        : VERSION_LATEST.hashCode();

    return new KeyIdentifier() {

      @Override
      public UUID getUuid() {
        return id;
      }

      @Override
      public int getVersionHash() {
        return vid;
      }

      @Override
      public String getVersionTag() {
        return SemanticIdentifier.this.getVersionTag();
      }

      @Override
      public int compareTo(KeyIdentifier other) {
        int byId = this.getUuid().compareTo(other.getUuid());
        if (byId != 0) {
          return byId;
        }

        Version v1 = VersionIdentifier.semVerOf(this.getVersionTag());
        Version v2 = VersionIdentifier.semVerOf(other.getVersionTag());
        if (v1 == null) {
          return v2 == null ? 0 : -1;
        }
        return v1.compareTo(v2);
      }

      public boolean equals(Object other) {
        if (other == null) {
          return false;
        }
        if (other instanceof KeyIdentifier) {
          KeyIdentifier otherkey = (KeyIdentifier) other;
          return this.getUuid().equals(otherkey.getUuid())
              && this.getVersionHash() == otherkey.getVersionHash();
        }
        if (other instanceof SemanticIdentifier) {
          SemanticIdentifier otherId = (SemanticIdentifier) other;
          return this.getUuid().equals(otherId.getUuid())
              && this.getVersionHash() == otherId.getVersionTag().hashCode();
        }
        return false;
      }

      public int hashCode() {
        int result = 31 + getUuid().hashCode();
        return 31 * result + getVersionHash();
      }

      public String toString() {
        return id + " # " + getVersionTag();
      }
    };
  }

  /**
   * Compose the ResourceId. By default will use namespace and tag. If tag is not provided, will use
   * UUID with namespace.
   *
   * @return resourceId URI
   */
  static URI toResourceId(String tag, URI namespace, UUID uuid) {
    String base = NameUtils.separatingName(namespace.toString());
    if (tag != null) {
      return URI.create(base + URLEncoder.encode(tag, Charset.defaultCharset()));
    } else if (uuid != null) {
      return Util.ensureUUID(uuid.toString())
          .map(uuid1 -> URI.create(base + uuid1.toString()))
          .orElseThrow(() -> new IllegalStateException("Invalid UUID Format"));
    } else {
      throw new IllegalStateException("No valid tag or uuid for ResourceId");
    }
  }

  /**
   * For the getResourceId methods that do not have a namespace provided, the namespace will default
   * the URN style.
   *
   * @return ResourceId
   */
  static URI toResourceId(String tag, UUID uuid) {
    return toResourceId(tag, DID_URN_URI, uuid);
  }

  static URI toResourceId(String tag) {
    return toResourceId(tag, DID_URN_URI, null);
  }

  static URI toResourceId(UUID uuid) {
    return toResourceId(null, DID_URN_URI, uuid);
  }

  /**
   * create resourceId given tag and namespace
   *
   * @param tag       the tag for the URI
   * @param namespace the namespace for the URI
   * @return resourceId URI
   */
  static URI toResourceId(String tag, URI namespace) {
    if (tag != null) {
      return toResourceId(tag, namespace, null);
    } else {
      throw new IllegalStateException("Tag required for ResourceId");
    }
  }

  static void checkTag(String tag) {
    if (Util.isEmpty(tag)) {
      throw new IllegalStateException("Missing required tag for Identifier");
    }
  }

  static void checkUUID(UUID uuid) {
    if (null == uuid) {
      throw new IllegalStateException("Missing required UUID for Identifier");
    }
  }


  static Comparator<SemanticIdentifier> mostRecentFirstComparator() {
    Comparator<SemanticIdentifier> c =
        Comparator.comparing(Identifier::getEstablishedOn);
    return c.reversed();
  }

  static Comparator<SemanticIdentifier> semVerComparator() {
    Comparator<SemanticIdentifier> c =
        Comparator.comparing(VersionIdentifier::getSemanticVersionTag);
    return c.reversed();
  }

  static Comparator<SemanticIdentifier> timedSemverComparator() {
    return (s1, s2) -> {
      int comp = semVerComparator().compare(s1, s2);
      if (comp == 0) {
        comp = mostRecentFirstComparator().compare(s1, s2);
      }
      return comp;
    };
  }

  default boolean sameAs(ResourceIdentifier other) {
    return other != null && this.asKey().equals(other.asKey());
  }

  default boolean sameAs(UUID otherId, String otherVersionTag) {
    return this.getUuid().equals(otherId) && this.getVersionTag().equals(otherVersionTag);
  }

  @JsonIgnore
  default UUID getVersionUuid() {
    long l1 = this.getUuid().getLeastSignificantBits();
    long l2 = this.getUuid().getMostSignificantBits();
    int v = (this.getVersionTag() != null)
        ? this.getVersionTag().hashCode()
        : VERSION_LATEST.hashCode();
    byte[] x = ByteBuffer.allocate(2 * Long.BYTES + Integer.BYTES)
        .putLong(l1).putLong(l2).putInt(v).array();
    return UUID.nameUUIDFromBytes(x);
  }

  static ResourceIdentifier hashIdentifiers(
      ResourceIdentifier id1, ResourceIdentifier id2, boolean asSnapshot) {
    UUID uuid = Util.hashUUID(id1.getUuid(), id2.getUuid());
    String vTag = asSnapshot ? VERSION_ZERO_SNAPSHOT : VERSION_ZERO;
    if (id1.getNamespaceUri().equals(id2.namespaceUri)) {
      return newId(id1.getNamespaceUri(), uuid, vTag);
    } else {
      return newId(uuid, vTag);
    }
  }

  static ResourceIdentifier hashIdentifiers(ResourceIdentifier id1, ResourceIdentifier id2) {
    return SemanticIdentifier.hashIdentifiers(id1, id2, false);
  }

}