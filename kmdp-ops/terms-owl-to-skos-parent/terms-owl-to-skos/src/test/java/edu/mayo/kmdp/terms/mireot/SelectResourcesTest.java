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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.mireot.MireotConfig.MireotParameters;
import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL2;
import org.junit.jupiter.api.Test;


public class SelectResourcesTest extends BaseMireotTest {

  @Test
  public void testExtractRelationships() {

    String base = "http://purl.org/spar/cito/";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.OBJ_PROP);

    Set<Resource> props = new MireotExtractor().fetch(stream("/ontology/cito.rdf"),
        URI.create(base + "cites"),
        cfg)
        .map((m) -> getResources(base,m))
        .orElse(new HashSet<>());

    assertTrue(props.contains(r(base, "cites")));
    assertTrue(props.contains(r(base, "updates")));
    assertTrue(props.contains(r(base, "supports")));
    assertTrue(props.contains(r(base, "linksTo")));
    assertTrue(props.contains(r(base, "disputes")));
    assertTrue(props.contains(r(base, "extends")));
    assertTrue(props.contains(r(base, "critiques")));

    assertEquals(44, props.size());
  }


  @Test
  public void testExtractClasses() {

    String base = "http://purl.org/spar/fabio/";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.CLASS);

    Set<Resource> props = new MireotExtractor().fetch(stream("/ontology/fabio.rdf"),
        URI.create(base + "Expression"),
        cfg)
        .map((m) -> getResources(base,m))
        .orElse(new HashSet<>());

    assertTrue(props.contains(r(base, "Database")));
    assertTrue(props.contains(r(base, "Oration")));
    assertTrue(props.contains(r(base, "Supplement")));
    assertTrue(props.contains(r(base, "LectureNotes")));
    assertTrue(props.contains(r(base, "ComputerProgram")));
    assertTrue(props.contains(r(base, "Expression")));

    assertEquals(130, props.size());

  }

  @Test
  public void testExtractDataProps() {

    String base = "http://purl.org/spar/fabio/";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.DATA_PROP);

    Set<Resource> props = new MireotExtractor().fetch(stream("/ontology/fabio.rdf"),
        URI.create("http://purl.org/dc/terms/title"),
        cfg)
        .map((m) -> getResources(base,m))
        .orElse(new HashSet<>());

    assertTrue(props.contains(r(base, "hasSubtitle")));
    assertTrue(props.contains(r(base, "hasTranslatedTitle")));
    assertTrue(props.contains(ResourceFactory.createResource("http://purl.org/dc/terms/title")));

    assertEquals(7, props.size());

  }

  @Test
  public void testExtractIndividuals() {

    String base = "http://purl.org/spar/fabio/";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.INST);

    Set<Resource> props = new MireotExtractor().fetch(stream("/ontology/fabio.rdf"),
        URI.create(base + "DigitalStorageMedium"),
        cfg)
        .map((m) -> getResources(base,m))
        .orElse(new HashSet<>());

    assertTrue(props.contains(r(base, "cloud")));
    assertTrue(props.contains(r(base, "hard-drive")));

    assertEquals(11, props.size());
  }

  @Test
  public void testExtractIndividuals2() {

    String base = "http://www.omg.org/spec/LCC/Languages/ISO639-1-LanguageCodes/";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.INST);

    Set<Resource> props = new MireotExtractor().fetch(stream("/ontology/lcc.rdf"),
        URI.create("http://www.omg.org/spec/LCC/Languages/LanguageRepresentation/IndividualLanguage"),
        cfg)
        .map((m) -> getResources(base,m))
        .orElse(new HashSet<>());

    assertEquals(154, props.size());
  }

  @Test
  public void testExtractIndividuals3() {

    String base = "http://edu.mayo.kmdp/registry";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.INST);

    Set<Resource> props = new MireotExtractor().fetch(stream("/ontology/kr-registry.owl"),
        URI.create("http://www.omg.org/spec/API4KP/core#ConstructedLanguage"),
        cfg)
        .map((m) -> getResources(base,m))
        .orElse(new HashSet<>());

    //props.forEach((r) -> System.out.println(r.getURI()));
    assertEquals(10, props.size());

  }


  @Test
  public void testVersionExtraction() {

    String base = "http://org.test/labelsTest";

    MireotConfig cfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, base)
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.INST);

    Optional<Model> model = new MireotExtractor().fetch(stream("/ontology/version.rdf"),
        URI.create("https://foo"),
        cfg);

    //props.forEach((r) -> System.out.println(r.getURI()));

    StmtIterator s = model.get().listStatements(ResourceFactory.createResource(base),
        OWL2.versionIRI,
        (Resource) null);

    assertTrue(s.hasNext());
    assertTrue(s.nextStatement().getObject().toString().contains("20190108"));

  }

}
