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
package edu.mayo.kmdp.terms.skosifier;

import java.util.UUID;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.XSD;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;


class TestBase {

  static final String PREAMBLE = "" +
      "PREFIX rdf: <" + RDF.getURI() + "> \n" +
      "PREFIX owl: <" + OWL.getURI() + "> \n" +
      "PREFIX xsd: <" + XSD.getURI() + ">\n " +
      "PREFIX rdfs: <" + RDFS.getURI() + "> \n" +
      "PREFIX skos: <" + SKOS.getURI() + "> \n" +
      "PREFIX dct: <http://purl.org/dc/terms/> \n" +
      "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n" +
      "PREFIX olex: <http://www.w3.org/ns/lemon/ontolex#> \n";


  static void init() {
    PrintUtil.registerPrefix("skos", SKOS.getURI());
    PrintUtil.registerPrefix("olex", "http://www.w3.org/ns/lemon/ontolex#");
  }

  String uuid(String name) {
    return UUID.nameUUIDFromBytes(name.getBytes()).toString();
  }

  Model run(List<String> onto, Owl2SkosConfig cfg) {
    Optional<Model> model = new Owl2SkosConverter().run(onto, cfg);
    if (model.isPresent()) {
      return model.get();
    } else {
      fail("Model is inconsistent");
      // Should never get here...
      return null;
    }
  }

  MapBuilder<String, String> a() {
    return new MapBuilder<>();
  }

  class MapBuilder<T, Q> extends HashMap<T, Q> {
    public MapBuilder<T, Q> with(T key, Q val) {
      super.put(key, val);
      return this;
    }
  }
}
