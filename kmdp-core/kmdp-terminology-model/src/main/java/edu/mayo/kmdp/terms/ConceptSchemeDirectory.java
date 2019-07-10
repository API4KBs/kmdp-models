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

import edu.mayo.kmdp.id.Identifier;
import edu.mayo.kmdp.id.Term;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

import java.net.URI;
import java.util.*;

public class ConceptSchemeDirectory {

  private Map<URI, Class<? extends Term>> registry = new HashMap<>();

  public void register(Class<? extends Term> klass) {
    Term[] terms = klass.getEnumConstants();
    if (terms != null && terms.length > 0) {
      Identifier id = klass.getEnumConstants()[0].getNamespace();
      if (id instanceof NamespaceIdentifier) {
        registry.put(((NamespaceIdentifier) id).getId(),
            klass);
      }
    }
  }


  public Set<URI> listVocabularies() {
    return new HashSet<>(registry.keySet());
  }

  public Optional<? extends Term> resolve(URI namespaceId, String tag) {
    return resolve(tag, lookup(namespaceId));
  }

  private <T extends Term> Class<T> lookup(URI namespaceId) {
    return (Class<T>) registry.get(namespaceId);
  }




  public static <T extends Term> Optional<T> resolve(final Term trm, final Class<T> enumKlass) {
    return Arrays.stream(enumKlass.getEnumConstants())
        .filter((x) -> trm.getRef().equals(x.getRef()))
        .findAny();
  }

  public static <T extends Term> Optional<T> resolve(final String tag, final Class<T> enumKlass) {
    return Arrays.stream(enumKlass.getEnumConstants())
        .filter((x) -> x.getTag().equals(tag))
        .findAny();
  }

  public static <T extends Term> Optional<T> resolveTag(final String tag, final Class<T> enumKlass) {
    return Arrays.stream(enumKlass.getEnumConstants())
        .filter((x) -> x.getTags().contains(tag))
        .findAny();
  }

  public static <T extends Term> Optional<T> resolveUUID(final UUID conceptId, final Class<T> enumKlass) {
    return Arrays.stream(enumKlass.getEnumConstants())
        .filter((x) -> x.getConceptUUID().equals(conceptId))
        .findAny();
  }

  public static <T extends Term> Optional<T> resolveId(final String conceptId, final Class<T> enumKlass) {
    return Arrays.stream(enumKlass.getEnumConstants())
        .filter((x) -> x.getConceptId().toString().equals(conceptId) || x.getConceptUUID().toString().equals(conceptId))
        .findAny();
  }

  public static <T extends Term> Optional<T> resolveRef(final String refUri, final Class<T> enumKlass) {
    return Arrays.stream(enumKlass.getEnumConstants())
        .filter((x) -> x.getRef().toString().equals(refUri))
        .findAny();
  }


}
