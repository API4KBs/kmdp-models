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
package org.omg.spec.api4kp._20200801.id;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.getDefaultVersionId;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.SNOMED_BASE_URI;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.SNOMED_DATE;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.SNOMED_URI;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.SNOMED_VERSION;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.checkTag;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.toResourceId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.NameUtils;
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
public interface Term extends Identifiable, ScopedIdentifier, UniversalIdentifier, VersionIdentifier {

  URI getReferentId();

  @Override
  default Identifier getIdentifier() {
    return this;
  }

  @JsonIgnore
  default URI getEvokes() {
    return getResourceId();
  }

  @JsonIgnore
  default URI getDenotes() {
    return getReferentId();
  }

  /**
   * Compares two Terms based on their ID
   * (Used to compare implementations of the same term)
   * @param other
   * @return true if the two Terms object represent the same Term
   */
  default boolean sameTermAs(Term other) {
    return other != null
        && getUuid().equals(other.getUuid());
  }

  /**
   * Compares two Terms based on the ID of the concept they evoke
   * (Used to compare alternative expressions)
   * @param other
   * @return true if the two Terms evoke the same concept
   */
  default boolean evokesSameAs(Term other) {
    return other != null
        && getEvokes().equals(other.getEvokes());
  }

  /**
   * Compares two Terms based on the (ID of) the entity they denote
   * (Used to compare alternative conceptualizations)
   * @param other
   * @return true if the two Terms denote the same entity
   */
  default boolean isCoreferent(Term other) {
    return other != null
        && getDenotes().equals(other.getDenotes());
  }

  /**
   * Alias 'name' as 'label' for terms
   * @return
   */
  @JsonIgnore
  default String getLabel() {
    return getName();
  }

  /**
   * Alias 'resourceId' as 'conceptId' for terms
   * @return
   */
  @JsonIgnore
  default URI getConceptId() {
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
    URI resourceId = toResourceId(uuid);
    return new ConceptIdentifier()
        // generate required tag from uuid
        // set required fields
        .withTag(uuid.toString())
        .withResourceId(resourceId)
        .withUuid(uuid);
  }

  /**
   * Constructs a generic Term
   *
   * @param conceptId The URI assigned to the Concept
   *                  The conceptId is version invariant
   * @param tag An identifier, scoped by the concept scheme to which this concept belongs.
   *            The tag evokes the concept, and denotes the referent
   * @param uuid A universal identifier associated to the concept
   *             The uuid is version invariant
   * @param namespace The URI of the namespace / concept scheme that defines the concept
   * @param referentId The URI of the referent of the concept
   * @param versionTag The version of this concept
   *                   The version tag can be combined with the concept Id to create a concept version URI
   * @param name The primary label for the concept
   * @param establishedDate The date the concept was established (usually inherited from the date
   *                        of the release of the version of the concept scheme that first incluedd this concept)
   * @return a Term
   */
  static Term newTerm(URI conceptId, String tag, UUID uuid, URI namespace, URI referentId,
      String versionTag, String name, Date establishedDate) {
    URI uri = conceptId != null ? conceptId : toResourceId(tag, namespace, uuid);
    return new ConceptIdentifier()
        .withUuid(uuid)
        .withReferentId(referentId)
        .withTag(tag)
        .withNamespaceUri(namespace)
        .withVersionTag(versionTag)
        .withResourceId(uri)
        .withName(name)
        .withEstablishedOn(establishedDate)
        .withVersionId(getDefaultVersionId(uri,versionTag));
  }

  static Term newTerm(String tag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag);
    return new ConceptIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId));
  }

  static Term newTerm(URI conceptUri) {
    String tag = NameUtils.getTrailingPart(conceptUri.toString());
    URI namespace = URI.create(conceptUri.toString().replace(tag, ""));
    return newTerm(namespace, tag);
  }

  static Term newTerm(URI namespace, String tag) {
    return newTerm(namespace, tag, null, null);
  }

  static Term newTerm(URI namespace, String tag, String label) {
    return newTerm(namespace, tag, null, label);
  }

  static Term newTerm(URI namespace, String tag, Version version) {
    return newTerm(namespace, tag, version.toString(), null);
  }

  static Term newTerm(URI namespace, String tag, String versionTag, String label) {
    URI resourceId = toResourceId(tag, namespace);
    ConceptIdentifier cid = new ConceptIdentifier()
        // set required fields
        .withTag(tag)
        .withName(label)
        .withResourceId(resourceId)
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withNamespaceUri(namespace);
    if (!Util.isEmpty(versionTag)) {
      cid.withVersionTag(versionTag)
          .withVersionId(getDefaultVersionId(resourceId, versionTag));
    }
    return cid;
  }

  static Term newTerm(String tag, Version version) {
    return newTerm(tag, version.toString());
  }

  static Term newTerm(String tag, String versionTag) {
    checkTag(tag);
    URI resourceId = toResourceId(tag);
    return new ConceptIdentifier()
        .withTag(tag)
        .withResourceId(resourceId)
        // generate required UUID from resourceId
        .withUuid(UniversalIdentifier.toUUID(tag, resourceId))
        .withVersionTag(versionTag)
        .withVersionId(getDefaultVersionId(resourceId,versionTag));
  }

  /**
   * Instantiates a SNOMED-CT term
   * @param label
   * @param code
   * @return a Term from SNOMED
   */
  static Term sct(String label, String code) {
    String termUri = SNOMED_BASE_URI + code;
    return newTerm(
        URI.create(termUri)
        ,code,
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
    return newTerm(
        URI.create(termUri),
        code,
        Util.uuid(termUri),
        mock,
        URI.create(termUri),
        "0.0.0",
        label,
        DateTimeUtil.today());
  }

}
