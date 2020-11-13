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
import java.util.Properties;

@SuppressWarnings("unchecked")
public class EnumGenerationConfig extends
    ConfigProperties<EnumGenerationConfig, EnumGenerationConfig.EnumGenerationParams> {

  private static final Properties DEFAULTS = defaulted(EnumGenerationParams.class);

  public EnumGenerationConfig() {
    super(DEFAULTS);
  }

  @Override
  public EnumGenerationParams[] properties() {
    return EnumGenerationParams.values();
  }

  public enum EnumGenerationParams implements Option<EnumGenerationParams> {

    WITH_JAXB(Opt.of(
        "withJaxb",
        Boolean.FALSE.toString(),
        "Enable Jaxb support",
        Boolean.class,
        false)),
    WITH_JSONLD(Opt.of(
        "withJsonLD",
        Boolean.FALSE.toString(),
        "Enable JSON-LD support",
        Boolean.class,
        false)),
    WITH_JSON(Opt.of(
        "withJson",
        Boolean.FALSE.toString(),
        "Enable JSON support",
        Boolean.class,
        false)),
    PACKAGE_NAME(Opt.of(
        "packageName",
        "",
        "Forces all enums to be generated in the given package (overrides and native will be ignored)",
        String.class,
        false)),
    PACKAGE_OVERRIDES(Opt.of(
        "packageOverrides",
        "",
        "Override package names from URI-driven defaults, provided as a comma-separated list of <defaultName>=<overriddenName>",
        String.class,
        false)),
    INTERFACE_OVERRIDES(Opt.of(
        "interfaceOverrides",
        "",
        "Properties-encoded map that causes any scheme X to implement the interface derived from a scheme Y",
        String.class,
        false)),
    XML_ADAPTER(Opt.of(
        "baseXmlAdapter",
        "edu.mayo.kmdp.terms.adapters.xml.TermsXMLAdapter",
        "Base class that controls the XML serialization of terminologies",
        String.class,
        false)),
    JSON_ADAPTER(Opt.of(
        "baseJsonAdapter",
        "edu.mayo.kmdp.terms.adapters.json.ConceptIdentifierTermsJsonAdapter",
        "Base class that controls the JSON serialization of terminologies",
        String.class,
        false)),
    UUID_INDEX(Opt.of(
        "indexByUUID",
        "true",
        "Uses UUID-based identifiers for indexing. "
            + "Must be true when using UUIDBasedDeserializer as JSON Adapter.",
        Boolean.class,
        false));

    private Opt<EnumGenerationParams> opt;

    EnumGenerationParams(Opt<EnumGenerationParams> opt) {
      this.opt = opt;
    }

    @Override
    public Opt<EnumGenerationParams> getOption() {
      return opt;
    }

  }
}