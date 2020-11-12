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
package edu.mayo.kmdp.terms.adapters.xml;

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.util.StreamUtil;
import java.util.Arrays;
import java.util.Optional;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.series.Series;

public abstract class TermsXMLAdapter extends
    javax.xml.bind.annotation.adapters.XmlAdapter<org.omg.spec.api4kp._20200801.id.ConceptIdentifier, Term> {

  @Override
  public Term unmarshal(org.omg.spec.api4kp._20200801.id.ConceptIdentifier v) {
    return DatatypeHelper.resolveTerm(
        v.getUuid(),
        v.getVersionTag() != null ? getValuesForVersion(v.getVersionTag()) : getValues(),
        Term::getUuid)
        .orElse(null);
  }

  @Override
  public org.omg.spec.api4kp._20200801.id.ConceptIdentifier marshal(Term v) {
    return v != null ? v.asConceptIdentifier() : null;
  }

  protected abstract Term[] getValues();

  @Deprecated
  // Need performance improvement
  protected Term[] getValuesForVersion( final String versionTag ) {
    return Arrays.stream(getValues())
        .map(x -> getVersion(x,versionTag))
        .flatMap(StreamUtil::trimStream)
        .toArray(Term[]::new);
  }

  private Optional<? extends Term> getVersion(Term x, String versionTag) {
    if (x instanceof Series) {
      @SuppressWarnings("unchecked")
      Series<? extends Term,?> series = ((Series<? extends Term,?>) x);
      Optional<? extends Term> termVersion
          = series.getVersion(versionTag);
      if (termVersion.isEmpty() && isSnapshot(versionTag)) {
        termVersion
            = Optional.ofNullable(series.getLatest());
      }
      return termVersion;
    } else {
      return Optional.of(x);
    }
  }

  private boolean isSnapshot(String versionTag) {
    return versionTag.contains("-");
  }

}