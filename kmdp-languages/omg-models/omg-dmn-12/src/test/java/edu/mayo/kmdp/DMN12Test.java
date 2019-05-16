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
package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.omg.spec.dmn._20180521.model.ObjectFactory;
import org.omg.spec.dmn._20180521.model.TDecision;
import org.omg.spec.dmn._20180521.model.TDefinitions;

public class DMN12Test {

  @Test
  public void testClasses() {
    TDecision decision = new TDecision();
    assertNotNull(decision);
  }

  @Test
  public void testValidation() {
    TDefinitions decision = new TDefinitions()
        .withNamespace("http://org.foo")
        .withName("example");

    assertTrue(isValid(decision));
  }

  @Test
  public void testInValidation() {
    TDefinitions decision = new TDefinitions()
        .withName("example");

    assertFalse(isValid(decision));
  }

  boolean isValid(TDefinitions decision) {
    ObjectFactory of = new ObjectFactory();
    return XMLUtil.getSchemas(URI.create("https://www.omg.org/spec/DMN/1.2/"))
        .flatMap((s) ->
            JaxbUtil.marshall(
                Collections.singleton(of.getClass()),
                decision,
                of::createDefinitions,
                s,
                JaxbUtil.defaultProperties()))
        .flatMap(Util::asString).isPresent();
  }

}

