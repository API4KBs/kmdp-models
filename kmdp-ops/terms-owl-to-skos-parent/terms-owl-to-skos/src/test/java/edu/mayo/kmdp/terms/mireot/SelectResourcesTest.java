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
package edu.mayo.kmdp.terms.mireot;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SelectResourcesTest extends BaseMireotTest {

  @Test
  public void testExtractRelationships() {
    MireotExtractor extractor = newExtractor("/ontology/cito.rdf");

    Set<Resource> props = extractor.extract(baseUri + "/cites", EntityTypes.OBJ_PROP);

    assertTrue(props.contains(r("cites")));
    assertTrue(props.contains(r("updates")));
    assertTrue(props.contains(r("supports")));
    assertTrue(props.contains(r("linksTo")));
    assertTrue(props.contains(r("disputes")));
    assertTrue(props.contains(r("extends")));
    assertTrue(props.contains(r("critiques")));

    assertEquals(44, props.size());
  }


  @Test
  public void testExtractClasses() {
    MireotExtractor extractor = newExtractor("/ontology/fabio.rdf", "http://purl.org/spar/fabio/");

    Set<Resource> props = extractor.extract(baseUri + "Expression", EntityTypes.CLASS);

    assertTrue(props.contains(r("Database")));
    assertTrue(props.contains(r("Oration")));
    assertTrue(props.contains(r("Supplement")));
    assertTrue(props.contains(r("LectureNotes")));
    assertTrue(props.contains(r("ComputerProgram")));
    assertTrue(props.contains(r("Expression")));

    assertEquals(130, props.size());

  }

  @Test
  public void testExtractDataProps() {
    MireotExtractor extractor = newExtractor("/ontology/fabio.rdf", "http://purl.org/spar/fabio/");

    Set<Resource> props = extractor
        .extract("http://purl.org/dc/terms/title", EntityTypes.DATA_PROP);

    assertTrue(props.contains(r("hasSubtitle")));
    assertTrue(props.contains(r("hasTranslatedTitle")));
    assertTrue(props.contains(ResourceFactory.createResource("http://purl.org/dc/terms/title")));

    assertEquals(7, props.size());

  }

  @Test
  public void testExtractIndividuals() {
    MireotExtractor extractor = newExtractor("/ontology/fabio.rdf", "http://purl.org/spar/fabio/");

    Set<Resource> props = extractor.extract(baseUri + "DigitalStorageMedium", EntityTypes.INST);

    assertTrue(props.contains(r("cloud")));
    assertTrue(props.contains(r("hard-drive")));

    assertEquals(11, props.size());
  }

  @Test
  public void testExtractIndividuals2() {
    MireotExtractor extractor = newExtractor("/ontology/lcc.rdf",
        "http://www.omg.org/spec/LCC/Languages/ISO639-1-LanguageCodes/");

    Set<Resource> props = extractor
        .extract("http://www.omg.org/spec/LCC/Languages/LanguageRepresentation/IndividualLanguage",
            EntityTypes.INST);

    assertEquals(154, props.size());
  }

  @Test
  public void testExtractIndividuals3() {
    MireotExtractor extractor = newExtractor("/ontology/kr-registry.owl",
        "http://edu.mayo.kmdp/registry");

    Set<Resource> props = extractor
        .extract("http://www.omg.org/spec/API4KP/core#ConstructedLanguage",
            EntityTypes.INST);

    props.forEach((r) -> System.out.println(r.getURI()));
    assertEquals(10, props.size());


  }

}
