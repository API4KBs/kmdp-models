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

import org.omg.spec.api4kp._20200801.id.Term;

/**
 * Copy of the official class for test purposes.
 * Duplicated in order to avoid a dependency on one additional module
 */
public abstract class MockTermsXMLAdapter extends
    javax.xml.bind.annotation.adapters.XmlAdapter<org.omg.spec.api4kp._20200801.id.ConceptIdentifier, Term> {

  @Override
  public Term unmarshal(org.omg.spec.api4kp._20200801.id.ConceptIdentifier v) {
    return java.util.Arrays.stream(getValues())
        .filter((x) -> x.getReferentId().equals(v.getReferentId()))
        .findFirst().orElse(null);
  }

  @Override
  public org.omg.spec.api4kp._20200801.id.ConceptIdentifier marshal(Term v) {
    return v != null ? v.asConceptIdentifier() : null;
  }

  protected abstract Term[] getValues();

}
