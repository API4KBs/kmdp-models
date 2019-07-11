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

import static edu.mayo.kmdp.util.Util.uuid;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.mireot.EntityTypes;
import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotConfig.MireotParameters;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.PrintUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class Owl2SkosTest extends TestBase {

  private static String NS = "http://my.edu/test";

  @BeforeAll
  public static void init() {
    TestBase.init();
    PrintUtil.registerPrefix("tgt", NS + "#");
  }


  @Test
  public void testOWL2Skos() {
    String queryConcept = PREAMBLE +
        "SELECT ?C ?S ?B ?K " +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       skos:inScheme ?S; " +
        "       olex:isConceptOf ?K; " +
        "       skos:broader ?B. " +
        "   FILTER( ?B != ?C )." +
        "}";

    Owl2SkosConfig cfg = new Owl2SkosConfig().with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.LEX_CON);
    Model result = run(singletonList("/ontology/singleClass.owl"), cfg);

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    assertEquals(1, answers.size());

    assertTrue(answers.contains(a()
        .with("B", NS + "#" + uuid("test" + "_Top"))
        .with("C", NS + "#" + uuid("Klass"))
        .with("S", NS + "#" + uuid("test"))
        .with("K", "http://org.test/labelsTest#Klass")));
  }


  @Test
  public void testIrreflexiveBroader() {
    String queryConcept = PREAMBLE +
        "SELECT ?C" +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       skos:broader ?B. " +
        "   FILTER( ?B = ?C )." +
        "}";

    Owl2SkosConfig cfg = new Owl2SkosConfig().with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.LEX_CON);
    Model result = run(singletonList("/ontology/singleClass.owl"), cfg);

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    //System.out.println( answers );
    assertEquals(0, answers.size());

  }


  @Test
  public void testOWL2Term() {
    String queryTerm = PREAMBLE +
        "SELECT ?P ?L" +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       a olex:LexicalConcept; " +
        "       skos:prefLabel ?P; " +
        "       rdfs:label ?L; " +
        "       olex:isConceptOf ?K; " +
        "       " +
        "       olex:isEvokedBy [ " +
        "           a olex:LexicalEntry; " +
        "           olex:denotes ?K; " +
        "           olex:canonicalForm [ " +
        "               a olex:Form; " +
        "               olex:writtenRep ?lP; " +
        "           ]; " +
        "           olex:otherForm [ " +
        "               a olex:Form; " +
        "               olex:writtenRep ?lL; " +
        "           ]; " +
        "       ]. " +
        "   FILTER( xsd:string(?lL) = ?L ). " +
        "   FILTER( xsd:string(?lP) = ?P ). " +
        "}";

    Owl2SkosConfig cfg = new Owl2SkosConfig().with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.FULL);
    Model result = run(singletonList("/ontology/singleClass.owl"), cfg);

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryTerm, RDFNode::toString);

    assertEquals(1, answers.size());

    assertTrue(answers.contains(a().with("P", "Pref Way to name My Class")
        .with("L", "My Class")));
  }

  @Test
  public void testLabelCombinations() {
    String queryTerm = PREAMBLE +
        "SELECT ?C ?L ?K ?P" +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       a olex:LexicalConcept; " +
        "       rdfs:label ?L; " +
        "       skos:prefLabel ?P; " +
        "       olex:isConceptOf ?K; " +
        "       " +
        "       olex:isEvokedBy [ " +
        "           a olex:LexicalEntry; " +
        "           olex:denotes ?K; " +
        "           olex:canonicalForm [ " +
        "               a olex:Form; " +
        "               olex:writtenRep ?lP; " +
        "           ]; " +
        "           olex:otherForm [ " +
        "               a olex:Form; " +
        "               olex:writtenRep ?lL; " +
        "           ]; " +
        "       ]. " +
        "   FILTER( xsd:string(?lL) = ?L ). " +
        "   FILTER( xsd:string(?lP) = ?P ). " +
        "}";

    Owl2SkosConfig cfg = new Owl2SkosConfig().with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.FULL);
    Model result = run(singletonList("/ontology/labelsTest.owl"), cfg);

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryTerm, RDFNode::toString);
    assertEquals(4, answers.size());

    assertTrue(answers.contains(a().with("P", "a RDFS Labelled Class")
        .with("C", "http://my.edu/test#" + uuid("Labeled"))
        .with("K", "http://org.test/labelsTest#Labeled")
        .with("L", "a RDFS Labelled Class")));

    assertTrue(answers.contains(a().with("P", "Fragmented")
        .with("C", "http://my.edu/test#" + uuid("Fragmented"))
        .with("K", "http://org.test/labelsTest#Fragmented")
        .with("L", "Fragmented")));

    assertTrue(answers.contains(a().with("P", "Slashed")
        .with("C", "http://my.edu/test#" + uuid("Slashed"))
        .with("K", "http://org.test/labelsTest/Slashed")
        .with("L", "Slashed")));

    assertTrue(answers.contains(a().with("P", "a SKOS preffed class")
        .with("C", "http://my.edu/test#" + uuid("SkosLabeled"))
        .with("K", "http://org.test/labelsTest#SkosLabeled")
        .with("L", "base Label")));
  }


  @Test
  public void testConceptHierarchyGeneration() {
    String queryConcept = PREAMBLE +
        "SELECT ?C ?B " +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       skos:inScheme ?S; " +
        "       skos:broader ?B. " +
        "" +
        "   FILTER( ?B != ?C )." +
        "}";

    Owl2SkosConfig cfg = new Owl2SkosConfig().with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.CON);
    Model result = run(singletonList("/ontology/hierTest.owl"), cfg);

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#" + uuid("Parent"))
        .with("B", "http://my.edu/test#" + uuid("test_Top"))));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#" + uuid("Child"))
        .with("B", "http://my.edu/test#" + uuid("Parent"))));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#" + uuid("Sibling"))
        .with("B", "http://my.edu/test#" + uuid("Parent"))));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#" + uuid("GrandChild"))
        .with("B", "http://my.edu/test#" + uuid("Child"))));

  }


  @Test
  public void testConceptHierarchyGenerationOnProps() {
    String queryConcept = PREAMBLE +
        "SELECT ?C ?B " +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       skos:inScheme ?S; " +
        "       skos:broader ?B. " +
        "" +
        "   FILTER( ?B != ?C )." +
        "}";

    Owl2SkosConfig cfg = new Owl2SkosConfig().with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.CON);
    Model result = run(singletonList("/ontology/relHier.owl"), cfg);

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#" + uuid("childProp"))
        .with("B", "http://my.edu/test#" + uuid("parentProp"))));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#" + uuid("subDataProp"))
        .with("B", "http://my.edu/test#" + uuid("dataProp"))));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#" + uuid("parentProp"))
        .with("B", "http://my.edu/test#" + uuid("test" + "_Top"))));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#" + uuid("dataProp"))
        .with("B", "http://my.edu/test#" + uuid("test" + "_Top"))));

  }

  @Test
  public void testConceptHierarchyGenerationOnIndividuals() {
    String queryConcept = PREAMBLE +
        "SELECT ?C ?L ?P ?B " +
        " " +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       a olex:LexicalConcept; " +
        "       rdfs:label ?L; " +
        "       skos:prefLabel ?P; " +
        "       skos:broader ?B; " +
        "       olex:isConceptOf ?K. " +
        "   FILTER( ?B != ?C )." +
        "}";

    Owl2SkosConfig cfg = new Owl2SkosConfig().with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.FULL);
    Model result = run(singletonList("/ontology/individuals.owl"), cfg);

    //result.write(System.out);

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    assertTrue(answers.contains(a().with("P", "the one Thing@en")
        .with("B", "http://my.edu/test#" + uuid("test" + "_Top"))
        .with("C", "http://my.edu/test#" + uuid("Identi2"))
        .with("L", "the one Thing@en")));

    assertTrue(answers.contains(a().with("P", "the klass member")
        .with("B", "http://my.edu/test#" + uuid("Klass"))
        .with("C", "http://my.edu/test#" + uuid("klassMember"))
        .with("L", "the klass member")));

  }

  @Test
  public void testConceptGenerationWithTags() {
    String queryConcept = PREAMBLE +
        "SELECT ?C ?L ?N " +
//				" ?L ?P ?B " +
        " " +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       rdfs:label ?L; " +
//				"       skos:prefLabel ?P; " +
        "       skos:notation ?N; " +
        "}";

    MireotConfig mfg = new MireotConfig()
        .with(MireotParameters.BASE_URI,
            "https://www.omg.org/spec/LCC/Languages/LanguageRepresentation/")
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.INST);
    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.FULL);

    Model result = new MireotExtractor().fetch(
        Owl2SkosTest.class.getResourceAsStream("/ontology/lcc.rdf"),
        URI.create(
            "https://www.omg.org/spec/LCC/Languages/LanguageRepresentation/IndividualLanguage"),
        mfg
    ).flatMap((m) -> new Owl2SkosConverter().apply(m, cfg)).get();

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    assertTrue(answers.stream()
        .filter((m) -> "Italian".equals(m.get("L")))
        .anyMatch((x) -> "it^^urn:Alpha2Code".equals(x.get("N"))));
  }

  @Test
  public void testConceptGenerationWithTags2() {
    String queryConcept = PREAMBLE +
        "SELECT ?C ?L ?N ?D" +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       rdfs:label ?L; " +
        "       skos:notation ?N; " +
        "       rdfs:isDefinedBy ?D" +
        "}";

    MireotConfig mfg = new MireotConfig()
        .with(MireotParameters.BASE_URI,
            "https://www.omg.org/spec/LCC/Languages/LanguageRepresentation/")
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.INST);

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE,
            "https://www.omg.org/spec/LCC/Languages/ISO639-2-LanguageCodes/")
        .with(OWLtoSKOSTxParams.MODE, Modes.SKOS);

    Model result = new MireotExtractor()
        .fetch(
            Owl2SkosTest.class.getResourceAsStream("/ontology/lcc2.rdf"),
            URI.create(
                "https://www.omg.org/spec/LCC/Languages/LanguageRepresentation/IndividualLanguage"),
            mfg)
        .flatMap((m) ->
            new Owl2SkosConverter().apply(m, cfg))
        .get();

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);
    
    Map<String, String> klingon = answers.stream()
        .filter((m) -> "Klingon".equals(m.get("L")))
        .findFirst()
        .orElse(Collections.emptyMap());

    assertEquals(4, klingon.size());
    assertEquals("tlh^^urn:Alpha3Code", klingon.get("N"));
    assertEquals("https://www.omg.org/spec/LCC/Languages/ISO639-2-LanguageCodes/Klingon",
        klingon.get("D"));
  }

  @Test
  public void testConceptGenerationWithVariedURIs() {
    String queryConcept = PREAMBLE +
        "SELECT ?C " +
        "?L " +
        "?N " +
        " " +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       rdfs:label ?L; " +
        "       skos:prefLabel ?P; " +
        "       skos:notation ?N; " +
        "}";

    MireotConfig mfg = new MireotConfig()
        .with(MireotParameters.BASE_URI, "http://www.omg.org/spec/API4KP/core#")
        .with(MireotParameters.ENTITY_TYPE, EntityTypes.INST)
        .with(MireotParameters.MIN_DEPTH, 1);

    Owl2SkosConfig cfg = new Owl2SkosConfig().with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.SKOS);

    Model result = new MireotExtractor().fetch(
        Owl2SkosTest.class.getResourceAsStream("/ontology/kr-registry.owl"),
        URI.create("http://www.omg.org/spec/API4KP/core#ConstructedLanguage"),
        mfg)
        .flatMap((m) -> new Owl2SkosConverter().apply(m, cfg)).get();

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    Set<String> uris = answers.stream().map((m) -> m.get("C")).collect(Collectors.toSet());
    assertEquals(10, uris.size());

  }


  @Test
  public void testDCIdentifier() {
    String queryConcept = PREAMBLE +
        "SELECT ?C " +
        "       ?I " +
        "       ?P " +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       skos:prefLabel ?P; " +
        "       dct:identifier ?I; " +
        "}";

    Owl2SkosConfig cfg = new Owl2SkosConfig().with(OWLtoSKOSTxParams.TGT_NAMESPACE, NS)
        .with(OWLtoSKOSTxParams.MODE, Modes.SKOS);
    Model result = run(singletonList("/ontology/individuals.owl"), cfg);

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    answers.forEach((ans) -> {
      Optional<UUID> uid = Util.ensureUUID(ans.get("I"));
      assertTrue(uid.isPresent());
      assertTrue(ans.get("C").contains(uid.get().toString()));
    });
    assertEquals(3, answers.size());
  }


}
