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
package edu.mayo.kmdp.terms.mireot;

import edu.mayo.kmdp.ConfigProperties;
import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;
import edu.mayo.kmdp.terms.mireot.MireotConfig.MireotParameters;
import edu.mayo.kmdp.util.PropertiesUtil;
import java.net.URI;
import java.util.Properties;

public class MireotConfig extends ConfigProperties<MireotConfig, MireotParameters> {

  private static final Properties DEFAULTS = defaulted( MireotParameters.class );

  public MireotConfig() {
    super( DEFAULTS );
  }

  public MireotConfig(Properties defaults) {
    super(defaults);
  }

  @Override
  public MireotConfig.MireotParameters[] properties() {
    return MireotConfig.MireotParameters.values();
  }

  public String encode() {
    return PropertiesUtil.serializeProps(this);
  }

  public enum MireotParameters implements Option<MireotConfig.MireotParameters> {

    BASE_URI( Opt.of(
        "baseUri",
        null,
        "The URI of the resulting ontology (should match the original)",
        URI.class,
        true) ),
    TARGET_URI( Opt.of(
        "targetURI",
        null,
        "The URI of the entity to be extracted",
        URI.class,
        true) ),
    ENTITY_TYPE( Opt.of(
        "entityType",
        null,
        "The type of entity to be extracted (Class, Individual, Data/Object Property...)",
        EntityTypes.class,
        true
    )) ,
    ENTITY_ONLY( Opt.of(
        "entityOnly",
        Boolean.FALSE.toString(),
        "Returns the entity itself, or the entity and its descendants",
        Boolean.class,
        false
    )),
    MIN_DEPTH( Opt.of(
        "min",
        "0",
        "The minimum level of depth (0 = entity itself)",
        Integer.class,
        false
    )),
    MAX_DEPTH( Opt.of(
        "max",
        "-1",
        "The maximum level of depth (-1 = no limit)",
        Integer.class,
        false
    )),
    NAMESPACE_SCOPED( Opt.of(
        "namespaceScoped",
        Boolean.FALSE.toString(),
        "If true, will only include entities in the same namespace as the root entity",
        Boolean.class,
        false
    ))
    ;

    private Opt<MireotParameters> opt;

    MireotParameters( Opt<MireotParameters> opt ) {
      this.opt = opt;
    }

    @Override
    public Opt<MireotParameters> getOption() {
      return opt;
    }

  }
}
