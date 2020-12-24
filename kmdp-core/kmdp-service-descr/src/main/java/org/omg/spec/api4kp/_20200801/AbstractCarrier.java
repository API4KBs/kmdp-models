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
package org.omg.spec.api4kp._20200801;

import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_LATEST;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.hashIdentifiers;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.newId;
import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.randomId;
import static org.omg.spec.api4kp._20200801.services.CompositeStructType.NONE;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.Turtle;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.asEnum;
import static org.omg.spec.api4kp._20200801.taxonomy.structuralreltype.StructuralPartTypeSeries.Has_Structural_Component;

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jena.rdf.model.Statement;
import org.omg.spec.api4kp._20200801.contrastors.ParsingLevelContrastor;
import org.omg.spec.api4kp._20200801.id.KeyIdentifier;
import org.omg.spec.api4kp._20200801.id.Link;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.CompositeStructType;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.services.transrepresentation.ModelMIMECoder;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfile;
import org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerialization;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.Lexicon;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevel;
import org.w3c.dom.Document;

public interface AbstractCarrier {

  //TODO This will become a controlled term in a future release of the ontologies (version 8+)
  String HAS_MEMBER = "<https://www.omg.org/spec/LCC/Languages/LanguageRepresentation/hasMember>";

  enum Encodings {
    DEFAULT;
  }

  static KnowledgeCarrier of(byte[] encoded) {
    return new org.omg.spec.api4kp._20200801.services.resources.KnowledgeCarrier()
        .withAssetId(randomId())
        .withExpression(encoded)
        .withLevel(Encoded_Knowledge_Expression);
  }

  static KnowledgeCarrier of(InputStream stream) {
    return new org.omg.spec.api4kp._20200801.services.resources.KnowledgeCarrier()
        .withAssetId(randomId())
        .withExpression(FileUtil.readBytes(stream).orElse(new byte[0]))
        .withLevel(Encoded_Knowledge_Expression);
  }

  static KnowledgeCarrier of(String serialized) {
    return new org.omg.spec.api4kp._20200801.services.resources.KnowledgeCarrier()
        .withAssetId(randomId())
        .withExpression(serialized)
        .withLevel(Serialized_Knowledge_Expression);
  }

  static KnowledgeCarrier of(Document dox) {
    return ofTree(dox);
  }

  static KnowledgeCarrier of(JsonNode jdox) {
    return ofTree(jdox);
  }

  static KnowledgeCarrier ofTree(Object parseTree) {
    return new org.omg.spec.api4kp._20200801.services.resources.KnowledgeCarrier()
        .withAssetId(randomId())
        .withExpression(parseTree)
        .withLevel(Concrete_Knowledge_Expression);
  }

  static KnowledgeCarrier ofAst(Object ast) {
    return new org.omg.spec.api4kp._20200801.services.resources.KnowledgeCarrier()
        .withAssetId(randomId())
        .withExpression(ast)
        .withLevel(Abstract_Knowledge_Expression);
  }

  static KnowledgeCarrier of(byte[] encoded, SyntacticRepresentation rep) {
    if (rep.getCharset() == null) {
      rep.withCharset(Charset.defaultCharset().name());
    }
    if (rep.getEncoding() == null) {
      rep.withEncoding(Encodings.DEFAULT.name());
    }
    return of(encoded)
        .withRepresentation(rep);
  }

  static KnowledgeCarrier of(InputStream stream, SyntacticRepresentation rep) {
    if (rep.getCharset() == null) {
      rep.withCharset(Charset.defaultCharset().name());
    }
    if (rep.getEncoding() == null) {
      rep.withEncoding(Encodings.DEFAULT.name());
    }
    return of(stream)
        .withRepresentation(rep);
  }

  static KnowledgeCarrier of(String serialized, SyntacticRepresentation rep) {
    if (rep.getCharset() == null) {
      rep.withCharset(Charset.defaultCharset().name());
    }
    return of(serialized)
        .withRepresentation(rep);
  }

  static KnowledgeCarrier of(Document dox, SyntacticRepresentation rep) {
    return of(dox)
        .withRepresentation(rep);
  }

  static KnowledgeCarrier of(JsonNode jdox, SyntacticRepresentation rep) {
    return of(jdox)
        .withRepresentation(rep);
  }

  static KnowledgeCarrier ofAst(Object ast, SyntacticRepresentation rep) {
    return ofAst(ast)
        .withRepresentation(rep);
  }

  static KnowledgeCarrier ofTree(Object ptree, SyntacticRepresentation rep) {
    return ofTree(ptree)
        .withRepresentation(rep);
  }

  /**
   * Constructs the appropriate type of KnowledgeCarrier for the
   * given artifact at the given abstraction level
   * @param artifact the artifact to be wrapped
   * @param level the parsing level of the artifact
   * @return a KnowledgeCarrier that wraps the artifact
   */
  static KnowledgeCarrier of(Object artifact, ParsingLevel level) {
    switch (asEnum(level)) {
      case Concrete_Knowledge_Expression:
        return ofTree(artifact)
            .withLevel(level);
      case Serialized_Knowledge_Expression:
        return of(artifact.toString())
            .withLevel(level);
      case Encoded_Knowledge_Expression:
        return of((byte[]) artifact)
            .withLevel(level);
      case Abstract_Knowledge_Expression:
        return ofAst(artifact)
            .withLevel(level);
      default:
        throw new IllegalArgumentException(
            "BUG: 'Asset Surrogate' should not be a valid parsing level");
    }
  }

  /**
   * Creates a Composite Knowledge Carrier from a set of "homogeneous" Knowledge Artifacts
   * that share the same representation, with a set-oriented structure - i.e. a structure.
   * where each one of the artifacts is a member with no explicit order or function.
   *
   * Assigns random asset and artifact IDs in the process
   *
   * @param rep The common representation
   * @param artifacts The artifacts to be aggregated into the composite
   * @param <T> The common type of the artifacts
   * @return A set-oriented Composite Knowledge Carrier
   */
  static <T> CompositeKnowledgeCarrier ofSet(SyntacticRepresentation rep, Collection<T> artifacts) {
    return ofIdentifiableSet(rep,
        x -> randomId(),
        x -> randomId(),
        x -> null,
        artifacts);
  }

  static CompositeKnowledgeCarrier ofIdentifiableSet(
      Collection<KnowledgeCarrier> components) {

    List<Object> expressions = new ArrayList<>();
    Map<Object,ResourceIdentifier> assetIds = new IdentityHashMap<>();
    Map<Object,ResourceIdentifier> artifactIds = new IdentityHashMap<>();
    Map<Object,String> labels = new IdentityHashMap<>();
    SyntacticRepresentation rep = components.isEmpty() ? null : components.iterator().next().getRepresentation();

    components.forEach(comp -> {
      Object expr = comp.getExpression();
      expressions.add(expr);
      assetIds.put(expr,comp.getAssetId());
      artifactIds.put(expr,comp.getArtifactId());
      labels.put(expr,comp.getLabel());
    });

    return ofIdentifiableSet(rep, assetIds::get, artifactIds::get, labels::get, expressions);
  }

  /**
   * Creates a Composite Knowledge Carrier from a set of "homogeneous" Knowledge Artifacts
   * that share the same representation, with a set-oriented structure - i.e. a structure.
   * where each one of the artifacts is a member with no explicit order or function.
   *
   * @param rep The common representation
   * @param artifacts The artifacts to be aggregated into the composite
   * @param assetIdentificator A function that allows to extract an (asset) ID from each of the artifacts
   * @param artifactIdentificator A function that allows to extract an (artifact) ID from each of the artifacts
   * @param <T> The common type of the artifacts
   * @return A set-oriented Composite Knowledge Carrier
   */
  static <T> CompositeKnowledgeCarrier ofIdentifiableSet(
      SyntacticRepresentation rep,
      Function<T, ResourceIdentifier> assetIdentificator,
      Function<T, ResourceIdentifier> artifactIdentificator,
      Function<T, String> assetLabeler,
      Collection<T> artifacts) {
    CompositeKnowledgeCarrier ckc =
        new CompositeKnowledgeCarrier().withStructType(CompositeStructType.SET);
    ParsingLevel level = ParsingLevelContrastor.detectLevel(rep);
    ckc.withRepresentation(rep);
    ckc.withLevel(level);

    // wrap the components into KnowledgeCarriers
    wrapComponents(rep, artifacts, level, assetIdentificator, artifactIdentificator, assetLabeler, ckc);

    // hash the (versioned) IDs of the components into an asset Id for the composite
    hashComponentIds(artifacts, assetIdentificator, ckc);

    // create a Struct in RDF
    List<Statement> structs =
        artifacts.stream()
            .map(assetIdentificator)
            .map(
                id ->
                    JenaUtil.objA(
                        ckc.getAssetId().getVersionId().toString(),
                        Has_Structural_Component.getReferentId().toString(),
                        id.getVersionId().toString()))
            .collect(Collectors.toList());
    ckc.withStruct(
        of(JenaUtil.fromStatementsToString(structs))
            .withAssetId(ckc.getAssetId())
            .withArtifactId(randomId())
            .withRepresentation(rep(OWL_2, Turtle, TXT)));

    return ckc;
  }

  /**
   * Creates a Composite Knowledge Carrier from a set of "homogeneous" Knowledge Artifacts
   * that share the same representation, with a tree structure.
   *
   * @param rep The common representation
   * @param artifacts The artifacts to be aggregated into the composite
   * @param assetIdentificator A function that allows to extract an (asset) ID from each of the artifacts
   * @param assetIdentificator A function that allows to extract an (artifact) ID from each of the artifacts
   * @param <T> The common type of the artifacts
   * @return A set-oriented Composite Knowledge Carrier
   */
  static <T> CompositeKnowledgeCarrier ofIdentifiableTree(
      SyntacticRepresentation rep,
      Function<T, ResourceIdentifier> assetIdentificator,
      Function<T, ResourceIdentifier> artifactidentificator,
      Function<T, String> assetLabeler,
      Function<T, Collection<? extends Link>> visitor,
      ResourceIdentifier rootId,
      Map<SemanticIdentifier, T> artifacts) {
    CompositeKnowledgeCarrier ckc =
        new CompositeKnowledgeCarrier().withStructType(CompositeStructType.TREE);
    ParsingLevel level = ParsingLevelContrastor.detectLevel(rep);
    ckc.withRepresentation(rep);
    ckc.withLevel(level);

    // wrap the components into KnowledgeCarriers
    wrapComponents(rep, artifacts.values(), level, assetIdentificator, artifactidentificator, assetLabeler, ckc);

    // hash the (versioned) IDs of the components into an asset Id for the composite
    hashComponentIds(artifacts.values(), assetIdentificator, ckc);

    // create a Struct for Structural Components
    List<Statement> structs =
        ckc.getComponent().stream()
            .map(
                component ->
                    JenaUtil.objA(
                        ckc.getAssetId().getVersionId().toString(),
                        Has_Structural_Component.getReferentId().toString(),
                        component.getAssetId().getVersionId().toString()))
            .collect(Collectors.toList());

    // Add Struct for relationships between Components
    visitor.apply(artifacts.get(rootId)).stream()
        .forEach(
            childLink -> {
              URI rel = childLink.getRel().getReferentId();
              URI versionId = childLink.getHrefVersionURI();
              structs.add(
                  JenaUtil.objA(
                      rootId.getVersionId().toString(), rel.toString(), versionId.toString()));
            });
    // Create String and set Struct
    String structString = JenaUtil.fromStatementsToString(structs);
    ckc.withStruct(
        of(structString)
            .withAssetId(ckc.getAssetId())
            .withArtifactId(randomId())
            .withRepresentation(rep(OWL_2, Turtle, TXT)));
    // Set root ID
    ckc.setRootId(rootId);

    return ckc;
  }

  static <T> void wrapComponents(
      SyntacticRepresentation rep,
      Collection<T> artifacts,
      ParsingLevel level,
      Function<T, ResourceIdentifier> assetIdentificator,
      Function<T, ResourceIdentifier> artifactIdentificator,
      Function<T, String> assetLabeler,
      CompositeKnowledgeCarrier ckc) {

    artifacts.stream()
        .map(
            x ->
                of(x, level)
                    .withRepresentation(rep)
                    .withAssetId(assetIdentificator.apply(x))
                    .withArtifactId(artifactIdentificator.apply(x))
                    .withLabel(assetLabeler.apply(x)))
        .forEach(ckc.getComponent()::add);
  }


  static <T> void hashComponentIds(
      Collection<T> artifacts,
      Function<T, ResourceIdentifier> assetIdentificator,
      CompositeKnowledgeCarrier ckc) {
    ckc.withAssetId(
        artifacts.stream()
            .map(assetIdentificator)
            .reduce(SemanticIdentifier::hashIdentifiers)
            .map(
                compsId ->
                    hashIdentifiers(
                        compsId, newId(Util.uuid(CompositeStructType.TREE), VERSION_LATEST)))
            .orElseThrow(IllegalArgumentException::new));
  }


  /**
   * Creates an Anonymous Composite Knowledge Carrier from a set of "homogeneous" Knowledge Artifacts
   * that share the same representation
   *
   * @param rep The common representation
   * @param artifacts The artifacts to be aggregated into the composite
   * @param assetIdentificator A function that allows to extract an (asset) ID from each of the artifacts
   * @param assetIdentificator A function that allows to extract an (artifact) ID from each of the artifacts
   * @param <T> The common type of the artifacts
   * @return An Anonymous Composite Knowledge Carrier
   */
  static <T> CompositeKnowledgeCarrier ofAnonymousComposite(
      SyntacticRepresentation rep,
      Function<T, ResourceIdentifier> assetIdentificator,
      Function<T, ResourceIdentifier> artifactidentificator,
      Collection<T> artifacts) {
    CompositeKnowledgeCarrier ckc = new CompositeKnowledgeCarrier();
    ParsingLevel level = ParsingLevelContrastor.detectLevel(rep);

    // wrap the components into KnowledgeCarriers
    artifacts.stream()
        .map(x -> of(x,level)
            .withRepresentation(rep)
            .withAssetId(assetIdentificator.apply(x))
            .withArtifactId(artifactidentificator.apply(x)))
        .forEach(ckc.getComponent()::add);

    ckc.withAssetId(randomId());
    ckc.withRepresentation(rep);
    ckc.withLevel(level);
    ckc.withStructType(NONE);
    // struct and type are left 'null' - may need to revisit this decision
    return ckc;
  }

  /**
   * Creates an Anonymous Composite Knowledge Carrier from a set of "heterogeneous"
   * Knowledge Artifacts
   *
   * @param artifacts The artifacts to be aggregated into the composite
   * @return An Anonymous Composite Knowledge Carrier
   */
  static CompositeKnowledgeCarrier ofHeterogeneousComposite(
      Collection<KnowledgeCarrier> artifacts) {
    CompositeKnowledgeCarrier ckc = new CompositeKnowledgeCarrier()
        .withComponent(artifacts);

    // hash the (versioned) IDs of the components into an asset Id for the composite
    ckc.withAssetId(randomId());
    ckc.withArtifactId(randomId());
    ckc.withStructType(NONE);
    return ckc;
  }


  default KnowledgeCarrier mainComponent() {
    if (this instanceof CompositeKnowledgeCarrier) {
      CompositeKnowledgeCarrier ckc = (CompositeKnowledgeCarrier) this;
      if (ckc.getRootId() != null) {
        KeyIdentifier key = ckc.getRootId().asKey();
        return ckc.getComponent().stream()
            .filter(kc -> kc.getAssetId() != null)
            .filter(kc -> kc.getAssetId().asKey().equals(key))
            .findFirst()
            .orElseThrow(IllegalStateException::new);
      }
    }
    return (KnowledgeCarrier) this;
  }

  default <T> T mainComponentAs(Class<T> klass) {
    return mainComponent()
        .as(klass)
        .orElseThrow(IllegalStateException::new);
  }

  default <T> Stream<T> componentsAs(Class<T> klass) {
    if (this instanceof CompositeKnowledgeCarrier) {
      return ((CompositeKnowledgeCarrier) this).getComponent().stream()
          .map(kc -> kc.as(klass))
          .flatMap(StreamUtil::trimStream);
    } else {
      return Stream.of(mainComponentAs(klass));
    }
  }

  default <T> Optional<T> componentAs(SemanticIdentifier id, Class<T> klass) {
    if (this instanceof CompositeKnowledgeCarrier) {
      return ((CompositeKnowledgeCarrier) this).getComponent().stream()
          .filter(kc -> kc.getAssetId().asKey().equals(id.asKey()))
          .findFirst()
          .flatMap(kc -> kc.as(klass));
    } else {
      return this.as(klass);
    }
  }

  default Stream<KnowledgeCarrier> components() {
    if (this instanceof CompositeKnowledgeCarrier) {
      return ((CompositeKnowledgeCarrier) this).getComponent().stream();
    } else {
      return Stream.of((KnowledgeCarrier)this);
    }
  }

  default List<KnowledgeCarrier> componentList() {
    return components().collect(Collectors.toList());
  }


  static SyntacticRepresentation rep(SyntacticRepresentation src) {
    SyntacticRepresentation rep = new org.omg.spec.api4kp._20200801.services.SyntacticRepresentation();
    src.copyTo(rep);
    return rep;
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language) {
    return rep(language, null, null, null, null);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language, Lexicon... vocabs) {
    return rep(language, null, null, null, null)
        .withLexicon(vocabs);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization serialization) {
    return rep(language, serialization, null, null, null);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      SerializationFormat format) {
    return rep(language, format, null, null);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      SerializationFormat format, Lexicon... vocabs) {
    return rep(language, format, null, null)
        .withLexicon(vocabs);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format) {
    return rep(language, serialization, format, null, null);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization ser,
      SerializationFormat format,
      Charset charset) {
    return rep(language, ser, format, charset, null);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      SerializationFormat format,
      Charset charset) {
    return rep(language, format, charset, null);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      SerializationFormat format,
      Charset charset,
      Encodings encoding) {
    return rep(language, null, format, charset, encoding);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format,
      Charset charset,
      Encodings encoding) {
    return rep(language, null, serialization, format, charset, encoding);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile) {
    return rep(language, profile, null, null, null, null);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      KnowledgeRepresentationLanguageSerialization serialization) {
    return rep(language, profile, serialization, null, null, null);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format) {
    return rep(language, profile, serialization, format, null, null);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format,
      Charset charset,
      Encodings encoding) {
    return rep(language, profile, serialization, format, charset, encoding, new Lexicon[] {});
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      SerializationFormat format,
      Charset charset) {
    return rep(language, profile, null, format, charset, null, new Lexicon[] {});
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format,
      Charset charset,
      Encodings encoding,
      Lexicon... lexicons) {
    return rep(language, profile, serialization, format, charset, encoding,
        Arrays.asList(lexicons));
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format,
      Charset charset,
      Encodings encoding,
      Collection<Lexicon> lexicons) {
    return new SyntacticRepresentation()
        .withLanguage(language)
        .withProfile(profile)
        .withSerialization(serialization)
        .withFormat(format)
        .withCharset(charset != null ? charset.name() : null)
        .withEncoding(encoding != null ? encoding.name() : null)
        .withLexicon(lexicons);
  }

  static SyntacticRepresentation rep(SerializationFormat format, Charset charset,
      Encodings encoding) {
    return rep(null, format, charset, encoding);
  }

  static SyntacticRepresentation rep(Charset charset, Encodings encoding) {
    return rep(null, null, charset, encoding);
  }

  static SyntacticRepresentation rep(Encodings encoding) {
    return rep(null, null, null, null, encoding);
  }


  static String codedRep(SyntacticRepresentation src) {
    SyntacticRepresentation rep = new org.omg.spec.api4kp._20200801.services.SyntacticRepresentation();
    src.copyTo(rep);
    return ModelMIMECoder.encode(rep);
  }

  static String codedRep(KnowledgeRepresentationLanguage language) {
    return codedRep(language, null, null, null, null);
  }

  static String codedRep(KnowledgeRepresentationLanguage language, Lexicon... vocabs) {
    return ModelMIMECoder.encode(
        rep(language, null, null, null, null)
            .withLexicon(vocabs));
  }

  static String codedRep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization serialization) {
    return codedRep(language, serialization, null, null, null);
  }

  static String codedRep(KnowledgeRepresentationLanguage language,
      SerializationFormat format) {
    return codedRep(language, format, null, null);
  }

  static String codedRep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format) {
    return codedRep(language, serialization, format, null, null);
  }

  static String codedRep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization ser,
      SerializationFormat format,
      Charset charset) {
    return codedRep(language, ser, format, charset, null);
  }

  static String codedRep(KnowledgeRepresentationLanguage language,
      SerializationFormat format,
      Charset charset) {
    return codedRep(language, format, charset, null);
  }

  static String codedRep(KnowledgeRepresentationLanguage language,
      SerializationFormat format,
      Charset charset,
      Encodings encoding) {
    return codedRep(language, null, format, charset, encoding);
  }

  static String codedRep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format,
      Charset charset,
      Encodings encoding) {
    return codedRep(language, null, serialization, format, charset, encoding);
  }

  static String codedRep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format) {
    return codedRep(language, profile, serialization, format, null, null);
  }

  static String codedRep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format,
      Charset charset,
      Encodings encoding) {
    return ModelMIMECoder.encode(
        rep(language,profile,serialization,format,charset, encoding));
  }

  static String codedRep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      SerializationFormat format,
      Charset charset) {
    return ModelMIMECoder.encode(rep(language,profile,format,charset));
  }

  static String codedRep(SerializationFormat format, Charset charset,
      Encodings encoding) {
    return codedRep(null, format, charset, encoding);
  }

  static String codedRep(Charset charset, Encodings encoding) {
    return codedRep(null, null, charset, encoding);
  }

  static String codedRep(Encodings encoding) {
    return codedRep(null, null, null, null, encoding);
  }


  default <T> Optional<T> as(Class<T> type) {
    return type.isInstance(getExpression())
            ? Optional.ofNullable(type.cast(getExpression()))
            : Optional.empty();
  }

  default <T> boolean is(Class<T> type) {
    return type.isInstance(getExpression());
  }

  /**
   * Attempts to return a String representation of the carried Knowledge.
   *  - Returns the Artifact if already encoded or serialized.
   *  - Attempts to serialize JSON and XML based parse trees
   *  - Other Parse Trees are not supported at the moment.
   *
   * General AST and ASG are excluded since they may have graph nature, which may lead
   * to infinite recursion when trying to construct a String.
   * Consider using 'lowering' operations to ensure success before invoking this method.
   * @return An optional 'toString' representation of the carried Knowledge Artifact
   */
  default Optional<String> asString() {
    if (this.getExpression() == null) {
      return Optional.empty();
    }
    if (this.getExpression() instanceof byte[]) {
      return Optional.of(new String((byte[]) this.getExpression()));
    }
    if (this.getExpression() instanceof Document) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      XMLUtil.streamXMLNode((Document) this.getExpression(), baos);
      return Optional.of(new String(baos.toByteArray()));
    }
    if (this.getExpression() instanceof JsonNode) {
      return JSonUtil.writeJsonAsString(this.getExpression());
    }
    if (this.getExpression() != null) {
      return Optional.ofNullable(this.getExpression().toString());
    }
    return Optional.empty();
  }

  default Optional<byte[]> asBinary() {
    return asString().map(String::getBytes);
  }

  Object getExpression();
}
