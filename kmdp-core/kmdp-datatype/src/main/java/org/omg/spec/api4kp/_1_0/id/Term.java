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

import static org.omg.spec.api4kp._1_0.id.SemanticIdentifier.checkTag;

import com.github.zafarkhaja.semver.Version;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

// TODO: need Serializable?

/**
 * Terms are specialized identifiers that, in addition to denoting entities, evoke concepts.
 * Provides factory methods for generating ConceptIdentifier
 *
 */
public interface Term extends ScopedIdentifier, UniversalIdentifier, VersionIdentifier {

  URI getReferentId();

  /**
   * label is a display value
   *
   * @return
   */
  default String getLabel() {
    return getName();
  }

  /**
   * Create ConceptIdentifier for the UUID provided.
   * Will generate tag and resourceId as required values.
   *
   * @param uuid
   * @return ResourceIdentifier
   */
  static Term newId(UUID uuid) {
    SemanticIdentifier.checkUUID(uuid);
    // compose required resourceId from uuid
    URI resourceId = SemanticIdentifier.toResourceId(uuid);
    return new ConceptIdentifier()
        // generate required tag from uuid
        // set required fields
        .withTag(uuid.toString())
        .withResourceId(resourceId)
        .withUuid(uuid);
  }

  static Term newId(String tag, UUID uuid, URI namespace, URI referentId, String locale,
      String versionTag, String name, Date establishedDate) {
    return new ConceptIdentifier()
        .withUuid(uuid)
        .withReferentId(referentId)
        .withTag(tag)
        .withNamespace(namespace)
        .withLocale(locale)
        .withVersionTag(versionTag)
        .withResourceId(SemanticIdentifier.toResourceId(tag, namespace, uuid))
        .withName(name)
        .withEstablishedOn(establishedDate);
  }

  static Term newId(String tag) {
    checkTag(tag);
    URI resourceId = SemanticIdentifier.toResourceId(tag);
    return new ConceptIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  static Term newId(URI namespace, String tag) {
    checkTag(tag);
    URI resourceId = SemanticIdentifier.toResourceId(tag, namespace);
    return new ConceptIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        .withNamespace(namespace)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  static Term newId(URI namespace, String tag, Version version) {
    return newId(namespace, tag, version.toString());
  }

  static Term newId(URI namespace, String tag, String versionTag) {
    URI resourceId = SemanticIdentifier.toResourceId(tag, namespace);
    return new ConceptIdentifier()
        .withVersionTag(versionTag)
        // set required fields
        .withTag(tag)
        .withResourceId(resourceId)
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withNamespace(namespace);
  }

  static Term newId(String tag, Version version) {
    return newId(tag, version.toString());
  }

  static Term newId(String tag, String versionTag) {
    checkTag(tag);
    URI resourceId = SemanticIdentifier.toResourceId(tag);
    return new ConceptIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withVersionTag(versionTag);
  }

}
