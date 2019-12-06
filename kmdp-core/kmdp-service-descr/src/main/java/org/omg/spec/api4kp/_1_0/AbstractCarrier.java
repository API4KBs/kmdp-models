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

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.SurrogateHelper;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerialization;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;
import org.jvnet.jaxb2_commons.lang.CopyStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.w3c.dom.Document;

public class AbstractCarrier {

  public static KnowledgeCarrier ofNaturalLanguageRep(String s) {
    return new org.omg.spec.api4kp._1_0.services.resources.ExpressionCarrier()
        .withSerializedExpression(s)
        .withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression)
        .withRepresentation(
            // ADD "Natural Language" to the list of languages
            rep(KnowledgeRepresentationLanguageSeries.HTML,
                SerializationFormatSeries.TXT));
  }

  public static KnowledgeCarrier of(byte[] encoded) {
    return new org.omg.spec.api4kp._1_0.services.resources.BinaryCarrier()
        .withEncodedExpression(encoded)
        .withLevel(ParsingLevelSeries.Encoded_Knowledge_Expression);
  }

  public static KnowledgeCarrier of(InputStream stream) {
    return new org.omg.spec.api4kp._1_0.services.resources.BinaryCarrier()
        .withEncodedExpression(FileUtil.readBytes(stream).orElse(new byte[0]))
        .withLevel(ParsingLevelSeries.Encoded_Knowledge_Expression);
  }

  public static KnowledgeCarrier of(String serialized) {
    return new org.omg.spec.api4kp._1_0.services.resources.ExpressionCarrier()
        .withSerializedExpression(serialized)
        .withLevel(ParsingLevelSeries.Concrete_Knowledge_Expression);
  }

  public static KnowledgeCarrier of(Document dox) {
    return new org.omg.spec.api4kp._1_0.services.resources.DocumentCarrier()
        .withStructuredExpression(dox)
        .withLevel(ParsingLevelSeries.Parsed_Knowedge_Expression);
  }

  public static KnowledgeCarrier of(JsonNode jdox) {
    return new org.omg.spec.api4kp._1_0.services.resources.DocumentCarrier()
        .withStructuredExpression(jdox)
        .withLevel(ParsingLevelSeries.Parsed_Knowedge_Expression);
  }

  public static KnowledgeCarrier ofAst(Object ast) {
    return new ASTCarrier().withParsedExpression(ast)
        .withLevel(ParsingLevelSeries.Abstract_Knowledge_Expression);
  }

  public static KnowledgeCarrier of(byte[] encoded, SyntacticRepresentation rep) {
    return of(encoded)
        .withRepresentation(rep);
  }

  public static KnowledgeCarrier of(InputStream stream, SyntacticRepresentation rep) {
    return of(stream)
        .withRepresentation(rep);
  }

  public static KnowledgeCarrier of(String serialized, SyntacticRepresentation rep) {
    return of(serialized)
        .withRepresentation(rep);
  }

  public static KnowledgeCarrier of(Document dox, SyntacticRepresentation rep) {
    return of(dox)
        .withRepresentation(rep);
  }

  public static KnowledgeCarrier of(JsonNode jdox, SyntacticRepresentation rep) {
    return of(jdox)
        .withRepresentation(rep);
  }

  public static KnowledgeCarrier ofAst(Object ast, SyntacticRepresentation rep) {
    return ofAst(ast)
        .withRepresentation(rep);
  }

  public static SyntacticRepresentation rep(SyntacticRepresentation src) {
    SyntacticRepresentation rep = new org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation();
    src.copyTo(rep);
    return rep;
  }

  public static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language) {
    return rep(language, null, null, null, null);
  }

  public static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization serialization) {
    return rep(language, serialization, null, null, null);
  }

  public static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      SerializationFormat format) {
    return rep(language, format, null, null);
  }

  public static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format) {
    return rep(language, serialization, format, null, null);
  }

  // Should the object be unified?
  public static SyntacticRepresentation rep(Representation meta) {
    return rep(meta.getLanguage(), meta.getSerialization(), meta.getFormat(), null, null);
  }

  public static SyntacticRepresentation canonicalRepresentationOf(KnowledgeAsset asset) {
    return rep(SurrogateHelper.canonicalRepresentationOf(asset));
  }


  public static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization ser,
      SerializationFormat format,
      String charset) {
    return rep(language, ser, format, charset, null);
  }

  public static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      SerializationFormat format,
      String charset) {
    return rep(language, format, charset, null);
  }

  public static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      SerializationFormat format,
      String charset,
      String encoding) {
    return rep(language, null, format, charset, encoding);
  }

  public static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format,
      String charset,
      String encoding) {
    return rep(language, null, serialization, format, charset, encoding);
  }

  public static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format) {
    return rep(language, profile, serialization, format, null, null);
  }

  public static SyntacticRepresentation rep(KnowledgeRepresentationLanguage language,
      KnowledgeRepresentationLanguageProfile profile,
      KnowledgeRepresentationLanguageSerialization serialization,
      SerializationFormat format,
      String charset,
      String encoding) {
    return new org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation()
        .withLanguage(language)
        .withProfile(profile)
        .withSerialization(serialization)
        .withFormat(format)
        .withCharset(charset)
        .withEncoding(encoding);
  }

  public static SyntacticRepresentation rep(SerializationFormat format, String charset,
      String encoding) {
    return rep(null, format, charset, encoding);
  }

  public static SyntacticRepresentation rep(String charset, String encoding) {
    return rep(null, null, charset, encoding);
  }

  public static SyntacticRepresentation rep(String encoding) {
    return rep(null, null, null, null, encoding);
  }

  protected Object copyTo(ObjectLocator locator, Object target, CopyStrategy strategy) {
    return target != null && locator != null && strategy != null
        ? target
        : null ;
  }

  public <T> Optional<T> as(Class<T> type) {
    return
        (this instanceof ASTCarrier
            && type.isInstance(((ASTCarrier) this).getParsedExpression()))
            ? Optional.ofNullable(type.cast(((ASTCarrier) this).getParsedExpression()))
            : Optional.empty();
  }


  public <T> Optional<T> asParseTree(Class<T> type) {
    return
        (this instanceof DocumentCarrier
            && type.isInstance(((DocumentCarrier) this).getStructuredExpression()))
            ? Optional.ofNullable(type.cast(((DocumentCarrier) this).getStructuredExpression()))
            : Optional.empty();
  }



  /**
   * @deprecated until reworked
   * @param mapper
   * @param <U>
   * @return the result of the function application
   */
  // Rewrite as proper map/flatMap
  @Deprecated
  public <U> U flatMap(Function<? super KnowledgeCarrier, U> mapper) {
    return mapper.apply((KnowledgeCarrier) this);
  }

  /**
   * @deprecated until reworked
   * @param mapper
   * @param <U>
   * @return the result of the function mapping
   */
  // Rewrite as proper map/flatMap
  @Deprecated
  public <U extends KnowledgeCarrier> U map(Function<? super AbstractCarrier, U> mapper) {
    return mapper.apply(this);
  }

}
