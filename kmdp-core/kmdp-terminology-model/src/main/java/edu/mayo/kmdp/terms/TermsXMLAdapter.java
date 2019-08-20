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
package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.id.Term;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

public abstract class TermsXMLAdapter extends
    javax.xml.bind.annotation.adapters.XmlAdapter<org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier, Term> {

  @Override
  public Term unmarshal(org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier v) {
    return java.util.Arrays.stream(getValues())
        .filter(x -> x.getRef().equals(v.getRef()))
        .findFirst().orElse(null);
  }

  @Override
  public org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier marshal(Term v) {
    if (v == null) {
      return null;
    }
    return new org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier()
        .withRef(v.getRef())
        .withLabel(v.getLabel())
        .withTag(v.getTag())
        .withConceptId(v.getConceptId())
        .withNamespace((NamespaceIdentifier) v.getNamespace());
  }

  protected abstract Term[] getValues();
}