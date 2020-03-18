package org.omg.spec.api4kp._1_0.id;

import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN_URI;

import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

/**
 * Factory class for generating ResourceIdentifiers.
 * resourceIdentifier will be composed of parts provided
 * uuid or tag must be provided at a minimum
 *
 * •	an explicit uuid can be the tag, if no other tag is provided
 * •	resourceId can be constructed from either tag or uuid, plus the namespace/base uri
 * •	if a uuid is not provided explicitly,  we can use UUID.namedUUIDfromBytes().
 *    using resourceId.toString.getBytes since resourceId is a URI
 */
public interface SemanticIdentifier extends VersionIdentifier, ScopedIdentifier, UniversalIdentifier {

  /**
   * Create ResourceIdentifier for the UUID provided.
   * Will generate tag and resourceId as required values.
   *
   * @param uuid
   * @return ResourceIdentifier
   */
  static SemanticIdentifier newId(UUID uuid) {
    checkUUID(uuid);
    // compose required resourceId from uuid
    URI resourceId = getResourceId(uuid);
    return new ResourceIdentifier()
        // generate required tag from uuid
        // set required fields
        .withTag(uuid.toString())
        .withResourceId(resourceId)
        .withUuid(uuid);
  }

  /**
   * Generate ResourceIdentifier from namespace, tag and version.
   * Required UUID will be generated from composed resourceId
   *
   * @param namespace
   * @param tag
   * @param version
   * @return ResourceIdentifier
   */
  static SemanticIdentifier newId(URI namespace, String tag, Version version) {
    URI resourceId = getResourceId(tag, namespace);
    return new ResourceIdentifier()
        .withVersionTag(version.toString())
        // set required fields
        .withTag(tag)
        .withResourceId(resourceId)
        .withUuid(UUID.nameUUIDFromBytes(resourceId.toString().getBytes()))
        .withNamespace(namespace);
  }

  /**
   * Create ResourceIdentifier from namespace, UUID, version and name.
   * Required tag will be generated from UUID.
   *
   * @param namespace namespace used to compose the resourceId
   * @param uuid used to compose resourceId and create required tag value
   * @param version the version of the resource
   * @param name the name for the resource
   * @return ResourceIdentifier with appropriate values set
   */
  static SemanticIdentifier newId(URI namespace, UUID uuid, Version version, String name) {
    checkUUID(uuid);
    // create URI id
    return new ResourceIdentifier()
        .withVersionTag(version.toString())
        // set required fields
        .withTag(uuid.toString())
        .withResourceId(getResourceId(uuid.toString(), namespace, uuid))
        .withUuid(uuid)
        .withNamespace(namespace)
        .withName(name);
  }


  /**
   * Generate ResourceIdentifier from uuid, version and name
   * Will generate required tag from uuid
   * Will default namespace
   *
   * @param uuid
   * @param version
   * @param name
   * @return ResourceIdentifier
   */
  static SemanticIdentifier newId(UUID uuid, Version version, String name) {
    checkUUID(uuid);
    // create URN id
    return new ResourceIdentifier()
        .withUuid(uuid)
        // generate required tag from UUID
        .withTag(uuid.toString())
        .withResourceId(getResourceId(uuid.toString(), uuid))
        .withVersionTag(version.toString())
        .withName(name);
  }

  /**
   * compose ResourceIdentifier from the given parts
   *
   * @param namespace
   * @param tag
   * @param uuid
   * @param version
   * @param name
   * @return ResourceIdentifier
   */
  static SemanticIdentifier newId(URI namespace, String tag, UUID uuid, Version version, String name) {
    checkUUID(uuid);
    if(null == tag) {
      // default to uuid or error?
      tag = uuid.toString();
  }
    return new ResourceIdentifier()
        .withUuid(uuid)
        .withTag(tag)
        .withNamespace(namespace)
        .withVersionTag(version.toString())
        .withName(name)
        .withResourceId(getResourceId(tag, namespace, uuid));
  }

  /**
   * Generate ResourceIdentifier with all the attributes
   *
   * @param namespace
   * @param tag
   * @param uuid
   * @param version
   * @param name
   * @param establishedOn
   * @return ResourceIdentifier
   */
  static SemanticIdentifier newId(URI namespace, String tag, UUID uuid, Version version, String name, Date establishedOn) {
    checkUUID(uuid);
    if(null == tag) {
      // default to uuid
      tag = uuid.toString();
    }
    return new ResourceIdentifier()
        .withUuid(uuid)
        .withTag(tag)
        .withNamespace(namespace)
        .withVersionTag(version.toString())
        .withName(name)
        .withResourceId(getResourceId(tag, namespace, uuid))
        .withEstablishedOn(establishedOn);
  }

  /**
   * Compose ResourceIdentifier from provided parts
   * UUID will be generated from resourceIdentifier
   *
   * @param namespace
   * @param tag
   * @return ResourceIdentifier with required values
   */
  static SemanticIdentifier newId(URI namespace, String tag) {
    checkTag(tag);
    URI resourceId = getResourceId(tag, namespace);
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        .withNamespace(namespace)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.getUUID(tag, resourceId));
    }

  /**
   * Generate resourceIdentifier for tag
   * Will default to URN
   * will generate uuid from resourceId
   *
   * @param tag
   * @return ResourceIdentifier with required values set
   */
  static SemanticIdentifier newId(String tag) {
    checkTag(tag);
    URI resourceId = getResourceId(tag);
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.getUUID(tag, resourceId));
  }

  /**
   * Create a ResourceIdentifier from the parts given.
   * ResourceId will be composed of default URN URI and tag
   * UUID will be generated from resourceId
   *
   * @param tag
   * @param version
   * @return ResourceIdentifier with all required attributes set
   */
  static SemanticIdentifier newId(String tag, Version version) {
    checkTag(tag);
    URI resourceId = getResourceId(tag);
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.getUUID(tag, resourceId))
        .withVersionTag(version.toString());
  }

  static SemanticIdentifier newId(String tag, String versionTag) {
    checkTag(tag);
    URI resourceId = getResourceId(tag);
    return new ResourceIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.getUUID(tag, resourceId))
        .withVersionTag(versionTag);
  }

  /**
   * Compose the ResourceId.
   * By default will use namespace and tag.
   * If tag is not provided, will use UUID with namespace.
   *
   * @param tag
   * @param namespace
   * @param uuid
   * @return resourceId URI
   */
  static URI getResourceId(String tag, URI namespace, UUID uuid) {
    if(tag != null) {
      return URI.create(namespace + tag);
    } else if(uuid != null) {
      return Util.ensureUUID(uuid.toString())
          .map(uuid1 -> URI.create(namespace + uuid1.toString()))
          .orElseThrow(() -> new IllegalStateException("Invalid UUID Format"));
    } else {
      throw new IllegalStateException("No valid tag or uuid for ResourceId");
    }
  }

  /**
   * For the getResourceId methods that do not have a namespace provided,
   * the namespace will default the URN style.
   *
   * @param tag
   * @param uuid
   * @return ResourceId
   */
  static URI getResourceId(String tag, UUID uuid) {
    return getResourceId(tag, BASE_UUID_URN_URI, uuid);
  }

  static URI getResourceId(String tag) {
    return getResourceId(tag, BASE_UUID_URN_URI, null);
  }

  static URI getResourceId(UUID uuid) {
    return getResourceId(null, BASE_UUID_URN_URI, uuid);
  }

  /**
   * create resourceId given tag and namespace
   *
   * @param tag the tag for the URI
   * @param namespace the namespace for the URI
   * @return resourceId URI
   */
  static URI getResourceId(String tag, URI namespace) {
    if(tag != null) {
      return getResourceId(tag, namespace, null);
    } else {
      throw new IllegalStateException("Tag required for ResourceId");
    }
  }

  static void checkTag(String tag) {
    if(Util.isEmpty(tag)) {
      throw new IllegalStateException("Missing required tag for Identifier");
    }
  }

  static void checkUUID(UUID uuid) {
    if(Util.isEmpty(uuid.toString()) ||
    !Util.isUUID(uuid.toString())) {
      throw new IllegalStateException("Missing required UUID for Identifier");
    }
  }

}
