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
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.util.Util;
import java.util.Arrays;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

public abstract class TermsXMLAdapter extends
    javax.xml.bind.annotation.adapters.XmlAdapter<org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier, Term> {

  @Override
  public Term unmarshal(org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier v) {
    return DatatypeHelper.resolveTerm(v.getTag(),getValues(v.getNamespace()),Term::getTag)
        .orElse(null);
  }

  @Override
  public org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier marshal(Term v) {
    return DatatypeHelper.toConceptIdentifier(v);
  }

  protected abstract Term[] getValues();

  protected Term[] getValues( final NamespaceIdentifier identifier ) {
    return Arrays.stream(getValues())
        .map(x -> getVersion(x,identifier.getVersion()))
        .flatMap(Util::trimStream)
        .toArray(Term[]::new);
  }

  private Optional<? extends Term> getVersion(Term x, String version) {
    return x instanceof TermSeries
        ? ((TermSeries<?>) x).getVersion(version)
        : Optional.of(x);
  }

}