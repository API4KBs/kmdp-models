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

  LEX(false, true, false, Paths.OLEX),
  CON(true, false, false, Paths.SKOS),
  ANN(true, false, true, Paths.ANNO),

  SKOS(true, false, true, Paths.SKOS, Paths.ANNO),
  SKOS_RDF(true, false, true, Paths.SKOS, Paths.ANNO),
  LEX_CON(true,true, false, Paths.OLEX, Paths.SKOS),

  FULL(true, true, true, Paths.ANNO, Paths.SKOS, Paths.OLEX);

  private static class Paths {
    private static final String OLEX = "/query/skosify/entities2ontolex.sparql";
    private static final String SKOS = "/query/skosify/entities2concept.sparql";
    private static final String ANNO = "/query/skosify/entities2skosMeta.sparql";
  }

  protected final List<String> queries;
  public final boolean skos;
  public final boolean usesOlex;
  public final boolean usedDC;

  Modes(boolean skos, boolean olex, boolean dc, String... qs) {
    this.skos = skos;
    this.usesOlex = olex;
    this.usedDC = dc;
    queries = Arrays.asList(qs);
  }

}
