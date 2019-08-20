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
package edu.mayo.kmdp.id;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.toQualifiedIdentifier;

import edu.mayo.kmdp.id.adapter.QualifiedId;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;

public interface Term extends LexicalIdentifier, Serializable {

  URI getRef();

  URI getConceptId();

  default UUID getConceptUUID() {
    return getTag() != null ? UUID.nameUUIDFromBytes(getTag().getBytes()) : null;
  }

  default List<String> getTags() {
    List<String> l = new ArrayList<>(1);
    l.add(getTag());
    return l;
  }

  default ConceptIdentifier asConcept() {
    return (ConceptIdentifier) this;
  }

  default QualifiedId asQualified() {
    return toQualifiedIdentifier(asConcept().getRef());
  }
}
