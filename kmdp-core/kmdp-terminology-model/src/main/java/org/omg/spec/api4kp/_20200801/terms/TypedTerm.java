/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.omg.spec.api4kp._20200801.terms;

import org.omg.spec.api4kp._20200801.id.Identifiable;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.series.Series;

/**
 * Term that evokes a concept (that denotes an entity) of a given kind
 * @param <T> (a type that represents) the kind of entity being denoted
 *
 * @see VersionableTerm
 */
public interface TypedTerm<T extends Term> extends Term {

  /**
   * Returns true if this and the other Terms evoke the same concept
   *
   * The default implementation assumes that the Term's UUID
   * is uniquely associated, and thus invariant, with the concept,
   *
   * This default implementation also ignores versions, as long
   * as they are versions of the same entity.
   *
   * The interface is Typed, so that only TypedTerms with the same
   * range can be compared with this method.
   *
   * Implementing classes can refine this method, or use related
   * methods:
   *
   * @param other The typed concept to cehck for co-reference
   *
   * @see Term#isCoreferent(Term)
   * @see Term#sameTermAs(Term)
   * @see Term#evokesSameAs(Term)
   * @see Series#isSameEntity(Identifiable)
   * @return
   */
  default boolean sameAs(T other) {
    return other != null
        && getUuid().equals(other.getUuid());
  }

}
