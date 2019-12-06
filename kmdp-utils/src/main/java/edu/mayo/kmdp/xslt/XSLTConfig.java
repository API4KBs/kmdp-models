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
package edu.mayo.kmdp.xslt;

import edu.mayo.kmdp.ConfigProperties;
import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;
import java.util.Properties;

public class XSLTConfig extends ConfigProperties<XSLTConfig, XSLTConfig.XSLTOptions> {

  private static final Properties DEFAULTS = defaulted(XSLTOptions.class);

  public XSLTConfig() {
    super(DEFAULTS);
  }

  @Override
  public XSLTOptions[] properties() {
    return XSLTOptions.values();
  }

  public enum XSLTOptions implements Option<XSLTOptions> {

    OUTPUT_RESOLVER(Opt.of(
        "http://saxon.sf.net/feature/outputURIResolver",
        null,
        "A class with a no-arg constructor that implements URIResolver",
        Class.class,
        false)),
    CATALOGS(Opt.of(
        "http://edu.mayo.kmdp/xslt/catalog",
        null,
        "URL of an XML catalog file",
        String.class,
        false)),
    OUTPUT_TYPE(Opt.of(
        "http://edu.mayo.kmdp/xslt/output",
        OUTPUT.XML.name(),
        "Asserts whether the transformation will produce XML ('XML'), or not ('TXT')",
        OUTPUT.class,
        false)),
    TARGET_NS(
        Opt.of(
            "http://edu.mayo.kmdp/xslt/targetNamespace",
            "http://kmdp.mayo.edu/common/models",
            "The default namespace of the generated XML",
            String.class,
            false));

    public enum OUTPUT {XML, TXT}

    private Opt<XSLTOptions> opt;

    XSLTOptions(Opt<XSLTOptions> opt) {
      this.opt = opt;
    }

    @Override
    public Opt<XSLTOptions> getOption() {
      return opt;
    }
  }
}
