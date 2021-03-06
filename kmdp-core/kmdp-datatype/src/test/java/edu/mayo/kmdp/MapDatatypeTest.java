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

import static edu.mayo.kmdp.id.adapter.CopyableHashMap.toBinds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.SwaggerTestUtil;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.datatypes.Bindings;
import org.omg.spec.api4kp._20200801.datatypes.Map;

class MapDatatypeTest {

  private static final Map custoMap = new Map();

  @BeforeAll
  static void buildMap() {
    custoMap.put("x", 1);
    custoMap.put("y", "2a");
  }

  @Test
  void testMapSerializationJson() {
    Optional<String> str = JSonUtil.printJson(custoMap);
    assertTrue(str.isPresent());

    JsonNode jTree = str.flatMap(JSonUtil::readJson).orElse(null);

    assertEquals("1", JSonUtil.jString("x", jTree).orElse(""));
    assertEquals("2a", JSonUtil.jString("y", jTree).orElse(""));
  }


  @Test
  void testYamlGeneration() {
    InputStream in = MapDatatypeTest.class
        .getResourceAsStream("/yaml/API4KP/api4kp/datatypes/datatypes.yaml");
    assertNotNull(in);

    Swagger sw = SwaggerTestUtil.parse(in);
    assertNotNull(sw);
    assertNotNull(sw.getDefinitions());

    Model mapModel = sw.getDefinitions().get("Map");
    assertNotNull(mapModel);

    assertTrue(mapModel instanceof ModelImpl);
    assertNotNull(((ModelImpl) mapModel).getAdditionalProperties());
  }
  
  @Test
  void testBindingConstructor() {
    Bindings<String,String> b = toBinds("x", "1", "y", "2");
    assertEquals(2, b.size());
    assertEquals("1", b.get("x"));
    assertEquals("2", b.get("y"));

    assertThrows(IllegalArgumentException.class, () -> toBinds("x"));

    assertTrue(toBinds().isEmpty());
  }

}
