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
package org.omg.spec.api4kp;

import edu.mayo.kmdp.terms.api4kp.parsinglevel._20190801.ParsingLevel;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.terms.krserialization._2018._08.KRSerialization;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class KnowledgeCarrierHelper {

  public static SyntacticRepresentation rep(KRLanguage language) {
    return rep(language, null, null, null);
  }

  public static SyntacticRepresentation rep(KRLanguage language, KRSerialization serialization) {
    return rep(language, serialization,null, null, null);
  }

  public static SyntacticRepresentation rep(KRLanguage language, KRFormat format) {
    return rep(language, format, null, null);
  }

  public static SyntacticRepresentation rep(KRLanguage language, KRSerialization serialization, KRFormat format) {
    return rep(language, serialization, format, null, null);
  }


  public static SyntacticRepresentation rep(KRLanguage language, KRFormat format, String charset) {
    return rep(language, format, charset, null);
  }

  public static SyntacticRepresentation rep(KRLanguage language, KRFormat format, String charset,
      String encoding) {
    return rep(language,null,format,charset,encoding);
  }
  public static SyntacticRepresentation rep(KRLanguage language, KRSerialization serialization, KRFormat format, String charset,
      String encoding) {
    return new org.omg.spec.api4kp._1_0.services.resources.SyntacticRepresentation()
        .withLanguage(language)
        .withSerialization(serialization)
        .withFormat(format)
        .withCharset(charset)
        .withEncoding(encoding);
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


  public static ParsingLevel detectLevel(KnowledgeCarrier carrier) {
    if (carrier.getLevel() != null) {
      return carrier.getLevel();
    }
    SyntacticRepresentation rep = carrier.getRepresentation();
    if (rep == null) {
      return ParsingLevel.Knowledge_Expression;
    }
    if (rep.getEncoding() != null || carrier instanceof BinaryCarrier) {
      return ParsingLevel.Encoded_Knowledge_Expression;
    }
    if (rep.getCharset() != null || carrier instanceof ExpressionCarrier) {
      return ParsingLevel.Concrete_Knowledge_Expression;
    }
    if (rep.getFormat() != null || carrier instanceof DocumentCarrier) {
      return ParsingLevel.Parsed_Knowedge_Expression;
    }
    if (rep.getLanguage() != null || carrier instanceof ASTCarrier) {
      return ParsingLevel.Abstract_Knowledge_Expression;
    }
    return ParsingLevel.Knowledge_Expression;
  }

  public static int compare(ParsingLevel l1, ParsingLevel l2) {
    if (l1 == l2 || l1 == ParsingLevel.Knowledge_Expression
        || l2 == ParsingLevel.Knowledge_Expression) {
      return 0;
    }
    switch (l1) {
      case Abstract_Knowledge_Expression:
        return 1;
      case Parsed_Knowedge_Expression:
        return l2 == ParsingLevel.Abstract_Knowledge_Expression ? -1 : 1;
      case Concrete_Knowledge_Expression:
        return l2 == ParsingLevel.Encoded_Knowledge_Expression ? 1 : -1;
      case Encoded_Knowledge_Expression:
        return -1;
    }
    return 1;
  }

}
