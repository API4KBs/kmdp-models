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

import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;

import javax.xml.bind.Marshaller;

public enum JaxbOptions implements Option<JaxbOptions> {

  FORMATTED_OUTPUT(Opt.of(Marshaller.JAXB_FORMATTED_OUTPUT, "true", Boolean.class)),
  NAMESPACE_MAPPER(Opt.of("com.sun.xml.bind.namespacePrefixMapper", null, Class.class)),
  SCHEMA_LOCATION(Opt.of(Marshaller.JAXB_SCHEMA_LOCATION, null, String.class));

  private Opt<JaxbOptions> opt;

  JaxbOptions(Opt<JaxbOptions> opt) {
    this.opt = opt;
  }

  @Override
  public Opt<JaxbOptions> getOption() {
    return opt;
  }
}
