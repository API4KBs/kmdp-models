package org.omg.spec.api4kp._1_0.id;

import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN_URI;

import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

/**
 * Factory class for generating ResourceIdentifiers. resourceIdentifier will be composed of parts
 * provided uuid or tag must be provided at a minimum
 *
 * •	an explicit uuid can be the tag, if no other tag is provided
 * •	resourceId can be constructed from either tag or uuid, plus the namespace/base uri
 * •	if a uuid is not provided explicitly, we can use UUID.namedUUIDfromBytes().
 *    using resourceId.toString.getBytes since resourceId is a URI
 *    TODO: should allow for resourceId to be provided? Or always construct?
 *    TODO: Since namespace is defaulted when not provided, also set on the Identifier?
 *
 */
public interface SemanticIdentifier extends VersionIdentifier, ScopedIdentifier,
    UniversalIdentifier {

  /**
   * Create ResourceIdentifier for the UUID provided. Will generate tag and resourceId as required
   * values.
   *
   * @return ResourceIdentifier
   */
  static SemanticIdentifier newId(UUID uuid) {
    checkUUID(uuid);
    // compose required resourceId from uuid
    URI resourceId = toResourceId(uuid);
    return new ResourceIdentifier()
        // generate required tag from uuid
        // set required fields
        .withTag(uuid.toString())
        .withResourceId(resourceId)
        .withUuid(uuid);
  }

  /**
   * Generate ResourceIdentifier from namespace, tag and version. Required UUID will be generated
   * from composed resourceId
   *
   * @return ResourceIdentifier
   */
  static SemanticIdentifier newId(URI namespace, String tag, Version version) {
    return newId(namespace, tag, version.toString());
  }

  static SemanticIdentifier newId(URI namespace, String tag, String versionTag) {
    URI resourceId = toResourceId(tag, namespace);
    return new ResourceIdentifier()
        .withVersionTag(versionTag)
        // set required fields
        .withTag(tag)
        .withResourceId(resourceId)
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withNamespaceUri(namespace);
  }

  /**
   * Create ResourceIdentifier from namespace, UUID, version and name. Required tag will be
   * generated from UUID.
   *
   * @param namespace namespace used to compose the resourceId
   * @param uuid used to compose resourceId and create required tag value
   * @param version the version of the resource
   * @param name the name for the resource
   * @return ResourceIdentifier with appropriate values set
   */
  static SemanticIdentifier newId(URI namespace, UUID uuid, Version version, String name) {
    return newId(namespace, uuid, version.toString(), name);
  }

  static SemanticIdentifier newId(URI namespace, UUID uuid, String versionTag, String name) {
    checkUUID(uuid);
    // create URI id
    return new ResourceIdentifier()
        .withVersionTag(versionTag)
        // set required fields
        .withTag(uuid.toString())
        .withResourceId(toResourceId(uuid.toString(), namespace, uuid))
        .withUuid(uuid)
        .withNamespaceUri(namespace)
        .withName(name);
  }

  /**
   * Generate ResourceIdentifier from uuid, version and name
   * Will generate required tag from uuid
   * Will default namespace
   *
   * @return ResourceIdentifier
   */
  static SemanticIdentifier newId(UUID uuid, Version version, String name) {
    return newId(uuid, version.toString(), name);
  }

  static SemanticIdentifier newId(UUID uuid, String versionTag, String name) {
    checkUUID(uuid);
    // create URN id
    return new ResourceIdentifier()
        .withUuid(uuid)
        // generate required tag from UUID
        .withTag(uuid.toString())
        .withResourceId(toResourceId(uuid.toString(), uuid))
        .withVersionTag(versionTag)
        .withName(name);
  }

  /**
   * compose ResourceIdentifier from the given parts
   *
   * @return ResourceIdentifier
   */
  static SemanticIdentifier newId(URI namespace, String tag, UUID uuid, Version version,
      String name) {
    return newId(namespace, tag, uuid, version.toString(), name);
  }

  static SemanticIdentifier newId(URI namespace, String tag, UUID uuid, String versionTag, String name) {
    checkUUID(uuid);
    checkTag(tag);
    return new ResourceIdentifier()
        .withUuid(uuid)
        .withTag(tag)
        .withVersionTag(versionTag)
        .withNamespaceUri(namespace)
        .withName(name)
        .withResourceId(toResourceId(tag, namespace, uuid));
  }

  /**
   * Generate ResourceIdentifier with all the attributes
   *
   * @return ResourceIdentifier
   */
  static SemanticIdentifier newId(URI namespace, String tag, UUID uuid, Version version,
      String name, Date establishedOn) {
    return newId(namespace, tag, uuid, version.toString(), name, establishedOn);
  }

  static SemanticIdentifier newId(URI namespace, String tag, UUID uuid, String versionTag, String name, Date establishedOn) {
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
  static SemanticIdentifier newId(URI namespace, String tag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag, namespace);
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        .withNamespaceUri(namespace)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  /**
   * Generate resourceIdentifier for tag Will default to URN will generate uuid from resourceId
   *
   * @return ResourceIdentifier with required values set
   */
  static SemanticIdentifier newId(String tag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag);
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  /**
   * Create a ResourceIdentifier from the parts given. ResourceId will be composed of default URN
   * URI and tag UUID will be generated from resourceId
   *
   * @return ResourceIdentifier with all required attributes set
   */
  static SemanticIdentifier newId(String tag, Version version) {
    return newId(tag, version.toString());
  }

  static SemanticIdentifier newId(String tag, String versionTag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag);
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withVersionTag(versionTag);
  }

  /**
   * Pointer for client/server interaction.
   *
   * @param namespace
   * @param tag
   * @return Pointer with required resourceId, tag and UUID set
   */
  static SemanticIdentifier newIdAsPointer(URI namespace, String tag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag, namespace);
    return new Pointer()
        .withNamespaceUri(namespace)
        .withTag(tag)
        .withResourceId(resourceId)
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  /**
   * Pointer for client/server interaction.
   *
   * @param namespace resource namespace
   * @param tag resource tag
   * @param description human readable description of the resource
   * @param locator use for cases when the resource URI cannot be dereferenced or the server wants
   * to point the client to a specific location where the denoted resource is available
   * @return Pointer with required resourceid, tag, UUID and all other values provided set
   */
  static SemanticIdentifier newIdAsPointer(URI namespace, String tag, String description, URI locator) {
    checkTag(tag);
    URI resourceId = toResourceId(tag, namespace);
    return new Pointer()
        .withNamespaceUri(namespace)
        .withTag(tag)
        .withResourceId(resourceId)
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withDescription(description)
        .withHref(locator);
  }

  /**
   * Compose the ResourceId. By default will use namespace and tag. If tag is not provided, will use
   * UUID with namespace.
   *
   * @return resourceId URI
   */
  static URI toResourceId(String tag, URI namespace, UUID uuid) {
    if (tag != null) {
      return URI.create(namespace + tag);
    } else if (uuid != null) {
      return Util.ensureUUID(uuid.toString())
          .map(uuid1 -> URI.create(namespace + uuid1.toString()))
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
   * @param tag the tag for the URI
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

}
