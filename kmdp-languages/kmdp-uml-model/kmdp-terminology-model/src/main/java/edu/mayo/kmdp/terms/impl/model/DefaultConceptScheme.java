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
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.Taxonomic;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

public class DefaultConceptScheme<T extends Enum<T> & Taxonomic<T> & Term> extends
    NamespaceIdentifier implements ConceptScheme<T> {

  private URI versionId;

  private EnumSet<T> concepts;
  private Class<T> type;
  private EnumMap<T, EnumSet<T>> ancestry;


  private DefaultConceptScheme() {
    super();
  }

  public DefaultConceptScheme(final String schemeID,
      final String schemeName,
      final URI schemeURI,
      final URI schemeVersionURI,
      final Class<T> type) {
    this.withId(schemeURI)
        .withLabel(schemeName)
        .withTag(schemeID)
        .withVersion(DatatypeHelper.versionOf(schemeVersionURI, schemeURI));
    this.versionId = schemeVersionURI;

    this.type = type;
    this.concepts = EnumSet.allOf(type);
    ancestry = this.concepts.stream()
        .collect(Collectors.toMap(Function.identity(),
            this::toAncestorSet,
            (k1, k2) -> k1,
            () -> new EnumMap<>(type)));
  }

  private EnumSet<T> toAncestorSet(T t) {
    List<T> sups = Arrays.stream(t.getAncestors())
        .filter(type::isInstance)
        .map(type::cast)
        .collect(Collectors.toList());
    T[] ancestors = sups.toArray((T[]) Array.newInstance(type, sups.size()));

    return sups.isEmpty()
        ? EnumSet.noneOf(type)
        : EnumSet.of(sups.get(0), Arrays.copyOfRange(ancestors, 1, ancestors.length));
  }

  @Override
  public URI getVersionId() {
    return versionId;
  }

  @Override
  public NamespaceIdentifier asNamespace() {
    return this;
  }

  public Stream<T> getConcepts() {
    return concepts.stream();
  }

  @Override
  public Optional<T> getTopConcept() {
    return concepts.stream().filter((t) -> !ancestry.containsKey(t) || ancestry.get(t).isEmpty())
        .findAny();
  }

  @Override
  public boolean subsumes(T sup, T sub) {
    return ancestry.get(sub).contains(sup);
  }

  @Override
  public Optional<T> lookup(Term cd) {
    if (type.isInstance(cd)) {
      return Optional.of(type.cast(cd));
    } else {
      return concepts.stream()
          .filter((t) -> t.getRef().equals(cd.getRef()))
          .findAny();
    }
  }


}
