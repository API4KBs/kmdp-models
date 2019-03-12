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

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ValueSet;

import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Stream;

public class DefaultValueSet<T extends Enum<T> & Term> extends AbstractValueSet implements
    ValueSet {

  private EnumSet<T> concepts;

  public DefaultValueSet(String id, String name, Class<T> type, T... concepts) {
    this(id, name, URI.create("urn:oid:" + id), type, concepts);
  }

  public DefaultValueSet(String id, String name, URI uri, Class<T> type, T... concepts) {
    super(id, name, uri);
    switch (concepts.length) {
      case 0:
        this.concepts = EnumSet.noneOf(type);
        break;
      case 1:
        init(concepts[0]);
        break;
      default:
        init(concepts[0], Arrays.copyOfRange(concepts, 1, concepts.length));
    }
  }

  public DefaultValueSet(String id, String name, Class<T> type, T pivot, T... concepts) {
    this(id, name, URI.create("urn:oid:" + id), type, pivot, concepts);
  }

  public DefaultValueSet(String id, String name, URI uri, Class<T> type, T pivot, T... concepts) {
    this(id, name, uri, type, concepts);
    setPivotalConcept(pivot);
    init(pivot, concepts);
  }

  private void init(T pivot, T... concepts) {
    this.concepts = EnumSet.of(pivot, concepts);
  }

  public boolean contains(Term cd) {
    return this.concepts.contains(cd);
  }

  @Override
  public Stream<Term> getConcepts() {
    return concepts.stream()
        .map(Term.class::cast);
  }


}
