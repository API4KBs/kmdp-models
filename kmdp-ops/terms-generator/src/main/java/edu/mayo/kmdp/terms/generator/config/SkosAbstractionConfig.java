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
import org.apache.jena.vocabulary.SKOS;

public class SkosAbstractionConfig extends
    ConfigProperties<SkosAbstractionConfig, SkosAbstractionParameters> {

  private static final Properties DEFAULTS = defaulted(SkosAbstractionParameters.class);

  public SkosAbstractionConfig() {
    super(DEFAULTS);
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
        Boolean.FALSE.toString(),
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
        Boolean.FALSE.toString(),
        "Runs a DL reasoner on the SKOS ontology before performing the abstraction",
        Boolean.class,
        false)),
    ENFORCE_VERSION(Opt.of(
        "enforceVersion",
        Boolean.FALSE.toString(),
        "If true, concept schemes must have a version, stated using owl:versionIRI",
        Boolean.class,
        false)),
    VERSION_PATTERN(Opt.of(
        "versionPattern",
        "",
        "Regular expression that maps a version URI to a version tag",
        String.class,
        false)),
    DATE_PATTERN(Opt.of(
        "datePattern",
        "yyyyMMdd",
        "Regular expression a version tag to a release date",
        String.class,
        false)),
    TAG_TYPE(Opt.of(
        "tag_type",
        "urn:uuid",
        "Chooses the type of 'notation' to use as primary tag, when multiple are present",
        URI.class,
        false)),
    VERSION_POS( Opt.of(
        "versionPosition",
        Integer.toString(Integer.MAX_VALUE - 1),
        "Determines the position of the version tag in the version IRI, "
            + "starting from the end of the IRI, in reverse order ",
        Integer.class,
        false)),
    LABEL_PROPERTY( Opt.of(
        "labelProperty",
        SKOS.prefLabel.getLocalName(),
        "Determines which label annotation will be used preferentially",
        String.class,
        false
    ));

    private Opt<SkosAbstractionParameters> opt;

    SkosAbstractionParameters(Opt<SkosAbstractionParameters> opt) {
      this.opt = opt;
    }

    @Override
    public Opt<SkosAbstractionParameters> getOption() {
      return opt;
    }

  }
}