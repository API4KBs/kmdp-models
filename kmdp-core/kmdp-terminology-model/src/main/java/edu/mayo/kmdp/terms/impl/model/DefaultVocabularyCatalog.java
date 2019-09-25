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
package edu.mayo.kmdp.terms.impl.model;


import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.VocabularyCatalog;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultVocabularyCatalog implements VocabularyCatalog {

  protected static final Map<URI, ConceptScheme> entries = new ConcurrentHashMap<>();

  @Override
  public void register(URI schemeURI, ConceptScheme scheme) {
    entries.put(schemeURI, scheme);
  }

  @Override
  public Optional<ConceptScheme> resolve(URI schemeURI) {
    return Optional.ofNullable(entries.get(schemeURI));
  }

  @Override
  public Optional<ConceptScheme> resolve(String schemeID) {
    return entries.values().stream()
        .filter(s -> schemeID.equals(s.getId().toString()))
        .findAny();
  }

  @Override
  public Optional<URI> lookupURI(String schemeID) {
    return resolve(schemeID)
        .map(ConceptScheme::getId);
  }
}
