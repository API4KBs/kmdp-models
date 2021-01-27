package org.omg.spec.api4kp._20200801;

import static edu.mayo.kmdp.registry.Registry.KNOWLEDGE_ASSET_URI;
import static edu.mayo.kmdp.util.JenaUtil.objA;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.of;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.contrastors.SyntacticRepresentationContrastor.theRepContrastor;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_LATEST;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.hashIdentifiers;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newId;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.randomId;
import static org.omg.spec.api4kp._20200801.services.CompositeStructType.GRAPH;
import static org.omg.spec.api4kp._20200801.services.CompositeStructType.NONE;
import static org.omg.spec.api4kp._20200801.services.CompositeStructType.SET;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.Turtle;
import static org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartTypeSeries.Has_Structural_Component;
import static org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartTypeSeries.Has_Structuring_Component;

import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.omg.spec.api4kp._20200801.contrastors.ParsingLevelContrastor;
import org.omg.spec.api4kp._20200801.id.Link;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.CompositeStructType;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevel;

public interface AbstractCompositeCarrier extends ClosedComposite<KnowledgeCarrier, KnowledgeCarrier, CompositeKnowledgeCarrier> {


  /* *********************************************************************************
   *  Named Composites
   * ******************************************************************************** */


  /**
   * Creates a Composite Knowledge Carrier for a named Composite Knowledge Resource,
   * using the Identifiers and the Structure provided by the client
   *
   * Wraps the components, which are barebone Expressions, into KnowledgeCarriers
   *
   * All the components are expected to share the same representation, which is shared
   * by the Composite
   *
   * @param assetId The ID of the composite Asset
   * @param artifactId The ID of the composite Artifact.- used to reference a (flat)
   *                   manifestation of the Asset, which could be inlined
   * @param rootId  The optional ID of a 'root' component, which may coincide with the Asset itself
   * @param name A human readable label associated to the Composite Asset
   * @param structType The topology of the Composite
   * @param struct The Structure of the Composite
   * @param artifacts The components
   * @param rep the common SyntacticReprsentation
   * @param assetIdentificator A function that assigns an AssetId to each Expression
   * @param artifactidentificator A function that assigns an ArtifactId to each Expression
   * @param assetLabeler A function that assigns a name/label to each Expression
   * @param <T> the Type of the component Expressions
   * @return The named Composite Carrier
   */
  static <T> CompositeKnowledgeCarrier ofUniformNamedComposite(
      ResourceIdentifier assetId,
      ResourceIdentifier artifactId,
      ResourceIdentifier rootId,
      String name,
      CompositeStructType structType,
      KnowledgeCarrier struct,
      Collection<T> artifacts,
      SyntacticRepresentation rep,
      Function<T, ResourceIdentifier> assetIdentificator,
      Function<T, ResourceIdentifier> artifactidentificator,
      Function<T, String> assetLabeler) {
    Map<ResourceIdentifier, KnowledgeCarrier> components =
        wrapComponents(artifacts, rep, assetIdentificator, artifactidentificator, assetLabeler)
            .collect(Collectors.toMap(
                KnowledgeCarrier::getAssetId,
                kc -> kc
            ));
    return addUniformRepresentation(
        ofNamedComposite(assetId, artifactId, rootId, name, structType, struct, components),
        components.values());
  }

  /**
   * Creates a Composite Knowledge Carrier for a named Composite Knowledge Resource,
   * using the Identifiers and the Structure provided by the client
   *
   * All the components are expected to share the same representation, which is shared
   * by the Composite
   *
   * @param assetId The ID of the composite Asset
   * @param artifactId The ID of the composite Artifact.- used to reference a (flat)
   *                   manifestation of the Asset, which could be inlined
   * @param rootId  The optional ID of a 'root' component, which may coincide with the Asset itself
   * @param name A human readable label associated to the Composite Asset
   * @param structType The topology of the Composite
   * @param struct The Structure of the Composite
   * @param artifacts The components
   * @return The named Composite Carrier
   */
  static CompositeKnowledgeCarrier ofUniformNamedComposite(
      ResourceIdentifier assetId,
      ResourceIdentifier artifactId,
      ResourceIdentifier rootId,
      String name,
      CompositeStructType structType,
      KnowledgeCarrier struct,
      Collection<KnowledgeCarrier> artifacts) {
    Map<ResourceIdentifier, KnowledgeCarrier> components =
        artifacts.stream().collect(Collectors.toMap(
            KnowledgeCarrier::getAssetId,
            kc -> kc
        ));
    return addUniformRepresentation(
        ofNamedComposite(assetId, artifactId, rootId, name, structType, struct, components),
        artifacts);
  }


  /**
   * Creates a Composite Knowledge Carrier for a named Composite Knowledge Resource,
   * using the Identifiers and the Structure provided by the client
   *
   * All the components are expected to share the same representation, which is shared
   * by the Composite
   *
   * @param assetId The ID of the composite Asset
   * @param artifactId The ID of the composite Artifact.- used to reference a (flat)
   *                   manifestation of the Asset, which could be inlined
   * @param rootId  The optional ID of a 'root' component, which may coincide with the Asset itself
   * @param name A human readable label associated to the Composite Asset
   * @param structType The topology of the Composite
   * @param visitor A function that determines the relationships for each component
   * @param artifacts The components
   * @return The named Composite Carrier
   */
  static CompositeKnowledgeCarrier ofUniformNamedComposite(
      ResourceIdentifier assetId,
      ResourceIdentifier artifactId,
      ResourceIdentifier rootId,
      String name,
      CompositeStructType structType,
      Function<KnowledgeCarrier, Collection<? extends Link>> visitor,
      Collection<KnowledgeCarrier> artifacts) {
    Map<ResourceIdentifier, KnowledgeCarrier> components =
        artifacts.stream().collect(Collectors.toMap(
            KnowledgeCarrier::getAssetId,
            kc -> kc
        ));
    CompositeKnowledgeCarrier ckc = addUniformRepresentation(
        ofNamedComposite(assetId, artifactId, rootId, name, structType, null, components),
        artifacts);
    return ckc.withStruct(
        inferStruct(ckc.getAssetId(), rootId, randomId(),
            visitor, KnowledgeCarrier::getAssetId, artifacts));
  }


  /**
   * Creates a Composite Knowledge Carrier for a named Composite Knowledge Resource,
   * using the Identifiers and the Structure provided by the client
   *
   * @param assetId The ID of the composite Asset
   * @param artifactId The ID of the composite Artifact.- used to reference a (flat)
   *                   manifestation of the Asset, which could be inlined
   * @param rootId  The optional ID of a 'root' component, which may coincide with the Asset itself
   * @param name A human readable label associated to the Composite Asset
   * @param structType The topology of the Composite
   * @param struct The Structure of the Composite
   * @param artifacts The components
   * @return The named Composite Carrier
   */
  static CompositeKnowledgeCarrier ofMixedNamedComposite(
      ResourceIdentifier assetId,
      ResourceIdentifier artifactId,
      ResourceIdentifier rootId,
      String name,
      CompositeStructType structType,
      KnowledgeCarrier struct,
      Collection<KnowledgeCarrier> artifacts) {
    Map<ResourceIdentifier, KnowledgeCarrier> components =
        artifacts.stream().collect(Collectors.toMap(
            KnowledgeCarrier::getAssetId,
            kc -> kc
        ));
    return ofNamedComposite(assetId, artifactId, rootId, name, structType, struct, components);
  }


  /**
   * Instantiates a CompositeKnowledgeCarrier for an 'anonymous' composite,
   * setting the AssetId to a random
   * @param rootId the 'root' assetID (if the topology is GRAPH or TREE based)
   * @param struct the Structure of the composite.
   *               Inferred from the optional presence of a root, if not specified explicitly
   * @param structType the topology of the structure
   * @param artifacts the components
   * @return a CompositeKnowledgeCarrier initialized with the given parameters
   */
  private static CompositeKnowledgeCarrier ofNamedComposite(
      ResourceIdentifier assetId,
      ResourceIdentifier artifactId,
      ResourceIdentifier rootId,
      String name,
      CompositeStructType structType,
      KnowledgeCarrier struct,
      Map<ResourceIdentifier, KnowledgeCarrier> artifacts) {
    return newCompositeKnowledgeCarrier(
        assetId,
        artifactId,
        rootId,
        name,
        artifacts.values(),
        null,
        null,
        structType,
        struct
    );
  }



  /* *********************************************************************************
   *  Anonymous Composites
   * ******************************************************************************** */


  /**
   * Creates an Anonymous Composite Knowledge Carrier from a set of Knowledge Artifacts
   * Creates an ephemeral Asset ID, and establishes a SET-oriented Struct with the given components
   *
   * @param artifacts The artifacts to be aggregated into the composite
   * @return An Anonymous Composite Knowledge Carrier
   */
  static CompositeKnowledgeCarrier ofMixedAnonymousComposite(
      Collection<KnowledgeCarrier> artifacts) {
    return ofMixedAnonymousComposite(null, artifacts);
  }


  /**
   * Creates an Anonymous Composite Knowledge Carrier from a set of Knowledge Artifacts
   *
   * Creates an ephemeral Asset ID, and establishes a TREE-oriented Struct with the given components,
   * where the root of the TREE is a given component.
   * A root component is such that it is depends (transitively) on every other component,
   * and is a dependency of no other component
   *
   * @param rootId The ID of the 'root' component
   * @param artifacts The artifacts to be aggregated into the composite
   * @return An Anonymous Composite Knowledge Carrier
   */
  static CompositeKnowledgeCarrier ofMixedAnonymousComposite(
      ResourceIdentifier rootId,
      Collection<KnowledgeCarrier> artifacts) {
    CompositeKnowledgeCarrier ckc = ofAnonymous(rootId, null, null, artifacts);
    return ckc.withStruct(inferStruct(
        ckc.getAssetId(),
        rootId,
        randomId(),
        t -> Collections.emptyList(),
        KnowledgeCarrier::getAssetId,
        artifacts
    ));
  }

  /**
   * Creates an Anonymous Composite Knowledge Carrier from a set of Knowledge Artifacts
   *
   * Creates an ephemeral Asset ID, and establishes a Struct with the given components.
   * Uses a visitor function (and an optional Root) to determine the relationships
   * between the components. These relationships will be added to the composite structure,
   * on top of the 'has structural component' ones between the Composite Asset ID and the
   * components themselves
   *
   * @param rootId The ID of the 'root' component
   * @param visitor A function that determines the relationships between the components
   * @param structType The topology of the component's structure
   * @param artifacts The artifacts to be aggregated into the composite
   * @return An Anonymous Composite Knowledge Carrier
   */
  static CompositeKnowledgeCarrier ofMixedAnonymousComposite(
      ResourceIdentifier rootId,
      Function<KnowledgeCarrier, Collection<? extends Link>> visitor,
      CompositeStructType structType,
      Collection<KnowledgeCarrier> artifacts) {
    CompositeKnowledgeCarrier ckc = ofAnonymous(rootId, null, structType, artifacts);
    return ckc.withStruct(
        inferStruct(ckc.getAssetId(), rootId, randomId(),
            visitor, KnowledgeCarrier::getAssetId, artifacts));
  }

  /**
   * Creates an Anonymous Composite Knowledge Carrier from a set of Knowledge Artifacts
   *
   * Creates an ephemeral Asset ID, links the given artifacts as components, and adds the
   * provided Structure on top.
   *
   * @param rootId The ID of the 'root' component
   * @param struct An explicitly provided struct for the new Composite
   * @param structType The topology of the component's structure
   * @param artifacts The artifacts to be aggregated into the composite
   * @return An Anonymous Composite Knowledge Carrier
   */
  static CompositeKnowledgeCarrier ofMixedAnonymousComposite(
      ResourceIdentifier rootId,
      KnowledgeCarrier struct,
      CompositeStructType structType,
      Collection<KnowledgeCarrier> artifacts) {
    return ofAnonymous(rootId, struct, structType, artifacts);
  }


  /**
   * Creates an Anonymous, Uniform Composite Knowledge Carrier from a set of Knowledge Artifacts
   * Creates an ephemeral Asset ID, and establishes a SET-oriented Struct with the given components
   * The Composite exposes the shared representation
   *
   * @param artifacts The artifacts to be aggregated into the composite
   * @return An Anonymous Composite Knowledge Carrier
   */
  static CompositeKnowledgeCarrier ofUniformAnonymousComposite(
      Collection<KnowledgeCarrier> artifacts) {
    return ofUniformAnonymousComposite(null, artifacts);
  }

  /**
   * Creates an Anonymous, Uniform Composite Knowledge Carrier from a set of Knowledge Artifacts
   *
   * Creates an ephemeral Asset ID, and establishes a TREE-oriented Struct with the given components,
   * where the root of the TREE is a given component.
   * A root component is such that it is depends (transitively) on every other component,
   * and is a dependency of no other component
   *
   * The Composite exposes the shared representation
   *
   * @param rootId The ID of the 'root' component
   * @param artifacts The artifacts to be aggregated into the composite
   * @return An Anonymous Composite Knowledge Carrier
   */
  static CompositeKnowledgeCarrier ofUniformAnonymousComposite(
      ResourceIdentifier rootId,
      Collection<KnowledgeCarrier> artifacts) {
    CompositeKnowledgeCarrier ckc = addUniformRepresentation(
        ofAnonymous(rootId, null, null, artifacts), artifacts);
    return ckc.withStruct(inferStruct(
        ckc.getAssetId(),
        rootId,
        randomId(),
        t -> Collections.emptyList(),
        KnowledgeCarrier::getAssetId,
        artifacts
    ));
  }

  /**
   * Creates an Anonymous, Uniform Composite Knowledge Carrier from a set of Knowledge Artifacts
   *
   * Creates an ephemeral Asset ID, links the given artifacts as components, and adds the
   * provided Structure on top.
   *
   * The Composite exposes the shared representation
   *
   * @param rootId The ID of the 'root' component
   * @param struct An explicitly provided struct for the new Composite
   * @param structType The topology of the component's structure
   * @param artifacts The artifacts to be aggregated into the composite
   * @return An Anonymous Composite Knowledge Carrier
   */
  static CompositeKnowledgeCarrier ofUniformAnonymousComposite(
      ResourceIdentifier rootId,
      KnowledgeCarrier struct,
      CompositeStructType structType,
      Collection<KnowledgeCarrier> artifacts) {
    return addUniformRepresentation(
        ofAnonymous(rootId, struct, structType, artifacts), artifacts);
  }


  /**
   * Creates an Anonymous Composite Knowledge Carrier from a set of Knowledge Artifacts
   *
   * Creates an ephemeral Asset ID, and establishes a Struct with the given components.
   * Uses a visitor function (and an optional Root) to determine the relationships
   * between the components. These relationships will be added to the composite structure,
   * on top of the 'has structural component' ones between the Composite Asset ID and the
   * components themselves
   *
   * @param rootId The ID of the 'root' component
   * @param visitor A function that determines the relationships between the components
   * @param structType The topology of the component's structure
   * @param artifacts The artifacts to be aggregated into the composite
   * @return An Anonymous Composite Knowledge Carrier
   */
  static CompositeKnowledgeCarrier ofUniformAnonymousComposite(
      ResourceIdentifier rootId,
      Function<KnowledgeCarrier, Collection<? extends Link>> visitor,
      CompositeStructType structType,
      Collection<KnowledgeCarrier> artifacts) {
    CompositeKnowledgeCarrier ckc = addUniformRepresentation(
        ofAnonymous(rootId, null, structType, artifacts), artifacts);
    return ckc.withStruct(
        inferStruct(ckc.getAssetId(), rootId, randomId(),
            visitor, KnowledgeCarrier::getAssetId, artifacts));
  }

  /**
   * Creates an Anonymous, Uniform Composite Knowledge Carrier from a set of Knowledge Expressions,
   * wrapping the Expressions into KnowledgeCarriers
   *
   * Creates an ephemeral Asset ID, links the given artifacts as components, and adds the
   * provided Structure on top.
   *
   *
   * The Composite exposes the shared representation
   *
   * @param rootId The ID of the 'root' component
   * @param struct An explicitly provided struct for the new Composite
   * @param structType The topology of the component's structure
   * @param artifacts The artifacts to be aggregated into the composite
   * @param rep the common SyntacticReprsentation
   * @param assetIdentificator A function that assigns an AssetId to each Expression
   * @param artifactidentificator A function that assigns an ArtifactId to each Expression
   * @param assetLabeler A function that assigns a name/label to each Expression
   * @return An Anonymous Composite Knowledge Carrier
   */
  static <T> CompositeKnowledgeCarrier ofUniformAnonymousComposite(
      ResourceIdentifier rootId,
      Collection<T> artifacts,
      SyntacticRepresentation rep,
      KnowledgeCarrier struct,
      CompositeStructType structType,
      Function<T, ResourceIdentifier> assetIdentificator,
      Function<T, ResourceIdentifier> artifactidentificator,
      Function<T, String> assetLabeler) {
    List<KnowledgeCarrier> components =
        wrapComponents(artifacts, rep, assetIdentificator, artifactidentificator, assetLabeler)
            .collect(Collectors.toList());
    return ofUniformAnonymousComposite(rootId, struct, structType, components);
  }


  /**
   * Instantiates a CompositeKnowledgeCarrier for an 'anonymous' composite,
   * setting the AssetId to a random
   * @param rootId the 'root' assetID (if the topology is GRAPH or TREE based)
   * @param struct the Structure of the composite.
   *               Inferred from the optional presence of a root, if not specified explicitly
   * @param structType the topology of the structure
   * @param artifacts the components
   * @return a CompositeKnowledgeCarrier initialized with the given parameters
   */
  private static CompositeKnowledgeCarrier ofAnonymous(
      ResourceIdentifier rootId,
      KnowledgeCarrier struct,
      CompositeStructType structType,
      Collection<KnowledgeCarrier> artifacts) {
    CompositeKnowledgeCarrier ckc = newCompositeKnowledgeCarrier(
        randomId(),
        null,
        rootId,
        null,
        artifacts,
        null,
        null,
        inferAnonStructType(structType, rootId),
        struct
    );
    return addLabel(ckc, "(Anonymous Composite)");
  }

  private static CompositeStructType inferAnonStructType(CompositeStructType structType,
      ResourceIdentifier rootId) {
    if (structType != null) {
      return structType;
    }
    if (rootId == null) {
      return SET;
    }
    return GRAPH;
  }


  /* *********************************************************************************
   *  Aggregates
   * ******************************************************************************** */


  /**
   * Creates an Mixed, Aggregate Knowledge Carrier from a set of Knowledge Artifacts
   * An Aggregate has multiple components, but does not have a root nor a structure,
   *
   * @param artifacts The artifacts to be aggregated
   * @return An Aggregate Knowledge Carrier
   */
  static CompositeKnowledgeCarrier ofMixedAggregate(
      Collection<KnowledgeCarrier> artifacts) {
    return ofAggregate(artifacts);
  }

  /**
   * Creates an Uniform, Aggregate Knowledge Carrier from a set of Knowledge Artifacts
   * An Aggregate has multiple components, but does not have a root nor a structure,
   * Being Uniform, the Aggregate reflects the common representation of its components
   *
   * @param artifacts The artifacts to be aggregated
   * @return An Aggregate Knowledge Carrier
   * @throws IllegalStateException if the artifacts do not have equivalent Representations
   */
  static CompositeKnowledgeCarrier ofUniformAggregate(
      Collection<KnowledgeCarrier> artifacts) {
    return addUniformRepresentation(
        ofAggregate(artifacts),
        artifacts);
  }

  /**
   * Creates an Uniform, Aggregate Knowledge Carrier from a set of Knowledge Expressions
   * An Aggregate has multiple components, but does not have a root nor a structure,
   * Being Uniform, the Aggregate reflects the common representation of its components
   *
   * @param artifacts The Expressions to be aggregated
   * @param rep the common Syntactic Representation
   * @param assetIdentificator A function that assigns an AssetId to each Expression
   * @param artifactidentificator A function that assigns an ArtifactId to each Expression
   * @param assetLabeler A function that assigns a name/label to each Expression
   *
   * @return An Aggregate Knowledge Carrier
   */
  static <T> CompositeKnowledgeCarrier ofUniformAggregate(
      Collection<T> artifacts,
      SyntacticRepresentation rep,
      Function<T, ResourceIdentifier> assetIdentificator,
      Function<T, ResourceIdentifier> artifactidentificator,
      Function<T, String> assetLabeler)  {
    List<KnowledgeCarrier> components =
        wrapComponents(artifacts, rep, assetIdentificator, artifactidentificator, assetLabeler)
            .collect(Collectors.toList());
    return ofUniformAggregate(components);
  }

  private static CompositeKnowledgeCarrier ofAggregate(
      Collection<KnowledgeCarrier> artifacts) {
    return newCompositeKnowledgeCarrier(
        randomId(),
        null,
        null,
        "(Anonymous Mixed Aggregate)",
        artifacts,
        null,
        null,
        NONE,
        null);
  }



  /* *********************************************************************************
   *  Helper functions
   * ******************************************************************************** */

  static <T> KnowledgeCarrier inferSetStruct(
      ResourceIdentifier assetId,
      ResourceIdentifier structId,
      Function<T,ResourceIdentifier> assetIdentifier,
      Collection<T> artifacts) {
    return inferStruct(
        assetId,
        null,
        structId,
        x -> Collections.emptyList(),
        assetIdentifier,
        artifacts
    );
  }

  static <T> KnowledgeCarrier inferStruct(
      ResourceIdentifier assetId,
      ResourceIdentifier rootId,
      ResourceIdentifier structId,
      Function<T, Collection<? extends Link>> visitor,
      Function<T,ResourceIdentifier> assetIdentifier,
      Collection<T> components) {
    Map<ResourceIdentifier, T> artifacts =
        components.stream().collect(Collectors.toMap(
            assetIdentifier,
            x -> x));

    List<Statement> structs =
        artifacts.keySet().stream()
            .flatMap(
                component -> Stream.of(
                    objA(
                        assetId.getVersionId(),
                        Has_Structural_Component.getReferentId(),
                        component.getVersionId()),
                    objA(
                        assetId.getVersionId().toString(),
                        RDF.type.getURI(),
                        KNOWLEDGE_ASSET_URI),
                    objA(
                        component.getVersionId().toString(),
                        RDF.type.getURI(),
                        KNOWLEDGE_ASSET_URI)
                ))
            .collect(Collectors.toList());

    if (rootId != null && artifacts.containsKey(rootId)) {
      // Add Struct for relationships between Components
      visitor.apply(artifacts.get(rootId))
          .forEach(
              childLink -> {
                URI rel = childLink.getRel().getReferentId();
                URI versionId = childLink.getHrefVersionURI();
                structs.add(
                    objA(
                        rootId.getVersionId().toString(), rel.toString(), versionId.toString()));
              });
    }

    if (structId != null) {
      structs.add(objA(
          assetId.getVersionId(),
          Has_Structuring_Component.getReferentId(),
          structId.getVersionId()));
    }

    // Create String and set Struct
    String structString = JenaUtil.fromStatementsToString(structs);
    return
        of(structString)
            .withAssetId(structId)
            .withArtifactId(randomId())
            .withRepresentation(rep(OWL_2, Turtle, TXT));
  }

  private static CompositeKnowledgeCarrier newCompositeKnowledgeCarrier(
      ResourceIdentifier assetId,
      ResourceIdentifier artifactId,
      ResourceIdentifier rootId,
      String label,
      Collection<KnowledgeCarrier> artifacts,
      SyntacticRepresentation rep,
      ParsingLevel level,
      CompositeStructType structType,
      KnowledgeCarrier struct) {

    return new CompositeKnowledgeCarrier()
        .withAssetId(assetId)
        .withArtifactId(artifactId)
        .withRootId(rootId)
        .withLabel(label)
        .withComponent(artifacts)
        .withRepresentation(rep)
        .withLevel(level)
        .withStructType(structType)
        .withStruct(struct);
  }

  private static <T> Stream<KnowledgeCarrier> wrapComponents(
      Collection<T> artifacts,
      SyntacticRepresentation rep,
      Function<T, ResourceIdentifier> assetIdentificator,
      Function<T, ResourceIdentifier> artifactIdentificator,
      Function<T, String> assetLabeler) {
    return wrapComponents(
        rep,
        artifacts,
        ParsingLevelContrastor.detectLevel(rep),
        assetIdentificator,
        artifactIdentificator,
        assetLabeler);
  }

  private static <T> Stream<KnowledgeCarrier> wrapComponents(
      SyntacticRepresentation rep,
      Collection<T> artifacts,
      ParsingLevel level,
      Function<T, ResourceIdentifier> assetIdentificator,
      Function<T, ResourceIdentifier> artifactIdentificator,
      Function<T, String> assetLabeler) {

    return artifacts.stream()
        .map(x -> x instanceof KnowledgeCarrier
            ? (KnowledgeCarrier) x
            : of(x, level)
                .withRepresentation(rep)
                .withAssetId(assetIdentificator.apply(x))
                .withArtifactId(artifactIdentificator.apply(x))
                .withLabel(assetLabeler.apply(x)));
  }

  private static CompositeKnowledgeCarrier addUniformRepresentation(CompositeKnowledgeCarrier ckc,
      Collection<KnowledgeCarrier> artifacts) {
    if (! artifacts.isEmpty()) {
      KnowledgeCarrier kc = artifacts.iterator().next();
      ParsingLevel level = ParsingLevelContrastor.detectLevel(kc);
      SyntacticRepresentation rep = kc.getRepresentation();

      boolean consistent = artifacts.stream().map(KnowledgeCarrier::getRepresentation)
          .allMatch(x -> theRepContrastor.isEqual(x,rep));
      if (! consistent) {
        throw new IllegalArgumentException();
      }

      ckc.withLevel(level);
      ckc.withRepresentation(rep);
    }
    return ckc;
  }

  private static CompositeKnowledgeCarrier addLabel(
      CompositeKnowledgeCarrier ckc,
      String defaultLabel) {
    return ckc.withLabel(
        ckc.tryMainComponent()
            .map(KnowledgeCarrier::getLabel)
            .orElse(defaultLabel));
  }

  private static <T> ResourceIdentifier hashComponentIds(
      Collection<T> artifacts,
      Function<T, ResourceIdentifier> assetIdentificator) {
    return
        artifacts.stream()
            .map(assetIdentificator)
            .reduce(SemanticIdentifier::hashIdentifiers)
            .map(
                compsId ->
                    hashIdentifiers(
                        compsId, newId(Util.uuid(CompositeStructType.TREE), VERSION_LATEST)))
            .orElseThrow(IllegalArgumentException::new);
  }



}
