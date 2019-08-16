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
package edu.mayo.kmdp.util.properties.jaxb;

import edu.mayo.kmdp.ConfigProperties;
import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;
import java.util.Properties;
import javax.xml.bind.Marshaller;

public class JaxbConfig extends ConfigProperties<JaxbConfig, JaxbConfig.JaxbOptions> {

  private static final Properties DEFAULTS = defaulted(JaxbOptions.class);

  public JaxbConfig() {
    super(DEFAULTS);
  }

  @Override
  public JaxbOptions[] properties() {
    return JaxbOptions.values();
  }

  public enum JaxbOptions implements Option<JaxbOptions> {

    FORMATTED_OUTPUT(Opt.of(
        Marshaller.JAXB_FORMATTED_OUTPUT,
        "true",
        "Pretty print the output (true) or compacts it (false)",
        Boolean.class,
        false)),
    NAMESPACE_MAPPER(Opt.of(
        "com.sun.xml.bind.namespacePrefixMapper",
        null,
        "A class that implements NamespacePrefixMapper",
        Class.class,
        false)),
    SCHEMA_LOCATION(Opt.of(
        Marshaller.JAXB_SCHEMA_LOCATION,
        null,
        "URL of the XSD schema to validate",
        String.class,
        false));

    private Opt<JaxbOptions> opt;

    JaxbOptions(Opt<JaxbOptions> opt) {
      this.opt = opt;
    }

    @Override
    public Opt<JaxbOptions> getOption() {
      return opt;
    }
  }
}
