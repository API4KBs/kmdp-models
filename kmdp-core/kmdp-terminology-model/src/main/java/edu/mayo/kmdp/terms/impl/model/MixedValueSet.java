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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.omg.spec.api4kp._20200801.id.Term;

public class MixedValueSet extends AbstractValueSet<Term> implements ValueSet {

  private Set<Term> concepts;

  public MixedValueSet(String id, String name, Term... concepts) {
    this(id, name, URI.create("urn:oid:" + id), concepts);
  }

  public MixedValueSet(String id, String name, URI uri, Term... concepts) {
    this(id, name, uri, null, concepts);
  }

  public MixedValueSet(String id, String name, Term pivot, Term... concepts) {
    this(id, name, URI.create("urn:oid:" + id), pivot, concepts);
  }

  public MixedValueSet(String id, String name, URI uri, Term pivot, Term... concepts) {
    super(id, name, uri);
    setPivotalConcept(pivot);
    this.concepts = new HashSet<>();
    this.concepts.addAll(Arrays.asList(concepts));
  }


  public boolean contains(Term cd) {
    return this.concepts.contains(cd);
  }

  @Override
  public Stream<Term> getConcepts() {
    return concepts.stream();
  }


}
