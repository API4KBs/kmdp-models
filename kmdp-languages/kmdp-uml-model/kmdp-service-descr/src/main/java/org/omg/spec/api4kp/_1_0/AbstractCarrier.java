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
package org.omg.spec.api4kp._1_0;

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.ontology.taxonomies.krformat._2018._08.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krprofile._2018._08.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krserialization._2018._08.KnowledgeRepresentationLanguageSerialization;
import java.io.InputStream;
import java.util.function.Function;
import org.jvnet.jaxb2_commons.lang.CopyStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.w3c.dom.Document;

public class AbstractCarrier {

  public static KnowledgeCarrier of(byte[] encoded) {
    return new org.omg.spec.api4kp._1_0.services.resources.BinaryCarrier()
        .withEncodedExpression(encoded)
        .withLevel(ParsingLevel.Encoded_Knowledge_Expression);
  }

  public static KnowledgeCarrier of(InputStream stream) {
    return new org.omg.spec.api4kp._1_0.services.resources.BinaryCarrier()
        .withEncodedExpression(FileUtil.readBytes(stream).orElse(new byte[0]))
        .withLevel(ParsingLevel.Encoded_Knowledge_Expression);
  }

  public static KnowledgeCarrier of(String serialized) {
    return new org.omg.spec.api4kp._1_0.services.resources.ExpressionCarrier()
        .withSerializedExpression(serialized)
        .withLevel(ParsingLevel.Concrete_Knowledge_Expression);
  }

  public static KnowledgeCarrier of(Document dox) {
    return new org.omg.spec.api4kp._1_0.services.resources.DocumentCarrier()
        .withStructuredExpression(dox)
        .withLevel(ParsingLevel.Parsed_Knowedge_Expression);
  }

  public static KnowledgeCarrier of(JsonNode jdox) {
    return new org.omg.spec.api4kp._1_0.services.resources.DocumentCarrier()
        .withStructuredExpression(jdox)
        .withLevel(ParsingLevel.Parsed_Knowedge_Expression);
  }

  public static KnowledgeCarrier ofAst(Object ast) {
    return new ASTCarrier().withParsedExpression(ast)
        .withLevel(ParsingLevel.Abstract_Knowledge_Expression);
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


  //TODO
  @Deprecated
  public <U> U flatMap(Function<? super KnowledgeCarrier, U> mapper) {
    return mapper.apply((KnowledgeCarrier) this);
  }

  protected Object copyTo(ObjectLocator locator, Object target,
      CopyStrategy strategy) {
    return target;
  }
}
