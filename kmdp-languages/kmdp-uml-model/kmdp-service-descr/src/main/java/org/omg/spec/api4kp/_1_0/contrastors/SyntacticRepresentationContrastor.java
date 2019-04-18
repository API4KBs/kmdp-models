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

import static org.omg.spec.api4kp._1_0.contrastors.ParsingLevelContrastor.parsingLevelContrastor;
import static org.omg.spec.api4kp._1_0.contrastors.ProfileContrastor.profileContrastor;

import edu.mayo.kmdp.comparator.Contrastor;
import edu.mayo.kmdp.terms.api4kp.parsinglevel._20190801.ParsingLevel;
import java.util.HashSet;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class SyntacticRepresentationContrastor extends Contrastor<SyntacticRepresentation> {

  public static SyntacticRepresentationContrastor repContrastor = new SyntacticRepresentationContrastor();

  protected SyntacticRepresentationContrastor() {
  }

  @Override
  public boolean comparable(SyntacticRepresentation sr1, SyntacticRepresentation sr2) {
    if (sr1.getLanguage() != sr2.getLanguage()) {
      return false;
    }
    Comparison profileComparison = profileContrastor.contrast(sr1.getProfile(), sr2.getProfile());
    if (profileComparison == Comparison.DISTINCT || profileComparison == Comparison.INCOMPARABLE) {
      return false;
    }
    if (sr1.getSerialization() != null && sr2.getSerialization() != null
        && sr1.getSerialization() != sr2.getSerialization()) {
      return false;
    }
    if (sr1.getFormat() != null && sr2.getFormat() != null
        && sr1.getFormat() != sr2.getFormat()) {
      return false;
    }
    if (! (new HashSet<>(sr1.getLexicon()).equals(new HashSet<>(sr2.getLexicon())))) {
      return false;
    }
    return true;
  }

  public int compare(SyntacticRepresentation sr1, SyntacticRepresentation sr2) {
    if (sr1.equals(sr2)) {
      return 0;
    }
    ParsingLevel p1 = ParsingLevelContrastor.detectLevel(sr1);
    ParsingLevel p2 = ParsingLevelContrastor.detectLevel(sr2);
    return parsingLevelContrastor.compare(p1,p2);
  }



}
