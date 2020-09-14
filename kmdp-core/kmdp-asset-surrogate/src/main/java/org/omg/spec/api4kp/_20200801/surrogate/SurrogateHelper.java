/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omg.spec.api4kp._20200801.surrogate;

import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel._20200801.ParsingLevel.Concrete_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel._20200801.ParsingLevel.Knowledge_Expression;

import com.google.common.base.Functions;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.validation.Schema;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.Link;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.CompositeStructType;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.languagerole.KnowledgeRepresentationLanguageRole;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.VersionableTerm;

public class SurrogateHelper {

  protected SurrogateHelper() {

  }

  public static Optional<Schema> getSchema() {
    return XMLUtil.getSchemas(
        SurrogateHelper.class.getResource("/xsd/API4KP/surrogate/surrogate.xsd"),
        XMLUtil.catalogResolver("/xsd/km-surrogate-catalog.xml", "/xsd/terms-catalog.xml"));
  }


  public static Optional<ConceptIdentifier> getSimpleAnnotationValue(KnowledgeAsset asset,
      Term rel) {
    return asset.getAnnotation().stream()
        .filter(ann -> rel == null || rel.getUuid().equals(ann.getRel().getUuid()))
        .map(Annotation::getRef)
        .findAny();
  }


  public static SyntacticRepresentation canonicalRepresentationOf(KnowledgeAsset asset) {
    if (asset.getCarriers().isEmpty()) {
      return null;
    }
    KnowledgeArtifact artifact = asset.getCarriers().get(0);
    return artifact.getRepresentation();
  }

  public static Stream<SyntacticRepresentation> expandRepresentation(
      SyntacticRepresentation rep,
      KnowledgeRepresentationLanguageRole role) {
    if (rep == null) {
      return Stream.empty();
    }
    return Stream.concat(
        role == null ? Stream.of(rep) : Stream.empty(),
        rep.getSubLanguage() != null
            ? rep.getSubLanguage().stream()
            .filter(sub -> role == null || sub.getRole().asEnum() == role
                || sub.getRole().hasAncestor(role))
            .flatMap(sub -> expandRepresentation(sub, role))
            : Stream.empty());
  }

  public static Set<KnowledgeRepresentationLanguage> getSublanguages(SyntacticRepresentation rep,
      KnowledgeRepresentationLanguageRole role) {
    return expandRepresentation(rep, role)
        .map(SyntacticRepresentation::getLanguage)
        .collect(Collectors.toSet());
  }


  /**
   * Helper method that looks for the ComputableKnowledgeArtifact with a given ID and version
   * among the Artifacts referenced in a Canonical Knowledge Asset Surrogate
   * @param carrierId The ID of the Computable Artifact to retrieve
   * @param carrierVersionTag The version Tag of the Computable Artifact to retrieve
   * @param surr  The Surrogate of the Asset for which a Computable Manifestation is requested
   * @return  The Computable Variant of the asset with the given ID and version, if present
   */
  public static Optional<KnowledgeArtifact> getComputableCarrierMetadata(
      UUID carrierId, String carrierVersionTag, KnowledgeAsset surr) {
    return getInnerArtifact(carrierId,carrierVersionTag,surr,KnowledgeAsset::getCarriers);
  }

  /**
   * Helper method that looks for the ComputableKnowledgeArtifact with a given ID and version
   * among the Surrogates referenced in a Canonical Knowledge Asset Surrogate
   * @param surrogateId The ID of the Surrogate Metadata to retrieve
   * @param surrogateVersionTag The version Tag of the Surrogate Metadata to retrieve
   * @param surr  The Surrogate of the Asset for which a Computable Surrogate Metadata record is requested
   * @return  The Surrogate Metadata with the given ID and version, if present
   */
  public static Optional<KnowledgeArtifact> getComputableSurrogateMetadata(
      UUID surrogateId, String surrogateVersionTag, KnowledgeAsset surr) {
    return getInnerArtifact(surrogateId,surrogateVersionTag,surr,KnowledgeAsset::getSurrogate);
  }

  private static Optional<KnowledgeArtifact> getInnerArtifact(
      UUID artifactId, String artifactVersionTag, KnowledgeAsset surr,
      Function<KnowledgeAsset, List<KnowledgeArtifact>> mapper) {
    ResourceIdentifier aId = SurrogateBuilder.artifactId(artifactId,artifactVersionTag);
    return mapper.apply(surr).stream()
        .filter(a -> aId.sameAs(a.getArtifactId()))
        .findAny();
  }

  /**
   * Extracts the metadata self-descriptor of the the surrogate that matches the given language and format,
   * from the list of Surrogates registered in a Canonical Surrogate itself.
   * If more than one is found, returns the first
   * @param assetSurrogate The Surrogate to extract the metadata from
   * @param surrogateModel The representation language of the desired surrogate
   * @param surrogateFormat The representation format of the desired surrogate
   * @return The id of the Chosen Surrogate
   */
  public static Optional<KnowledgeArtifact> getSurrogateMetadata(KnowledgeAsset assetSurrogate,
      KnowledgeRepresentationLanguage surrogateModel, SerializationFormat surrogateFormat) {
    return assetSurrogate.getSurrogate().stream()
        .filter(cka -> surrogateModel.sameAs(cka.getRepresentation().getLanguage())
            && (surrogateFormat == null || surrogateFormat.sameAs(cka.getRepresentation().getFormat())))
        .findFirst();
  }

  /**
   * Extracts the ID of the matching surrogate
   * @param assetSurrogate The Surrogate to extract the Id from
   * @param surrogateModel The representation language of the desired surrogate
   * @param surrogateFormat The representation format of the desired surrogate
   * @return The id of the Surrogate it'self'
   */
  public static Optional<ResourceIdentifier> getSurrogateId(KnowledgeAsset assetSurrogate,
      KnowledgeRepresentationLanguage surrogateModel, SerializationFormat surrogateFormat) {
    return getSurrogateMetadata(assetSurrogate, surrogateModel, surrogateFormat)
        .map(KnowledgeArtifact::getArtifactId);
  }

  /**
   * Updates the ID of a surrogate, from the list of Surrogates registered
   * in a canonical surrogate itself.
   *
   * @param assetSurrogate The Surrogate to extract the Id from
   * @param surrogateModel The representation language of the desired surrogate
   * @param surrogateFormat The representation format of the desired surrogate
   * @param newSurrogateId The new Surrogate Id
   */
  public static ResourceIdentifier setSurrogateId(KnowledgeAsset assetSurrogate,
      KnowledgeRepresentationLanguage surrogateModel, SerializationFormat surrogateFormat,
      ResourceIdentifier newSurrogateId) {
    return assetSurrogate.getSurrogate().stream()
        .filter(cka -> surrogateModel.sameAs(cka.getRepresentation().getLanguage())
            && (surrogateFormat == null || surrogateFormat.sameAs(cka.getRepresentation().getFormat())))
        .findFirst()
        .map(cka -> cka.withArtifactId(newSurrogateId))
        .map(KnowledgeArtifact::getArtifactId)
        .orElse(null);
  }


  /**
   * Constructs a Composite Surrogate for a Composite Asset, instantiating a CompositeKnowledgeCarrier
   * whose components are Asset Surrogates
   *
   * @param rootAssetId The root component (if the Struct is TREE-based, null otherwise)
   * @param components The components, which must be KnowledgeAsset surrogates
   * @param structType The type of structure (supports SET and TREE)
   * @return a CompositeKnowledgeCarrier with wrapped KnowledgeAsset components
   */
  public static CompositeKnowledgeCarrier toCompositeAsset(
      ResourceIdentifier rootAssetId,
      Collection<KnowledgeAsset> components,
      CompositeStructType structType) {

    switch (structType) {
      case SET:
        return AbstractCarrier.ofIdentifiableSet(
            rep(Knowledge_Asset_Surrogate_2_0),
            KnowledgeAsset::getAssetId,
            ka -> SurrogateHelper.getSurrogateId(ka, Knowledge_Asset_Surrogate_2_0, JSON)
                .orElseGet(SurrogateBuilder::randomArtifactId),
            KnowledgeAsset::getName,
            components
        );
      case TREE:
        return AbstractCarrier.ofIdentifiableTree(
            rep(Knowledge_Asset_Surrogate_2_0),
            KnowledgeAsset::getAssetId,
            ka -> SurrogateHelper.getSurrogateId(ka, Knowledge_Asset_Surrogate_2_0, JSON)
                .orElseGet(SurrogateBuilder::randomArtifactId),
            KnowledgeAsset::getName,
            KnowledgeAsset::getLinks,
            rootAssetId,
            components.stream().collect(
                Collectors.toMap(
                    KnowledgeAsset::getAssetId,
                    Functions.identity()
                )
            )
        ).withRootId(rootAssetId);
      case GRAPH:
      default:
        throw new UnsupportedOperationException();
    }
  }

  /** Filter utility method that, given a KnowledgeAsset Surrogate,
   *  allows to retrieve the 'related' assets by relationship type
   *
   * @param surr the Surrogate to navigate
   * @param relClass the type of Link (dependency, component, etc)
   * @param relType an optional semantic type
   * @return a Stream of the (IDs of the) related Assets
   */
  public static Stream<SemanticIdentifier> links(
      KnowledgeAsset surr, Class<? extends Link> relClass, VersionableTerm<?,?> relType ) {
    return surr.getLinks().stream()
        .flatMap(StreamUtil.filterAs(relClass))
        .filter(rel -> relType == null || relType.sameAs(rel.getRel())
            || (rel.getRel() instanceof ConceptTerm && ((ConceptTerm<?>) rel.getRel()).hasAncestor(relType))
        )
        .map(Link::getHref);
  }

  /**
   * Converts a KnowledgeAsset Surrogate (KA) to a KnowledgeCarrier (KC)
   * Both structures contain metadata about a Knowledge Artifact:
   * KA is designed to be used 'at rest', while KC is optimized for processing.
   *
   * This function initializes a KC using the subset of relevant metadata contained in a KA,
   * using the 'first' referenced carrier Artifact if present.
   *
   * @param surrogate The source Knowledge Asset Surrogate
   * @return A KnowledgeCarrier that preserves the relevant metadata
   */
  public static KnowledgeCarrier toRuntimeSurrogate(KnowledgeAsset surrogate) {
    Optional<KnowledgeArtifact> canonicalArtifact
        = surrogate.getCarriers().stream().findFirst();
    String inlined = canonicalArtifact.map(KnowledgeArtifact::getInlinedExpression).orElse(null);

    return new KnowledgeCarrier()
        .withAssetId(surrogate.getAssetId())
        .withArtifactId(canonicalArtifact.map(KnowledgeArtifact::getArtifactId).orElse(null))
        .withExpression(inlined)
        .withLabel(surrogate.getName())
        .withLevel(inlined != null ? Concrete_Knowledge_Expression : Knowledge_Expression)
        .withRepresentation(canonicalArtifact.map(KnowledgeArtifact::getRepresentation).orElse(null))
        .withHref(canonicalArtifact.map(KnowledgeArtifact::getLocator).orElse(null));
  }
}