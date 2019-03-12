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

import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;

public enum EnumGenerationParams implements Option<EnumGenerationParams> {

  PACKAGE_NAME(Opt.of("packageName", "", String.class)),
  WITH_JAXB(Opt.of("withJaxb", "false", Boolean.class)),
  WITH_JSONLD(Opt.of("withJsonLD", "false", Boolean.class)),
  WITH_JSON(Opt.of("withJson", "false", Boolean.class)),
  TERMS_PROVIDER(Opt.of("termsProvider", "", String.class));

  private Opt opt;

  EnumGenerationParams(Opt opt) {
    this.opt = opt;
  }

  @Override
  public Opt getOption() {
    return opt;
  }

}
