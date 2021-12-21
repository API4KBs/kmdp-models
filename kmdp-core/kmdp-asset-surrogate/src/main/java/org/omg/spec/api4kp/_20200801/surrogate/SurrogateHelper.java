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

import static edu.mayo.kmdp.util.CatalogBasedURIResolver.catalogResolver;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.inferStruct;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.ofUniformAggregate;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.ofUniformAnonymousComposite;
import static org.omg.spec.api4kp._20200801.AbstractCompositeCarrier.ofUniformNamedComposite;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newId;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.randomId;
import static org.omg.spec.api4kp._20200801.services.CompositeStructType.GRAPH;
import static org.omg.spec.api4kp._20200801.surrogate.SurrogateBuilder.artifactId;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate_2_0;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartTypeSeries.Has_Structural_Component;

import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.validation.Schema;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
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
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevel;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;

public class SurrogateHelper {

  protected SurrogateHelper() {

  }

  public static Optional<Schema> getSchema() {
    return XMLUtil.getSchemas(
        SurrogateHelper.class.getResource("/xsd/API4KP/surrogate/surrogate.xsd"),
        catalogResolver("/xsd/km-surrogate-catalog.xml", "/xsd/terms-catalog.xml"));
  }


  public static Optional<ConceptIdentifier> getSimpleAnnotationValue(
      KnowledgeAsset asset, Term rel) {
    if (asset == null) {
      return Optional.empty();
    }
    return asset.getAnnotation().stream()
        .filter(ann -> rel == null || rel.sameTermAs(ann.getRel()))
        .map(Annotation::getRef)
        .findAny();
  }

  public static Collection<ConceptIdentifier> getAnnotationValues(
      KnowledgeAsset asset, Term rel) {
    if (asset == null) {
      return Collections.emptySet();
    }
    return asset.getAnnotation().stream()
        .filter(ann -> rel == null || rel.sameTermAs(ann.getRel()))
        .map(Annotation::getRef)
        .collect(Collectors.toSet());
  }


  public static SyntacticRepresentation canonicalRepresentationOf(KnowledgeAsset asset) {
    if (asset == null || asset.getCarriers().isEmpty()) {
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
            .filter(sub -> role == null || role.sameAs(sub.getRole())
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
   * @return The Computable Variant of the asset with the given ID and version, if present
   */
  public static Optional<KnowledgeArtifact> getComputableCarrierMetadata(
      UUID carrierId, String carrierVersionTag, KnowledgeAsset surr) {
    return getInnerArtifact(carrierId, carrierVersionTag, surr, KnowledgeAsset::getCarriers);
  }

  /**
   * Helper method that looks for the ComputableKnowledgeArtifact with a given ID and version
   * among the Surrogates referenced in a Canonical Knowledge Asset Surrogate
   * @param surrogateId The ID of the Surrogate Metadata to retrieve
   * @param surrogateVersionTag The version Tag of the Surrogate Metadata to retrieve
   * @param surr  The Surrogate of the Asset for which a Computable Surrogate Metadata record is requested
   * @return The Surrogate Metadata with the given ID and version, if present
   */
  public static Optional<KnowledgeArtifact> getComputableSurrogateMetadata(
      UUID surrogateId, String surrogateVersionTag, KnowledgeAsset surr) {
    return getInnerArtifact(surrogateId, surrogateVersionTag, surr, KnowledgeAsset::getSurrogate);
  }

  private static Optional<KnowledgeArtifact> getInnerArtifact(
      UUID artifactId, String artifactVersionTag, KnowledgeAsset surr,
      Function<KnowledgeAsset, List<KnowledgeArtifact>> mapper) {
    ResourceIdentifier aId =
        artifactId(surr.getAssetId().getNamespaceUri(), artifactId, artifactVersionTag);
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
            && (surrogateFormat == null || surrogateFormat
            .sameAs(cka.getRepresentation().getFormat())))
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
   * Extracts the ID of the canonical surrogate, from the list of Surrogates registered in a
   * canonical surrogate itself
   *
   * @param assetSurrogate The Surrogate to extract the Id from
   * @return The id of the Surrogate it'self'
   */
  public static Optional<ResourceIdentifier> getCanonicalSurrogateId(
      KnowledgeAsset assetSurrogate) {
    return getSurrogateId(assetSurrogate, Knowledge_Asset_Surrogate_2_0, null);
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
            && (surrogateFormat == null || surrogateFormat
            .sameAs(cka.getRepresentation().getFormat())))
        .findFirst()
        .map(cka -> cka.withArtifactId(newSurrogateId))
        .map(KnowledgeArtifact::getArtifactId)
        .orElse(null);
  }

  //Set
  public static CompositeKnowledgeCarrier toAnonymousCompositeAsset(
      Collection<KnowledgeAsset> components) {
    return toAnonymousCompositeAsset(null, components);
  }

  /**
   * Constructs a Composite Surrogate for an 'anonymous' Composite Asset,
   * instantiating a CompositeKnowledgeCarrier whose components are canonical Asset Surrogates
   *
   * Anonymous assets have a random asset id
   *
   * @param rootAssetId The root component (if the Struct is TREE-based, null otherwise)
   * @param components The components, which must be KnowledgeAsset surrogates
   * @return a CompositeKnowledgeCarrier with wrapped KnowledgeAsset components
   */
  public static CompositeKnowledgeCarrier toAnonymousCompositeAsset(
      ResourceIdentifier rootAssetId,
      Collection<KnowledgeAsset> components) {
    return ofUniformAnonymousComposite(
        rootAssetId,
        components,
        rep(Knowledge_Asset_Surrogate_2_0),
        inferStruct(
            randomId(),
            rootAssetId,
            randomId(),
            KnowledgeAsset::getLinks,
            KnowledgeAsset::getAssetId,
            components),
        GRAPH,
        KnowledgeAsset::getAssetId,
        ka -> getCanonicalSurrogateId(ka).orElse(null),
        KnowledgeAsset::getName
    );
  }

  /**
   * Constructs a Composite Surrogate for an 'named' Composite Asset,
   * instantiating a CompositeKnowledgeCarrier whose components are canonical Asset Surrogates
   *
   * Named assets have a known, managed asset id and version
   *
   * @param compositeAssetId The Asset Id
   * @param components The components, which must be KnowledgeAsset surrogates
   * @return a CompositeKnowledgeCarrier with wrapped KnowledgeAsset components
   */
  public static CompositeKnowledgeCarrier toNamedCompositeAsset(
      ResourceIdentifier compositeAssetId,
      Collection<KnowledgeAsset> components) {
    return toNamedCompositeAsset(compositeAssetId, components, GRAPH);
  }

  /**
   * Constructs a Composite Surrogate for an 'named' Composite Asset,
   * instantiating a CompositeKnowledgeCarrier whose components are canonical Asset Surrogates
   *
   * Named assets have a known, managed asset id and version
   *
   * @param compositeAssetId The Asset Id
   * @param components The components, which must be KnowledgeAsset surrogates
   * @param structType An explicit structure pattern
   * @return a CompositeKnowledgeCarrier with wrapped KnowledgeAsset components
   */
  public static CompositeKnowledgeCarrier toNamedCompositeAsset(
      ResourceIdentifier compositeAssetId,
      Collection<KnowledgeAsset> components,
      CompositeStructType structType) {
    return ofUniformNamedComposite(
        compositeAssetId,
        null,
        compositeAssetId,
        "",
        structType,
        inferStruct(
            compositeAssetId,
            null,
            randomId(),
            KnowledgeAsset::getLinks,
            KnowledgeAsset::getAssetId,
            components),
        components,
        rep(Knowledge_Asset_Surrogate_2_0),
        KnowledgeAsset::getAssetId,
        ka -> getCanonicalSurrogateId(ka).orElse(null),
        KnowledgeAsset::getName
    );
  }

  /**
   * Constructs an Aggregate of otherwise possibly unrelated Surrogates
   *
   * @param components The components, which must be KnowledgeAsset surrogates
   * @return a CompositeKnowledgeCarrier with wrapped KnowledgeAsset components
   */
  public static CompositeKnowledgeCarrier toAggregateAsset(
      Collection<KnowledgeAsset> components) {
    return ofUniformAggregate(
        components,
        rep(Knowledge_Asset_Surrogate_2_0),
        KnowledgeAsset::getAssetId,
        ka -> getCanonicalSurrogateId(ka).orElse(null),
        KnowledgeAsset::getName
    );
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
      KnowledgeAsset surr, Class<? extends Link> relClass, ConceptTerm relType) {
    return surr.getLinks().stream()
        .flatMap(StreamUtil.filterAs(relClass))
        .filter(rel -> relType == null
            || relType.sameTermAs(rel.getRel())
            || (rel.getRel() instanceof ConceptTerm && ((ConceptTerm) rel.getRel())
            .hasAncestor(relType))
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
   * @return A KnowledgeCarrier that wraps the Asset's (primary) Artifact manifestation,
   * preserving the relevant metadata
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
        .withLevel(inlined != null ? Serialized_Knowledge_Expression : Knowledge_Expression)
        .withRepresentation(
            canonicalArtifact.map(KnowledgeArtifact::getRepresentation).orElse(null))
        .withHref(canonicalArtifact.map(KnowledgeArtifact::getLocator).orElse(null));
  }

  public static KnowledgeCarrier toRuntimeSurrogate(
      KnowledgeAsset surrogate, ParsingLevel level, Object expr) {
    Optional<KnowledgeArtifact> canonicalArtifact
        = surrogate.getCarriers().stream().findFirst();

    return new KnowledgeCarrier()
        .withAssetId(surrogate.getAssetId())
        .withArtifactId(canonicalArtifact.map(KnowledgeArtifact::getArtifactId).orElse(null))
        .withExpression(expr)
        .withLabel(surrogate.getName())
        .withLevel(level)
        .withRepresentation(
            canonicalArtifact.map(KnowledgeArtifact::getRepresentation).orElse(null))
        .withHref(canonicalArtifact.map(KnowledgeArtifact::getLocator).orElse(null));
  }

  /**
   * Ensures runtime metadata consistency between two carriers,
   * where the latter is supposed to be a variant, possibly derivative
   * of the former
   *
   * Enforces that the two carrier share the same AssetId
   *
   * This operation should not be necessary if the variant has been
   * explicitly generated using chained API4KP compliant 'trans*ors'
   *
   * @param sourceCarrier
   * @param variantCarrier
   * @return
   */
  public static KnowledgeCarrier toVariantRuntimeSurrogate(
      KnowledgeCarrier sourceCarrier,
      KnowledgeCarrier variantCarrier) {
    return ((KnowledgeCarrier) variantCarrier.clone())
        .withAssetId(sourceCarrier.getAssetId());
  }


  /**
   * Wraps a KnowledgeAsset Surrogate (KA) in a KnowledgeCarrier (KC)
   * Surrogates are Artifacts themselves, expressed in a specific, API4KP defined schema
   * As such, they can be processed using a variety of APIs once properly wrapped
   *
   * Note: This method wraps the Asset's surrogate itself. Conversely, toRuntimeSurrogate
   * tries to wrap the Asset's (primary) Carrier, as stated by the Surrogate itself
   *
   * @param surrogate The source Knowledge Asset Surrogate
   * @return A KnowledgeCarrier that wraps the Surrogate itself,
   * allowing to process it as a Knowledge Artifact
   */
  public static KnowledgeCarrier carry(KnowledgeAsset surrogate) {
    Optional<KnowledgeArtifact> canonicalSurrogate
        = SurrogateHelper.getSurrogateMetadata(surrogate, Knowledge_Asset_Surrogate_2_0, null);

    return AbstractCarrier.ofAst(surrogate)
        .withAssetId(surrogate.getAssetId())
        .withArtifactId(canonicalSurrogate.map(KnowledgeArtifact::getArtifactId).orElse(null))
        .withLabel(surrogate.getName())
        .withRepresentation(rep(Knowledge_Asset_Surrogate_2_0));
  }

  /**
   * Extracts the Components that are directly asserted as 'links' in a Knowledge Asset Surrogate,
   * which is implicitly assumed to be the surrogate of a Composite Knowledge Asset
   * Accepts an optional term, which will be matched against the Component.role
   *
   * @param surrogate
   * @param role
   * @return
   */
  public static Stream<ResourceIdentifier> getComponents(KnowledgeAsset surrogate, Term role) {
    return surrogate.getLinks().stream()
        .flatMap(StreamUtil.filterAs(Component.class))
        .filter(comp -> comp.getRel().sameTermAs(Has_Structural_Component))
        .filter(comp -> role == null || comp.getRol().sameTermAs(role))
        .map(Component::getHref);
  }

  /**
   * Extracts the Components from a Composite Knowledge Asset, manifested via a
   * CompositeKnowledgeCarrier that wraps a Struct, as well as the Surrogates of the Components
   * Accepts an optional term, which will be matched against the Component.role
   *
   * @param surrogate
   * @param role
   * @return
   */
  public static Stream<ResourceIdentifier> getComponents(
      KnowledgeAsset surrogate, Model struct, Term role) {
    if (struct == null) {
      return getComponents(surrogate, role);
    }
    Resource composite = createResource(surrogate.getAssetId().getVersionId().toString());
    return struct.listStatements(
            composite,
            createProperty(Has_Structural_Component.getReferentId().toString()),
            (String) null)
        .toList().stream()
        .map(Statement::getObject)
        .filter(RDFNode::isURIResource)
        .map(RDFNode::asResource)
        .map(Resource::getURI)
        .filter(comp -> role == null ||
            struct.contains(
                createResource(comp),
                RDF.type,
                createResource(role.getReferentId().toString())))
        .map(URI::create)
        .map(SemanticIdentifier::newVersionId);
  }

  /**
   * Extracts the Component of a given type, as asserted directly in a Knowledge Assset Surrogate
   *
   * @param surrogate
   * @param role
   * @return
   */
  public static Optional<ResourceIdentifier> getComponent(KnowledgeAsset surrogate, Term role) {
    return getComponents(surrogate, role).findFirst();
  }

  /**
   * Extracts the Component of a given type, as asserted directly in a Knowledge Assset Surrogate
   *
   * @param surrogate
   * @param role
   * @return
   */
  public static Optional<ResourceIdentifier> getComponent(KnowledgeAsset surrogate, Model struct,
      Term role) {
    return getComponents(surrogate, struct, role).findFirst();
  }

  public enum VersionIncrement {MAJOR, MINOR, PATCH}

  public static void incrementVersion(KnowledgeAsset changingSurrogate, VersionIncrement incr) {
    getSurrogateMetadata(changingSurrogate, Knowledge_Asset_Surrogate_2_0, null)
        .ifPresent(meta ->
            meta.withArtifactId(nextVersion(changingSurrogate, incr)));
  }


  /**
   * Increments the version ID of a Knowledge Asset Surrogate
   * returns the incremented ID (without updating the Surrogate itself)
   * @param changingSurrogate
   * @param incr
   * @return
   */
  public static ResourceIdentifier nextVersion(KnowledgeAsset changingSurrogate,
      VersionIncrement incr) {
    return getSurrogateMetadata(changingSurrogate, Knowledge_Asset_Surrogate_2_0, null)
        .map(meta -> {
          Version surrogateVersion = Version.valueOf(meta.getArtifactId().getVersionTag());
          switch (incr) {
            case MAJOR:
              surrogateVersion = surrogateVersion.incrementMajorVersion();
              break;
            case MINOR:
              surrogateVersion = surrogateVersion.incrementMinorVersion();
              break;
            case PATCH:
              surrogateVersion = surrogateVersion.incrementPatchVersion();
              break;
          }
          return newId(
              meta.getArtifactId().getNamespaceUri(),
              meta.getArtifactId().getUuid(),
              surrogateVersion);
        })
        .or(() -> SurrogateHelper.getCanonicalSurrogateId(changingSurrogate))
        .orElseThrow(() -> new IllegalStateException("Unable to detect current and/or next version"
            + "for canonical surrogate of asset " + changingSurrogate.getAssetId()));
  }
}