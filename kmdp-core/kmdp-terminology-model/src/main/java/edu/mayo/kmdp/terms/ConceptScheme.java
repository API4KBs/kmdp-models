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
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.util.NameUtils;
import java.io.Serializable;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

public interface ConceptScheme<T extends Term> extends VersionedIdentifier, Serializable {

  String getLabel();

  String getTag();

  URI getId();

  String getVersion();

  URI getVersionId();


  NamespaceIdentifier asNamespace();

  Stream<T> getConcepts();

  Optional<T> getTopConcept();

  Optional<T> lookup(Term other);

  boolean subsumes(T sup, T sub);


  default String getPublicName() {
    return NameUtils.getTermCodeSystemName(getTopConcept()
        .map(Term::getLabel)
        .orElseGet(this::getLabel));
  }
}
