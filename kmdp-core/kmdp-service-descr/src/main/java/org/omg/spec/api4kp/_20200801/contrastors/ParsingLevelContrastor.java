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
package org.omg.spec.api4kp._20200801.contrastors;

import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Concrete_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;

import edu.mayo.kmdp.comparator.Contrastor;
import java.util.Comparator;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevel;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries;

public class ParsingLevelContrastor extends Contrastor<ParsingLevel> implements
    Comparator<ParsingLevel> {

  private ParsingLevelContrastor() {

  }

  public static final ParsingLevelContrastor theLevelContrastor = new ParsingLevelContrastor();

  public static ParsingLevel detectLevel(KnowledgeCarrier carrier) {
    if (carrier.getLevel() != null) {
      return carrier.getLevel();
    }
    SyntacticRepresentation rep = carrier.getRepresentation();
    if (rep == null) {
      return Knowledge_Expression;
    }

    if (rep.getEncoding() != null) {
      return Encoded_Knowledge_Expression;
    }
    if (rep.getCharset() != null) {
      return Serialized_Knowledge_Expression;
    }
    if (rep.getFormat() != null || rep.getSerialization() != null) {
      return Concrete_Knowledge_Expression;
    }
    if (rep.getLanguage() != null) {
      return Abstract_Knowledge_Expression;
    }
    return Knowledge_Expression;
  }

  public static ParsingLevel detectLevel(SyntacticRepresentation rep) {
    if (rep == null) {
      return Knowledge_Expression;
    }

    if (rep.getEncoding() != null) {
      return Encoded_Knowledge_Expression;
    }
    if (rep.getCharset() != null) {
      return Serialized_Knowledge_Expression;
    }
    if (rep.getFormat() != null || rep.getSerialization() != null) {
      return Concrete_Knowledge_Expression;
    }
    if (rep.getLanguage() != null) {
      return Abstract_Knowledge_Expression;
    }
    return Knowledge_Expression;
  }

  public int compare(ParsingLevel l1, ParsingLevel l2) {
    if (l1 == l2 || l1.sameAs(l2) || l1.asEnum() == Knowledge_Expression
        || l2.asEnum() == Knowledge_Expression) {
      return 0;
    }
    switch (l1.asEnum()) {
      case Abstract_Knowledge_Expression:
        return 1;
      case Concrete_Knowledge_Expression:
        return l2.asEnum() == Abstract_Knowledge_Expression ? -1 : 1;
      case Serialized_Knowledge_Expression:
        return l2.asEnum() == Encoded_Knowledge_Expression ? 1 : -1;
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

  public boolean isBroaderOrEqual(ParsingLevel r1, ParsingLevel r2) {
    return Contrastor.isBroaderOrEqual(theLevelContrastor.contrast(r1,r2));
  }
  public boolean isNarrowerOrEqual(ParsingLevel r1, ParsingLevel r2) {
    return Contrastor.isNarrowerOrEqual(theLevelContrastor.contrast(r1,r2));
  }

}
