/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.surrogate;

import static edu.mayo.kmdp.surrogate.LegacyTermNamespaceMap.resolveKnownConceptByNamespace;

import edu.mayo.kmdp.SurrogateHelper;
import edu.mayo.kmdp.series.Series;
import org.omg.spec.api4kp._1_0.id.Term;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;

public class LegacyTermsXMLAdapter extends
    javax.xml.bind.annotation.adapters.XmlAdapter<ConceptIdentifier, Term> {

  @Override
  public Term unmarshal(ConceptIdentifier v) {
    if (v == null) {
      return null;
    }
    return resolveKnownConceptByNamespace(v.getConceptId(), v.getNamespace().getId(), v.getNamespace().getVersion())
        .orElseThrow(IllegalStateException::new);
  }

  @Override
  public ConceptIdentifier marshal(Term v) {
    if (v instanceof Series) {
      v = (Term) ((Series) v).getLatest();
    }
    return v != null
      ? SurrogateHelper.toLegacyConceptIdentifier(v.asConceptIdentifier())
        : null;
  }

}