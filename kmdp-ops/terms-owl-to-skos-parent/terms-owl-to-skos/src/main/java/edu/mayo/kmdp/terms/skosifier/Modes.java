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
package edu.mayo.kmdp.terms.skosifier;

import java.util.Arrays;
import java.util.List;

public enum Modes {
  LEX(false, true, "/query/skosify/entities2ontolex.sparql"),
  CON(true, false, "/query/skosify/entities2concept.sparql"),
  ANN(true, false, "/query/skosify/entities2skosMeta.sparql"),
  SKOS(true, false, "/query/skosify/entities2concept.sparql",
      "/query/skosify/entities2skosMeta.sparql"),
  FULL(true, true, "/query/skosify/entities2concept.sparql",
      "/query/skosify/entities2skosMeta.sparql", "/query/skosify/entities2ontolex.sparql");

  public final List<String> queries;
  public final boolean skos;
  public final boolean olex;

  Modes(boolean skos, boolean olex, String... qs) {
    this.skos = skos;
    this.olex = olex;
    queries = Arrays.asList(qs);
  }

}
