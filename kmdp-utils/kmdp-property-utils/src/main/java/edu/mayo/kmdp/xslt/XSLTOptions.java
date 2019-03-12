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

import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;

public enum XSLTOptions implements Option<XSLTOptions> {

  OUTPUT_RESOLVER(Opt.of("http://saxon.sf.net/feature/outputURIResolver", null, Class.class)),
  CATALOGS(Opt.of("http://edu.mayo.kmdp/xslt/catalog", null, String.class)),
  OUTPUT_TYPE(Opt.of("http://edu.mayo.kmdp/xslt/output", OUTPUT.XML.name(), OUTPUT.class)),
  TARGET_NS(
      Opt.of("http://edu.mayo.kmdp/xslt/targetNamespace", "http://kmdp.mayo.edu/common/models",
          String.class));

  public static enum OUTPUT {XML, TXT}

  private Opt opt;

  XSLTOptions(Opt opt) {
    this.opt = opt;
  }

  @Override
  public Opt getOption() {
    return opt;
  }
}


