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

import static org.omg.spec.api4kp._20200801.id.SemanticIdentifier.randomId;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.asEnum;

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.services.transrepresentation.ModelMIMECoder;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfile;
import org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerialization;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.Lexicon;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevel;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries;
import org.w3c.dom.Document;

public interface AbstractCarrier {

  enum Encodings {
    DEFAULT;
  }

  static KnowledgeCarrier of(byte[] encoded) {
    return new org.omg.spec.api4kp._20200801.services.KnowledgeCarrier()
        .withAssetId(randomId())
        .withExpression(encoded)
        .withLevel(Encoded_Knowledge_Expression);
  }

  static KnowledgeCarrier of(InputStream stream) {
    return new org.omg.spec.api4kp._20200801.services.KnowledgeCarrier()
        .withAssetId(randomId())
        .withExpression(FileUtil.readBytes(stream).orElse(new byte[0]))
        .withLevel(Encoded_Knowledge_Expression);
  }

  static KnowledgeCarrier of(String serialized) {
    return new org.omg.spec.api4kp._20200801.services.KnowledgeCarrier()
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
    return new org.omg.spec.api4kp._20200801.services.KnowledgeCarrier()
        .withAssetId(randomId())
        .withExpression(parseTree)
        .withLevel(Concrete_Knowledge_Expression);
  }

  static KnowledgeCarrier ofAst(Object ast) {
    return new org.omg.spec.api4kp._20200801.services.KnowledgeCarrier()
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

  default Optional<KnowledgeCarrier> tryMainComponent() {
    if (this instanceof CompositeKnowledgeCarrier) {
      CompositeKnowledgeCarrier ckc = (CompositeKnowledgeCarrier) this;
      return componentById(ckc.getRootId(), ckc)
          .or(() -> componentById(ckc.getAssetId(), ckc));
    }
    return Optional.of((KnowledgeCarrier) this);
  }

  default Optional<KnowledgeCarrier> componentById(
      ResourceIdentifier id, CompositeKnowledgeCarrier ckc) {
    return Optional.ofNullable(id)
        .map(SemanticIdentifier::asKey)
        .flatMap(key -> ckc.getComponent().stream()
            .filter(kc -> kc.getAssetId() != null)
            .filter(kc -> kc.getAssetId().asKey().equals(key))
            .findFirst());
  }

  default KnowledgeCarrier mainComponent() {
    return tryMainComponent()
        .orElseThrow(IllegalStateException::new);
  }

  default <T> T mainComponentAs(Class<T> klass) {
    return mainComponent()
        .as(klass)
        .orElseThrow(IllegalStateException::new);
  }

  default <T> Optional<T> tryMainComponentAs(Class<T> klass) {
    return tryMainComponent()
        .flatMap(kc -> kc.as(klass));
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
    var rep = new org.omg.spec.api4kp._20200801.services.SyntacticRepresentation();
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
    var rep = new org.omg.spec.api4kp._20200801.services.SyntacticRepresentation();
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
      var baos = new ByteArrayOutputStream();
      XMLUtil.streamXMLNode((Document) this.getExpression(), baos);
      return Optional.of(baos.toString());
    }
    if (this.getExpression() instanceof JsonNode) {
      return JSonUtil.writeJsonAsString(this.getExpression());
    }
    if (this.getExpression() instanceof String) {
      String s = (String) this.getExpression();
      if (Encoded_Knowledge_Expression.sameAs(this.getLevel())) {
        s = new String(Base64.getDecoder().decode(s));
      }
      return Optional.ofNullable(s);
    }
    return Optional.of(this.getExpression().toString());
  }

  default Optional<byte[]> asBinary() {
    if (this.getExpression() == null) {
      return Optional.empty();
    }
    if (this.getExpression() instanceof byte[]) {
      return Optional.of((byte[]) this.getExpression());
    }
    if (this.getExpression() instanceof String) {
      switch (ParsingLevelSeries.asEnum(getLevel())) {
        case Encoded_Knowledge_Expression:
          return Optional.ofNullable(Base64.getDecoder().decode((String) getExpression()));
        case Serialized_Knowledge_Expression:
          return Optional.of(((String) getExpression()).getBytes());
        default:
      }
    }
    throw new IllegalStateException("Unexpected String Expression at level " + getLevel().getLabel());
  }

  Object getExpression();

  ParsingLevel getLevel();
}
