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
package org.omg.spec.api4kp._1_0;

import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.Turtle;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.VERSION_LATEST;
import static org.omg.spec.api4kp._1_0.id.SemanticIdentifier.newId;

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.SurrogateHelper;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.lexicon.Lexicon;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.omg.spec.api4kp._1_0.contrastors.ParsingLevelContrastor;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.CompositeStructType;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.w3c.dom.Document;

public interface AbstractCarrier {

  //TODO This will become a controlled term in a future release of the ontologies (version 8+)
  String HAS_MEMBER = "<https://www.omg.org/spec/LCC/Languages/LanguageRepresentation/hasMember>";

  static KnowledgeCarrier of(byte[] encoded) {
    return new org.omg.spec.api4kp._1_0.services.resources.BinaryCarrier()
        .withEncodedExpression(encoded)
        .withLevel(ParsingLevelSeries.Encoded_Knowledge_Expression);
  }

  static KnowledgeCarrier of(InputStream stream) {
    return new org.omg.spec.api4kp._1_0.services.resources.BinaryCarrier()
        .withEncodedExpression(FileUtil.readBytes(stream).orElse(new byte[0]))
        .withLevel(ParsingLevelSeries.Encoded_Knowledge_Expression);
  }

  static KnowledgeCarrier of(String serialized) {
    return new org.omg.spec.api4kp._1_0.services.resources.ExpressionCarrier()
        .withSerializedExpression(serialized)
        .withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression);
  }

  static KnowledgeCarrier of(Document dox) {
    return ofTree(dox);
  }

  static KnowledgeCarrier of(JsonNode jdox) {
    return ofTree(jdox);
  }

  static KnowledgeCarrier ofTree(Object parseTree) {
    return new org.omg.spec.api4kp._1_0.services.resources.DocumentCarrier()
        .withStructuredExpression(parseTree)
        .withLevel(ParsingLevelSeries.Parsed_Knowedge_Expression);
  }

  static KnowledgeCarrier ofAst(Object ast) {
    return new ASTCarrier().withParsedExpression(ast)
        .withLevel(ParsingLevelSeries.Abstract_Knowledge_Expression);
  }

  static KnowledgeCarrier of(byte[] encoded, SyntacticRepresentation rep) {
    return of(encoded)
        .withRepresentation(rep);
  }

  static KnowledgeCarrier of(InputStream stream, SyntacticRepresentation rep) {
    return of(stream)
        .withRepresentation(rep);
  }

  static KnowledgeCarrier of(String serialized, SyntacticRepresentation rep) {
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

  /**
   * Constructs the appropriate type of KnowledgeCarrier for the
   * given artifact at the given abstraction level
   * @param artifact the artifact to be wrapped
   * @param level the parsing level of the artifact
   * @return a KnowledgeCarrier that wraps the artifact
   */
  static KnowledgeCarrier of(Object artifact, ParsingLevel level) {
    switch (level.asEnum()) {
      case Parsed_Knowedge_Expression:
        return ofTree(artifact)
            .withLevel(level);
      case Concrete_Knowledge_Expression:
        return of(artifact.toString())
            .withLevel(level);
      case Encoded_Knowledge_Expression:
        return of((byte[]) artifact)
            .withLevel(level);
      case Abstract_Knowledge_Expression:
        return ofAst(artifact)
            .withLevel(level);
      case Knowledge_Asset_Surogate:
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
  static <T> KnowledgeCarrier ofSet(SyntacticRepresentation rep, Collection<T> artifacts) {
    return ofIdentiableSet(rep,
        x -> newId(UUID.randomUUID()),
        artifacts);
  }

  /**
   * Creates a Composite Knowledge Carrier from a set of "homogeneous" Knowledge Artifacts
   * that share the same representation, with a set-oriented structure - i.e. a structure.
   * where each one of the artifacts is a member with no explicit order or function.
   *
   * @param rep The common representation
   * @param artifacts The artifacts to be aggregated into the composite
   * @param identificator A function that allows to extract an (asset) ID from each of the artifacts
   * @param <T> The common type of the artifacts
   * @return A set-oriented Composite Knowledge Carrier
   */
  static <T> KnowledgeCarrier ofIdentiableSet(
      SyntacticRepresentation rep,
      Function<T, ResourceIdentifier> identificator,
      Collection<T> artifacts) {
    CompositeKnowledgeCarrier ckc = new CompositeKnowledgeCarrier()
        .withStructType(CompositeStructType.SET);
    ParsingLevel level = ParsingLevelContrastor.detectLevel(rep);

    // wrap the components into KnowledgeCarriers
    artifacts.stream()
        .map(x -> of(x,level)
            .withRepresentation(rep)
            .withAssetId(identificator.apply(x))
            .withArtifactId(newId(UUID.randomUUID())))
        .forEach(ckc.getComponent()::add);

    // hash the (versioned) IDs of the components into an asset Id for the composite
    ckc.withAssetId(artifacts.stream()
        .map(identificator)
        .map(ResourceIdentifier::getVersionUuid)
        .reduce(Util::hashUUID)
        .map(uid -> SemanticIdentifier.newId(uid, VERSION_LATEST))
        .orElse(newId(UUID.randomUUID())));

    // random artifact Id
    ckc.withArtifactId(newId(UUID.randomUUID()));

    // create a Struct in RDF
    String struct = artifacts.stream()
        .map(identificator)
        .map(id -> new StringBuilder()
            .append("<").append(ckc.getAssetId().getResourceId()).append(">")
            .append(" ").append(HAS_MEMBER).append(" ")
            .append("<").append(id.getResourceId()).append(">")
            .append(".")
        ).collect(Collectors.joining("\n"));
    ckc.withStruct(of(struct)
        .withAssetId(ckc.getAssetId())
        .withRepresentation(rep(OWL_2,Turtle,TXT)));

    return ckc;
  }


  static SyntacticRepresentation rep(SyntacticRepresentation src) {
    SyntacticRepresentation rep = new org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation();
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
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format) {
    return rep(language, serialization, format, null, null);
  }

  // Should the object be unified?
  static SyntacticRepresentation rep(Representation meta) {
    return rep(meta.getLanguage(), meta.getSerialization(), meta.getFormat(), null, null);
  }

  static SyntacticRepresentation canonicalRepresentationOf(KnowledgeAsset asset) {
    return rep(SurrogateHelper.canonicalRepresentationOf(asset));
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
      String encoding) {
    return rep(language, null, format, charset, encoding);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format,
      Charset charset,
      String encoding) {
    return rep(language, null, serialization, format, charset, encoding);
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
      String encoding) {
    return new org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation()
        .withLanguage(language)
        .withProfile(profile)
        .withSerialization(serialization)
        .withFormat(format)
        .withCharset(charset != null ? charset.name() : null)
        .withEncoding(encoding);
  }

  static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      SerializationFormat format,
      Charset charset) {
    return new org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation()
        .withLanguage(language)
        .withProfile(profile)
        .withFormat(format)
        .withCharset(charset != null ? charset.name() : null);
  }

  static SyntacticRepresentation rep(SerializationFormat format, Charset charset,
      String encoding) {
    return rep(null, format, charset, encoding);
  }

  static SyntacticRepresentation rep(Charset charset, String encoding) {
    return rep(null, null, charset, encoding);
  }

  static SyntacticRepresentation rep(String encoding) {
    return rep(null, null, null, null, encoding);
  }


  default  <T> Optional<T> as(Class<T> type) {
    return
        (this instanceof ASTCarrier
            && type.isInstance(((ASTCarrier) this).getParsedExpression()))
            ? Optional.ofNullable(type.cast(((ASTCarrier) this).getParsedExpression()))
            : Optional.empty();
  }
  
  default  <T> Optional<T> asParseTree(Class<T> type) {
    return
        (this instanceof DocumentCarrier
            && type.isInstance(((DocumentCarrier) this).getStructuredExpression()))
            ? Optional.ofNullable(type.cast(((DocumentCarrier) this).getStructuredExpression()))
            : Optional.empty();
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
    if (this instanceof ExpressionCarrier) {
      return Optional.ofNullable(((ExpressionCarrier) this).getSerializedExpression());
    } else if (this instanceof BinaryCarrier) {
      return Optional.of(new String(((BinaryCarrier) this).getEncodedExpression()));
    } else if (this instanceof DocumentCarrier) {
      DocumentCarrier doc = (DocumentCarrier) this;
      if (doc.getStructuredExpression() instanceof Document) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.streamXMLNode((Document) doc.getStructuredExpression(), baos);
        return Optional.of(new String(baos.toByteArray()));
      } else if (doc.getStructuredExpression() instanceof JsonNode) {
        return JSonUtil.writeJsonAsString(doc.getStructuredExpression());
      }
    }
    return Optional.empty();
  }
}
