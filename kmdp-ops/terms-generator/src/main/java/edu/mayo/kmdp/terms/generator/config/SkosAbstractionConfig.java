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
package edu.mayo.kmdp.terms.generator.config;

import edu.mayo.kmdp.ConfigProperties;
import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import java.net.URI;
import java.util.Properties;

public class SkosAbstractionConfig extends
    ConfigProperties<SkosAbstractionConfig, SkosAbstractionParameters> {

  private static final Properties defaults = defaulted(SkosAbstractionParameters.class);

  public SkosAbstractionConfig() {
    super(defaults);
  }

  public enum CLOSURE_MODE {
    IMPORTS, INCLUDES;
  }

  @Override
  public SkosAbstractionParameters[] properties() {
    return SkosAbstractionParameters.values();
  }

  public enum SkosAbstractionParameters implements Option<SkosAbstractionParameters> {

    ENFORCE_CLOSURE(Opt.of(
        "enforceClosure",
        "false",
        "Enforce the non-standard rule broader(Concept,Top) and topConceptOf(Top,Scheme) => inScheme(Concept,Scheme)",
        Boolean.class,
        false)),
    CLOSURE_MODE(Opt.of(
        "closureMode",
        "IMPORTS",
        "Lets a scheme reference another (import), vs redeclaring the concepts (includes)",
        CLOSURE_MODE.class,
        false)),
    REASON(Opt.of(
        "reason",
        "false",
        "Runs a DL reasoner on the SKOS ontology before performing the abstraction",
        Boolean.class,
        false)),
    TAG_TYPE(Opt.of(
        "tag_type",
        "urn:uuid",
        "Chooses the type of 'notation' to use as primary tag, when multiple are present",
        URI.class,
        false));

    private Opt opt;

    SkosAbstractionParameters(Opt opt) {
      this.opt = opt;
    }

    @Override
    public Opt getOption() {
      return opt;
    }

  }
}