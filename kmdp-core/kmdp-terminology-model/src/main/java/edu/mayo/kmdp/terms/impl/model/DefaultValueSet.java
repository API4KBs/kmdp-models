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

import edu.mayo.kmdp.terms.ValueSet;
import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Stream;
import org.omg.spec.api4kp._20200801.id.Term;

@SuppressWarnings("unchecked")
public class DefaultValueSet<T extends Enum<T> & Term> extends AbstractValueSet implements
    ValueSet {

  private EnumSet<T> concepts;

  @SuppressWarnings("unchecked")
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

  @SuppressWarnings("unchecked")
  public DefaultValueSet(String id, String name, URI uri, Class<T> type, T pivot, T... concepts) {
    this(id, name, uri, type, concepts);
    setPivotalConcept(pivot);
    init(pivot, concepts);
  }

  @SuppressWarnings("unchecked")
  private void init(T pivot, T... concepts) {
    this.concepts = EnumSet.of(pivot, concepts);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean contains(Term cd) {
    return this.concepts.contains((T) cd);
  }

  @Override
  public Stream<Term> getConcepts() {
    return concepts.stream()
        .map(Term.class::cast);
  }


}
