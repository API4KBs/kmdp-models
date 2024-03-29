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

import edu.mayo.kmdp.comparator.Contrastor;
import edu.mayo.kmdp.util.StreamUtil;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevel;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;

public class SyntacticRepresentationContrastor extends Contrastor<SyntacticRepresentation> {

  public static final SyntacticRepresentationContrastor theRepContrastor = new SyntacticRepresentationContrastor();

  protected SyntacticRepresentationContrastor() {
  }

  @Override
  public boolean comparable(SyntacticRepresentation sr1, SyntacticRepresentation sr2) {
    boolean sameLanguage = (sr1.getLanguage() == null && sr2.getLanguage() == null)
        || (sr1.getLanguage() != null && sr1.getLanguage().sameAs(sr2.getLanguage()));
    if (! sameLanguage) {
      return false;
    }
    Comparison profileComparison = ProfileContrastor.theProfileContrastor
        .contrast(sr1.getProfile(), sr2.getProfile());
    if (profileComparison == Comparison.DISTINCT || profileComparison == Comparison.INCOMPARABLE) {
      return false;
    }
    if (sr1.getSerialization() != null && sr2.getSerialization() != null
        && ! sr1.getSerialization().sameAs(sr2.getSerialization())) {
      return false;
    }
    if (sr1.getFormat() != null && sr2.getFormat() != null
        && ! sr1.getFormat().sameAs(sr2.getFormat())) {
      return false;
    }
    return sr1.getLexicon().isEmpty()
        || sr2.getLexicon().isEmpty()
        || StreamUtil.mapToSet(sr1.getLexicon(), ConceptTerm::getUuid)
        .equals(StreamUtil.mapToSet(sr2.getLexicon(), ConceptTerm::getUuid));
  }

  public int compare(SyntacticRepresentation sr1, SyntacticRepresentation sr2) {
    if (sr1.equals(sr2)) {
      return 0;
    }
    ParsingLevel p1 = ParsingLevelContrastor.detectLevel(sr1);
    ParsingLevel p2 = ParsingLevelContrastor.detectLevel(sr2);
    return ParsingLevelContrastor.theLevelContrastor.compare(p1,p2);
  }

  public boolean isBroaderOrEqual(SyntacticRepresentation r1, SyntacticRepresentation r2) {
    return Contrastor.isBroaderOrEqual(theRepContrastor.contrast(r1,r2));
  }
  public boolean isNarrowerOrEqual(SyntacticRepresentation r1, SyntacticRepresentation r2) {
    return Contrastor.isNarrowerOrEqual(theRepContrastor.contrast(r1,r2));
  }
  public boolean isEqual(SyntacticRepresentation r1, SyntacticRepresentation r2) {
    return Contrastor.isEqual(theRepContrastor.contrast(r1,r2));
  }

}
