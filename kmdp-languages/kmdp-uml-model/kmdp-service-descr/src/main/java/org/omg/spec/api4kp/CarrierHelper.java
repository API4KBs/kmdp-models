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
package org.omg.spec.api4kp;

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.common.model.ConceptIdentifier;
import edu.mayo.kmdp.common.model.DocumentCarrier;
import edu.mayo.kmdp.common.model.ExpressionCarrier;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import org.omg.spec.api4kp._1_0.Level;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.w3c.dom.Document;

public class CarrierHelper {


  public static KnowledgeCarrier of(byte[] encoded) {
    return new edu.mayo.kmdp.common.model.BinaryCarrier().withEncodedExpression(encoded);
  }

  public static KnowledgeCarrier of(String serialized) {
    return new ExpressionCarrier().withSerializedExpression(serialized);
  }

  public static KnowledgeCarrier of(Document dox) {
    return new DocumentCarrier().withStructuredExpression(dox);
  }

  public static KnowledgeCarrier of(JsonNode jdox) {
    return new DocumentCarrier().withStructuredExpression(jdox);
  }

  public static KnowledgeCarrier of(Object ast) {
    return new ASTCarrier().withParsedExpression(ast);
  }


  public static SyntacticRepresentation rep(KRLanguage language) {
    return rep(language, null, null, null);
  }


  public static SyntacticRepresentation rep(KRLanguage language, KRFormat format) {
    return rep(language, format, null, null);
  }


  public static SyntacticRepresentation rep(KRLanguage language, KRFormat format, String charset) {
    return rep(language, format, charset, null);
  }

  public static SyntacticRepresentation rep(KRLanguage language, KRFormat format, String charset,
      String encoding) {
    return new SyntacticRepresentation().withLanguage(language)
        .withFormat(format)
        .withCharset(charset != null ? new ConceptIdentifier().withTag(charset) : null)
        .withEncoding(encoding != null ? new ConceptIdentifier().withTag(encoding) : null);
  }

  public static SyntacticRepresentation rep(KRFormat format, String charset, String encoding) {
    return rep(null, format, charset, encoding);
  }

  public static SyntacticRepresentation rep(String charset, String encoding) {
    return rep(null, null, charset, encoding);
  }

  public static SyntacticRepresentation rep(String encoding) {
    return rep(null, null, null, encoding);
  }


  public static Level detectLevel(SyntacticRepresentation rep) {
    if (rep == null) {
      return Level.UNKNOWN;
    }
    if (rep.getEncoding() != null) {
      return Level.BINARY;
    }
    if (rep.getCharset() != null) {
      return Level.STRING;
    }
    if (rep.getFormat() != null) {
      return Level.PARSE_TREE;
    }
    if (rep.getLanguage() != null) {
      return Level.AST;
    }
    return Level.UNKNOWN;
  }

}
