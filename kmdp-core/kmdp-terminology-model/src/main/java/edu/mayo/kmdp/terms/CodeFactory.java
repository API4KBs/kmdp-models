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

import edu.mayo.kmdp.terms.impl.model.AnonymousConceptScheme;
import edu.mayo.kmdp.terms.impl.model.InternalTerm;
import java.net.URI;
import org.omg.spec.api4kp._1_0.id.Term;

public interface CodeFactory {

  Term of(URI uri,
      String code,
      String codeName,
      String schemeID,
      String schemeName,
      URI schemeURI);

  static Term of(String s) {
    URI codeUri = URI.create(s);
    URI schemeURI = URI.create(codeUri.getRawSchemeSpecificPart());

    return new InternalTerm(
        codeUri,
        codeUri.getFragment(),
        codeUri.getFragment(),
        null,
        codeUri,
        new AnonymousConceptScheme(schemeURI),
        null);
  }
}
