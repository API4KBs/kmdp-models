/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.registry;

import static edu.mayo.kmdp.registry.RegistryUtil.askQuery;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;


public class RegistryVersionTest extends RegistryTestBase {

  @Test
  public void testLatestVersion() {
    Model registry = initRegistry("LATEST");
    assertTrue(checkLanguage("http://www.w3.org/2002/07/owl",
        registry, "api4kp"));
    assertTrue(checkLanguage("https://graphql.github.io/graphql-spec/June2018/",
        registry, "api4kp"));
  }

  @Test
  public void testOlderVersion() {
    Model registry = initRegistry("20190801", "http://ontology.mayo.edu/ontologies/kmdp-registry/");
    assertTrue(checkLanguage("http://www.w3.org/2002/07/owl",
        registry, "know"));
    assertFalse(checkLanguage("https://graphql.github.io/graphql-spec/June2018/",
        registry, "know"));
  }


  private boolean checkLanguage(final String langURI,
      final Model registry, String prefix) {
    String qry = PREAMBLE +
        "SELECT ?L " +
        " " +
        "WHERE { " +
        "   ?L a " + prefix + ":ConstructedLanguage. " +
        "}";

    List<Map<String, String>> ans = askQuery(qry, registry);
    return ans.stream()
        .map(Map::values)
        .anyMatch(x -> x.contains(langURI));
  }
}
