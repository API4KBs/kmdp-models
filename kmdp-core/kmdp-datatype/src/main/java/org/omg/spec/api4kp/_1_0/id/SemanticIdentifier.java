package org.omg.spec.api4kp._1_0.id;

import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN_URI;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.VERSIONS_RX;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.VERSION_LATEST;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Date;
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
    URI nsUri = URI.create(uriStr.substring(0, uriStr.lastIndexOf(tag)));
    if (! "urn".equals(nsUri.getScheme()) && nsUri.getAuthority() == null) {
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
  static ResourceIdentifier newNamespaceId(URI uri) {
    String tag = URIUtil.detectLocalName(uri);
    return new ResourceIdentifier()
        // generate required tag from uuid
        // set required fields
        .withTag(tag)
        .withNamespaceUri(uri)
        .withResourceId(uri)
        .withEstablishedOn(DateTimeUtil.today())
        .withUuid(Util.isUUID(tag)
            ? UUID.fromString(tag)
            : Util.uuid(tag));
  }


  /**
   * Generate resourceIdentifier for URI that includes
   * version information according to known patterns
   *
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newVersionId(URI versionUri) {
    return newVersionId(versionUri, VERSIONS_RX, 3, 2);
  }

  /**
   * Generate resourceIdentifier from a series URI and a version Tag
   *
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newVersionId(URI seriesUri, String versionTag) {
    return newVersionId(URI.create(seriesUri.toString() + IdentifierConstants.VERSIONS + versionTag));
  }

  /**
   * Generate resourceIdentifier for URI that includes
   * version information according to a user-specified Pattern
   *
   * The Pattern can specify two groups (base URI + version)
   * or three groups (base URI + tag + version)
   *
   * @param versionUri the URI that contains version information
   * @param versionPattern the Pattern that allows to deconstruct the URI
   * @param versionGroupIdx the index of the pattern group that contains the version information
   * @param tagGroupIdx the index of the pattern group that contains the base and/or tag information
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newVersionId(URI versionUri, Pattern versionPattern, int versionGroupIdx, int tagGroupIdx) {
    if (versionUri.getScheme().equals("urn")) {
      // assume pattern urn:uuid:tag:version
      String str = versionUri.toString();
      StringTokenizer tok = new StringTokenizer(str, ":");
      if (tok.countTokens() >= 4) {
        int idx = str.lastIndexOf(':');
        return newId(URI.create(str.substring(0, idx)))
            .withVersionTag(str.substring(idx + 1));
      }
    }
    Matcher m = versionPattern.matcher(versionUri.toString());
    if (m.matches()) {
      switch (m.groupCount()) {
        case 2:
          return newId(URI.create(m.group(tagGroupIdx)))
              .withVersionTag(m.group(versionGroupIdx));
        case 3:
          return newId(URI.create(m.group(1)), m.group(tagGroupIdx))
              .withVersionTag(m.group(versionGroupIdx));
        default:
      }
    }
    logger.warn("Unable to detect version information from URI : {}", versionUri);
    return newId(versionUri);
  }

  /**
   * Generate resourceIdentifier for URI that includes
   * version information according to a user-specified Pattern
   * Further assumes that the tag is date-based, and can be parsed
   * as a date using a given pattern. The date will be used
   * as the identifier's establishing date
   *
   * @param versionUri the URI that contains version information
   * @param versionPattern the Pattern that allows to deconstruct the URI
   * @param versionGroupIdx the index of the pattern group that contains the version information
   * @param tagGroupIdx the index of the pattern group that contains the base and/or tag information
   * @param datePattern the pattern used to parse the tag into the establishedDate
   * @return ResourceIdentifier with required values set
   */
  static ResourceIdentifier newDateVersionId(URI versionUri, Pattern versionPattern, int versionGroupIdx, int tagGroupIdx, Pattern datePattern) {
    ResourceIdentifier id = newVersionId(versionUri,versionPattern,versionGroupIdx,tagGroupIdx);
    Matcher m = datePattern.matcher(id.getTag());
    if (m.matches()) {
      id.withEstablishedOn(DateTimeUtil.parseDate(id.getTag(),datePattern.pattern()));
    }
    return id;
  }

  /**
   * Create ResourceIdentifier for the UUID provided. Will generate tag and resourceId as required
   * values.
   *
   * @return ResourceIdentifier
   */
  static ResourceIdentifier newId(URI base, UUID uuid) {
    checkUUID(uuid);
    return newId(toResourceId(uuid.toString(),base,uuid));
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
    return newId(BASE_UUID_URN_URI, tag.toString(), version.toString());
  }

  static ResourceIdentifier newId(UUID tag, String version) {
    return newId(BASE_UUID_URN_URI, tag.toString(), version);
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
        .withNamespaceUri(namespace);
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
    return new ResourceIdentifier()
        .withVersionTag(versionTag)
        // set required fields
        .withTag(uuid.toString())
        .withResourceId(toResourceId(uuid.toString(), namespace, uuid))
        .withUuid(uuid)
        .withNamespaceUri(namespace)
        .withEstablishedOn(DateTimeUtil.today())
        .withName(name);
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
    return new ResourceIdentifier()
        .withUuid(uuid)
        // generate required tag from UUID
        .withTag(uuid.toString())
        .withResourceId(toResourceId(uuid.toString(), uuid))
        .withVersionTag(versionTag)
        .withEstablishedOn(DateTimeUtil.today())
        .withName(name);
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
    return new ResourceIdentifier()
        .withUuid(uuid)
        .withTag(tag)
        .withVersionTag(versionTag)
        .withNamespaceUri(namespace)
        .withName(name)
        .withEstablishedOn(DateTimeUtil.today())
        .withResourceId(toResourceId(tag, namespace, uuid));
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
    return new ResourceIdentifier()
        .withUuid(uuid)
        .withTag(tag)
        .withVersionTag(versionTag)
        .withNamespaceUri(namespace)
        .withName(name)
        .withResourceId(toResourceId(tag, namespace, uuid))
        .withEstablishedOn(establishedOn);
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
        .withVersionTag(versionTag);
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
    return new org.omg.spec.api4kp._1_0.id.resources.Pointer()
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
    URI resourceId = toResourceId(tag.toString(), BASE_UUID_URN_URI);
    return new org.omg.spec.api4kp._1_0.id.resources.Pointer()
        .withNamespaceUri(resourceId)
        .withTag(tag.toString())
        .withVersionTag(versionTag)
        .withResourceId(resourceId)
        .withEstablishedOn(DateTimeUtil.today())
        .withUuid(tag);
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
    return new org.omg.spec.api4kp._1_0.id.resources.Pointer()
        .withNamespaceUri(namespace)
        .withTag(tag)
        .withVersionTag(versionTag)
        .withResourceId(resourceId)
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withDescription(description)
        .withEstablishedOn(DateTimeUtil.today())
        .withHref(locator);
  }

  static Pointer newIdAsPointer(URI namespace, String tag, String name,
      String versionTag, String description, URI entityType, URI locator) {
    checkTag(tag);
    URI resourceId = toResourceId(tag, namespace);
    return new org.omg.spec.api4kp._1_0.id.resources.Pointer()
        .withNamespaceUri(namespace)
        .withType(entityType)
        .withTag(tag)
        .withName(name)
        .withVersionTag(versionTag)
        .withResourceId(resourceId)
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withDescription(description)
        .withEstablishedOn(DateTimeUtil.today())
        .withHref(locator);
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
    return new org.omg.spec.api4kp._1_0.id.resources.Pointer()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  static ResourceIdentifier randomId() {
    return newId(BASE_UUID_URN_URI,UUID.randomUUID(),IdentifierConstants.VERSION_LATEST);
  }

  /**
   * Builds a pointer from a ResourceIdentifier
   *
   * @return A new Pointer derived from this ResourceIdentifier
   */
  default Pointer toPointer() {
    return new org.omg.spec.api4kp._1_0.id.resources.Pointer()
        .withNamespaceUri(getNamespaceUri())
        .withTag(getTag())
        .withVersionTag(getVersionTag())
        .withResourceId(getResourceId())
        .withUuid(getUuid())
        .withEstablishedOn(getEstablishedOn());
  }

  /**
   * Builds a pointer from a ResourceIdentifier
   *
   * Unlike other constructors, this method returns a base Pointer
   * that is not an @XmlRootElement. As such, it cannot be serialized
   * directly, but can be used within serializable resources
   * @return A new Pointer derived from this ResourceIdentifier
   */
  default Pointer toInnerPointer() {
    return new Pointer()
        .withNamespaceUri(getNamespaceUri())
        .withTag(getTag())
        .withVersionTag(getVersionTag())
        .withResourceId(getResourceId())
        .withUuid(getUuid())
        .withEstablishedOn(getEstablishedOn());
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

  default KeyIdentifier asKey() {
    final UUID id = getUuid();
    final int vid = getVersionTag().hashCode();

    return new KeyIdentifier() {
      @Override
      public UUID getUuid() {
        return id;
      }

      @Override
      public int getVersionHash() {
        return vid;
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
      return URI.create(base + tag);
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
    return toResourceId(tag, BASE_UUID_URN_URI, uuid);
  }

  static URI toResourceId(String tag) {
    return toResourceId(tag, BASE_UUID_URN_URI, null);
  }

  static URI toResourceId(UUID uuid) {
    return toResourceId(null, BASE_UUID_URN_URI, uuid);
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
    byte[] x = ByteBuffer.allocate(2*Long.BYTES+Integer.BYTES)
        .putLong(l1).putLong(l2).putInt(v).array();
    return UUID.nameUUIDFromBytes(x);
  }

  static ResourceIdentifier hashIdentifiers(ResourceIdentifier id1, ResourceIdentifier id2) {
    UUID uuid = Util.hashUUID(id1.getUuid(), id2.getUuid());
    String vTag = IdentifierConstants.VERSION_ZERO + "+" + Util.hashString(id1.getVersionTag(),id2.getVersionTag());
    if (id1.getNamespaceUri().equals(id2.namespaceUri)) {
      return newId(id1.getNamespaceUri(),uuid,vTag);
    } else {
      return newId(uuid,vTag);
    }
  }

}
