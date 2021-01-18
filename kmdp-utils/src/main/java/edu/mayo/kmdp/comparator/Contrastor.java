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
package edu.mayo.kmdp.comparator;

import java.util.Comparator;


public abstract class Contrastor<T> implements Comparator<T> {

  public enum Comparison {

    /* Objects are the same, according to a principle of identity
     *  Implies equality, equivalence, not distinctness */
    IDENTICAL,
    /* Objects are equal - indistinguishable given all their public properties
     *  Implies equivalence, not distinctness */
    EQUAL,
    /* Objects are equivalent according to a (partial) ordering relation
     *  Implies not distinctness */
    EQUIVALENT,
    /* Objects are not equivalent, nor equal, nor identical */
    DISTINCT,
    /* First > Second, according to a (partial) ordering relation
     *  Implies distinctness */
    BROADER,
    /* First < Second according to a (partial) ordering relation
     *  Implies distinctness */
    NARROWER,
    /* Neither equivalent, nor broader, nor narrower, according to a (partial) ordering relation
     *  Implies distinctness */
    INCOMPARABLE,
    /* Unable to determine, usually due to missing/unknown information */
    UNKNOWN;

  }

  protected Contrastor() {}

  public Comparison contrast(T first, T second) {
    if (first == null || second == null) {
      return Comparison.UNKNOWN;
    }
    if (first == second) {
      return Comparison.IDENTICAL;
    }
    if (first.equals(second)) {
      return Comparison.EQUAL;
    }
    if (!canCompare()) {
      return Comparison.DISTINCT;
    }
    if (!comparable(first, second)) {
      return Comparison.INCOMPARABLE;
    }
    int comp = compare(first, second);
    if (comp == 0) {
      return Comparison.EQUIVALENT;
    }
    if (comp > 0) {
      return Comparison.BROADER;
    }
    return Comparison.NARROWER;
  }

  protected boolean canCompare() {
    return true;
  }

  public abstract boolean comparable(T first, T second);

  public static boolean isBroaderOrEqual(Comparison c) {
    return c == Comparison.BROADER || c == Comparison.EQUAL || c == Comparison.EQUIVALENT || c == Comparison.IDENTICAL;
  }
  public static boolean isNarrowerOrEqual(Comparison c) {
    return c == Comparison.NARROWER || c == Comparison.EQUAL || c == Comparison.EQUIVALENT || c == Comparison.IDENTICAL;
  }
  public static boolean isEqual(Comparison c) {
    return c == Comparison.EQUAL || c == Comparison.EQUIVALENT || c == Comparison.IDENTICAL;
  }
}
