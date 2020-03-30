/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.omg.spec.api4kp._1_0.id;

import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.SNOMED_BASE_URI;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.SNOMED_DATE;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.SNOMED_URI;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.SNOMED_VERSION;
import static org.omg.spec.api4kp._1_0.id.SemanticIdentifier.checkTag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.Util;
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

  @JsonIgnore
  default URI getEvokes() {
    return getResourceId();
  }

  /**
   * label is a display value
   *
   * @return
   */
  @JsonIgnore
  default String getPrefLabel() {
    return getName();
  }

  default ConceptIdentifier asConceptIdentifier() {
    return (ConceptIdentifier) this;
  }

  /**
   * Create ConceptIdentifier for the UUID provided.
   * Will generate tag and resourceId as required values.
   *
   * @param uuid
   * @return ResourceIdentifier
   */
  static Term newTerm(UUID uuid) {
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

  static Term newTerm(String tag, UUID uuid, URI namespace, URI referentId,
      String versionTag, String name, Date establishedDate) {
    return new ConceptIdentifier()
        .withUuid(uuid)
        .withReferentId(referentId)
        .withTag(tag)
        .withNamespaceUri(namespace)
        .withVersionTag(versionTag)
        .withResourceId(SemanticIdentifier.toResourceId(tag, namespace, uuid))
        .withName(name)
        .withEstablishedOn(establishedDate);
  }

  static Term newTerm(String tag) {
    checkTag(tag);
    URI resourceId = SemanticIdentifier.toResourceId(tag);
    return new ConceptIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  static Term newTerm(URI namespace, String tag) {
    checkTag(tag);
    URI resourceId = SemanticIdentifier.toResourceId(tag, namespace);
    return new ConceptIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        .withNamespaceUri(namespace)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  static Term newTerm(URI namespace, String tag, Version version) {
    return newTerm(namespace, tag, version.toString());
  }

  static Term newTerm(URI namespace, String tag, String versionTag) {
    URI resourceId = SemanticIdentifier.toResourceId(tag, namespace);
    return new ConceptIdentifier()
        .withVersionTag(versionTag)
        // set required fields
        .withTag(tag)
        .withResourceId(resourceId)
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withNamespaceUri(namespace);
  }

  static Term newTerm(String tag, Version version) {
    return newTerm(tag, version.toString());
  }

  static Term newTerm(String tag, String versionTag) {
    checkTag(tag);
    URI resourceId = SemanticIdentifier.toResourceId(tag);
    return new ConceptIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withVersionTag(versionTag);
  }

  /**
   * Instantiates a SNOMED-CT term
   * @param label
   * @param code
   * @return a Term from SNOMED
   */
  static Term sct(String label, String code) {
    String termUri = SNOMED_BASE_URI + code;
    return newTerm(code,
        Util.uuid(termUri),
        SNOMED_URI,
        URI.create(termUri),
        SNOMED_VERSION,
        label,
        SNOMED_DATE);
  }

  /**
   * Instantiates a mock test local term
   * @param label
   * @param code
   * @return a Test Term from a fictitious concept scheme for testing purposes
   */
  static Term mock(String label, String code) {
    URI mock = URI.create("http://ontology.mock.edu/mock/");
    String termUri = mock + code;
    return newTerm(code,
        Util.uuid(termUri),
        mock,
        URI.create(termUri),
        "0.0.0",
        label,
        DateTimeUtil.now());
  }

}
