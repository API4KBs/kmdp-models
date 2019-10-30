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
package org.omg.spec.api4kp._1_0.contrastors;

import edu.mayo.kmdp.comparator.Contrastor;
import edu.mayo.kmdp.series.Series;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevel;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import java.util.Comparator;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class ParsingLevelContrastor extends Contrastor<ParsingLevel> implements
    Comparator<ParsingLevel> {

  private ParsingLevelContrastor() {

  }

  public static final Contrastor<ParsingLevel> singleton = new ParsingLevelContrastor();

  public static ParsingLevel detectLevel(KnowledgeCarrier carrier) {
    if (carrier.getLevel() != null) {
      return carrier.getLevel();
    }
    SyntacticRepresentation rep = carrier.getRepresentation();
    if (rep == null) {
      return ParsingLevelSeries.Knowledge_Expression;
    }

    if (rep.getEncoding() != null || carrier instanceof BinaryCarrier) {
      return ParsingLevelSeries.Encoded_Knowledge_Expression;
    }
    if (rep.getCharset() != null || carrier instanceof ExpressionCarrier) {
      return ParsingLevelSeries.Concrete_Knowledge_Expression;
    }
    if (rep.getFormat() != null || rep.getSerialization() != null || carrier instanceof DocumentCarrier) {
      return ParsingLevelSeries.Parsed_Knowedge_Expression;
    }
    if (rep.getLanguage() != null || carrier instanceof ASTCarrier) {
      return ParsingLevelSeries.Abstract_Knowledge_Expression;
    }
    return ParsingLevelSeries.Knowledge_Expression;
  }

  public static ParsingLevel detectLevel(SyntacticRepresentation rep) {
    if (rep == null) {
      return ParsingLevelSeries.Knowledge_Expression;
    }

    if (rep.getEncoding() != null) {
      return ParsingLevelSeries.Encoded_Knowledge_Expression;
    }
    if (rep.getCharset() != null) {
      return ParsingLevelSeries.Concrete_Knowledge_Expression;
    }
    if (rep.getFormat() != null || rep.getSerialization() != null) {
      return ParsingLevelSeries.Parsed_Knowedge_Expression;
    }
    if (rep.getLanguage() != null) {
      return ParsingLevelSeries.Abstract_Knowledge_Expression;
    }
    return ParsingLevelSeries.Knowledge_Expression;
  }

  public int compare(ParsingLevel l1, ParsingLevel l2) {
    if (l1 == l2 || l1.asEnum() == ParsingLevelSeries.Knowledge_Expression
        || l2.asEnum() == ParsingLevelSeries.Knowledge_Expression) {
      return 0;
    }
    switch (l1.asEnum()) {
      case Abstract_Knowledge_Expression:
        return 1;
      case Parsed_Knowedge_Expression:
        return l2.asEnum() == ParsingLevelSeries.Abstract_Knowledge_Expression ? -1 : 1;
      case Concrete_Knowledge_Expression:
        return l2.asEnum() == ParsingLevelSeries.Encoded_Knowledge_Expression ? 1 : -1;
      case Encoded_Knowledge_Expression:
        return -1;
      default:
        return 1;
    }
  }

  @Override
  public boolean comparable(ParsingLevel first, ParsingLevel second) {
    return true;
  }
}
