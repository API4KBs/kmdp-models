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

import edu.mayo.kmdp.ConfigProperties;
import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.PropertiesUtil;
import java.util.Properties;

@SuppressWarnings("unchecked")
public class Owl2SkosConfig extends ConfigProperties<Owl2SkosConfig, OWLtoSKOSTxParams> {

  private static final Properties DEFAULTS = defaulted( OWLtoSKOSTxParams.class );

  public Owl2SkosConfig() {
    super( DEFAULTS );
  }

  public Owl2SkosConfig(Properties defaults) {
    super(defaults);
  }

  @Override
  public Owl2SkosConfig.OWLtoSKOSTxParams[] properties() {
    return Owl2SkosConfig.OWLtoSKOSTxParams.values();
  }

  public String encode() {
    return PropertiesUtil.serializeProps(this);
  }


  public enum OWLtoSKOSTxParams implements Option<Owl2SkosConfig.OWLtoSKOSTxParams> {

    TGT_NAMESPACE( Opt.of(
        "targetNamespace",
        null,
        "The base URI of the generated SKOS vocabulary",
        String.class,
        true) ),
    SCHEME_NAME( Opt.of(
        "schemeName",
        null,
        "The label of generated skos:ConceptScheme",
        String.class,
        false
    )) ,
    TOP_CONCEPT_NAME( Opt.of(
        "topConceptName",
        null,
        "The Name of the class that will be mapped to a skos:TopConcept for the new scheme",
        String.class,
        false
    )),
    MODE( Opt.of(
        "mode",
        Modes.SKOS.name(),
        "The transformation profile, with combinations of SKOS Concept Schemes, SKOS Annotations, OLEX Terminology",
        String.class,
        false
    )),
    VALIDATE( Opt.of(
        "validate",
        Boolean.FALSE.toString(),
        "if true, performs validation after the generation",
        Boolean.class,
        false
    )),
    FLATTEN( Opt.of(
        "flatten",
        Boolean.FALSE.toString(),
        "if true, merges the dependency ontologies (SKOS, OLEX)",
        Boolean.class,
        false
    )),
    ADD_IMPORTS( Opt.of(
        "imports",
        Boolean.TRUE.toString(),
        "if true, imports the dependency ontologies (SKOS, OLEX)",
        Boolean.class,
        false
    )),
    VERSION_POS( Opt.of(
        "versionPosition",
        Integer.toString(Integer.MAX_VALUE - 1),
        "Determines the position of the version tag in the version IRI, "
            + "starting from the end of the IRI, in reverse order ",
        Integer.class,
        false
    ))
    ;

    private Opt<OWLtoSKOSTxParams> opt;

    OWLtoSKOSTxParams( Opt<OWLtoSKOSTxParams> opt ) {
      this.opt = opt;
    }

    @Override
    public Opt<OWLtoSKOSTxParams> getOption() {
      return opt;
    }

  }
}
