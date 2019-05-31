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
import edu.mayo.ontology.taxonomies.krprofile._2018._08.KnowledgeRepresentationLanguageProfile;

public class ProfileContrastor extends Contrastor<KnowledgeRepresentationLanguageProfile> {

  public static ProfileContrastor profileContrastor = new ProfileContrastor();

  protected ProfileContrastor() {
  }

  @Override
  public boolean comparable(KnowledgeRepresentationLanguageProfile first, KnowledgeRepresentationLanguageProfile second) {
    // TODO need to consult the profile lattice (for a given language)
    return first != null && first == second;
  }

  @Override
  public int compare(KnowledgeRepresentationLanguageProfile o1, KnowledgeRepresentationLanguageProfile o2) {
    // this gets invoked only if the profiles are comparable, i.e. if they are the same
    return 0;
  }
}