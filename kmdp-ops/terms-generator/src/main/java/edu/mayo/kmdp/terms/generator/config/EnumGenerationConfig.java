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
import java.util.Map;
import java.util.Properties;

public class EnumGenerationConfig extends
    ConfigProperties<EnumGenerationConfig, EnumGenerationConfig.EnumGenerationParams> {

  private static final Properties defaults = defaulted(EnumGenerationParams.class);

  public EnumGenerationConfig() {
    super(defaults);
  }

  @Override
  public EnumGenerationParams[] properties() {
    return EnumGenerationParams.values();
  }

  public enum EnumGenerationParams implements Option<EnumGenerationParams> {

    WITH_JAXB(Opt.of(
        "withJaxb",
        "false",
        "Enable Jaxb support",
        Boolean.class,
        false)),
    WITH_JSONLD(Opt.of(
        "withJsonLD",
        "false",
        "Enable JSON-LD support",
        Boolean.class,
        false)),
    WITH_JSON(Opt.of(
        "withJson",
        "false",
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
    XML_ADAPTER(Opt.of(
        "baseXmlAdapter",
        "edu.mayo.kmdp.terms.TermsXMLAdapter",
        "Base class that controls the XML serialization of terminologies",
        String.class,
        false)),
    JSON_ADAPTER(Opt.of(
        "baseJsonAdapter",
        "edu.mayo.kmdp.terms.TermsJsonAdapter.Deserializer",
        "Base class that controls the JSON serialization of terminologies",
        String.class,
        false));

    private Opt opt;

    EnumGenerationParams(Opt opt) {
      this.opt = opt;
    }

    @Override
    public Opt getOption() {
      return opt;
    }

  }
}