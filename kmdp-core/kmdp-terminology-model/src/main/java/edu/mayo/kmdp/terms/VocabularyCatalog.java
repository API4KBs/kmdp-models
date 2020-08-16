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

import java.net.URI;
import java.util.Optional;
import org.omg.spec.api4kp._20200801.id.Term;

/**
 * Catalog of Concept Schemes
 */
public interface VocabularyCatalog {

  void register(URI schemeURI, ConceptScheme<? extends Term> scheme);

  Optional<ConceptScheme<? extends Term>> resolve(URI schemeURI);

  Optional<ConceptScheme<? extends Term>> resolve(String schemeID);

  Optional<URI> lookupURI(String schemeID);

}
