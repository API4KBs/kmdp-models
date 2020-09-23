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
package edu.mayo.kmdp.terms.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.example.SomeBean;
import edu.mayo.kmdp.terms.example.sch1.SCH1;
import edu.mayo.kmdp.util.JSonLDUtil;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JenaUtil;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;

public class JSONLDSerializationTest {

  @Test
  public void testSemanticEnumSerialization() {
    SomeBean sb = new SomeBean();
    sb.setSchone(SCH1.Specific_Concept);
    sb.setSchones(new HashSet<>(Arrays.asList(SCH1.Nested_Specific_Concept, SCH1.Sub_Sub_Concept)));

    Optional<ByteArrayOutputStream> baos = JSonUtil.writeJson(sb,
        JSonLDUtil.initLDModule(),
        JSonUtil.defaultProperties());

    assertTrue(baos.isPresent());

    // System.out.println(new String(baos.get().toByteArray()));

    Optional<Model> model = JenaUtil.fromJsonLD(new String(baos.get().toByteArray()));

    assertTrue(model.isPresent());

    // System.out.println(JenaUtil.asString(model.get()));
    assertEquals(37, (int) JenaUtil.sizeOf(model.get()));


  }
}


/*

 */