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

import edu.mayo.kmdp.terms.mireot.EntityTypes;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.util.JenaUtil;
import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.PrintUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class Owl2SkosTest extends TestBase {


  private Owl2SkosConverter cv;

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

    cv = new Owl2SkosConverter(NS, Modes.CON, Modes.LEX);
    Model result = run(cv, singletonList("/ontology/singleClass.owl"));

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    assertEquals(1, answers.size());

    assertTrue(answers.contains(a().with("B", NS + "#test_Scheme_Top")
        .with("C", NS + "#Klass")
        .with("S", NS + "#test_Scheme")
        .with("K", "http://org.test/labelsTest#Klass")));
  }


  @Test
  public void testReflexiveBroader() {
    String queryConcept = PREAMBLE +
        "SELECT ?C" +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       skos:broader ?B. " +
        "   FILTER( ?B = ?C )." +
        "}";

    cv = new Owl2SkosConverter(NS, Modes.CON, Modes.LEX);
    Model result = run(cv, singletonList("/ontology/singleClass.owl"));

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    //System.out.println( answers );
    assertEquals(1, answers.size());

    assertTrue(answers.contains(a().with("C", NS + "#Klass")));
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

    cv = new Owl2SkosConverter(NS, Modes.FULL);
    Model result = run(cv, singletonList("/ontology/singleClass.owl"));

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

    cv = new Owl2SkosConverter(NS, Modes.FULL);
    Model result = run(cv, singletonList("/ontology/labelsTest.owl"));

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryTerm, RDFNode::toString);
    assertEquals(4, answers.size());

    assertTrue(answers.contains(a().with("P", "a RDFS Labelled Class")
        .with("C", "http://my.edu/test#Labeled")
        .with("K", "http://org.test/labelsTest#Labeled")
        .with("L", "a RDFS Labelled Class")));

    assertTrue(answers.contains(a().with("P", "Fragmented")
        .with("C", "http://my.edu/test#Fragmented")
        .with("K", "http://org.test/labelsTest#Fragmented")
        .with("L", "Fragmented")));

    assertTrue(answers.contains(a().with("P", "Slashed")
        .with("C", "http://my.edu/test#Slashed")
        .with("K", "http://org.test/labelsTest/Slashed")
        .with("L", "Slashed")));

    assertTrue(answers.contains(a().with("P", "a SKOS preffed class")
        .with("C", "http://my.edu/test#SkosLabeled")
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

    cv = new Owl2SkosConverter(NS, Modes.CON);
    Model result = run(cv, singletonList("/ontology/hierTest.owl"));

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#Parent")
        .with("B", "http://my.edu/test#test_Scheme_Top")));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#Child")
        .with("B", "http://my.edu/test#Parent")));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#Sibling")
        .with("B", "http://my.edu/test#Parent")));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#GrandChild")
        .with("B", "http://my.edu/test#Child")));

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

    cv = new Owl2SkosConverter(NS, Modes.CON);
    Model result = run(cv, singletonList("/ontology/relHier.owl"));

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#childProp")
        .with("B", "http://my.edu/test#parentProp")));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#subDataProp")
        .with("B", "http://my.edu/test#dataProp")));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#parentProp")
        .with("B", "http://my.edu/test#test_Scheme_Top")));

    assertTrue(answers.contains(a().with("C", "http://my.edu/test#dataProp")
        .with("B", "http://my.edu/test#test_Scheme_Top")));

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

    cv = new Owl2SkosConverter(NS, Modes.FULL);
    Model result = run(cv, singletonList("/ontology/individuals.owl"));

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    assertTrue(answers.contains(a().with("P", "the one Thing@en")
        .with("B", "http://my.edu/test#test_Scheme_Top")
        .with("C", "http://my.edu/test#Identi2")
        .with("L", "the one Thing@en")));

    assertTrue(answers.contains(a().with("P", "the klass member")
        .with("B", "http://my.edu/test#Klass")
        .with("C", "http://my.edu/test#klassMember")
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

    cv = new Owl2SkosConverter(NS, Modes.FULL);
    Model result = MireotExtractor.extract(
        Owl2SkosTest.class.getResourceAsStream("/ontology/lcc.rdf"),
        "http://www.omg.org/spec/LCC/Languages/LanguageRepresentation/IndividualLanguage",
        EntityTypes.INST, 0, -1)
        .flatMap((m) -> cv.run(m, false, false)).get();

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);
    System.out.println(answers);

    assertTrue(answers.stream()
        .filter((m) -> "Italian".equals(m.get("L")))
        .anyMatch((x) -> "it".equals(x.get("N"))));
  }

  @Test
  public void testConceptGenerationWithVariedURIs() {
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

    cv = new Owl2SkosConverter(NS, Modes.FULL);
    Model result = MireotExtractor.extract(
        Owl2SkosTest.class.getResourceAsStream("/ontology/kr-registry.owl"),
        "http://www.omg.org/spec/API4KP/core#ConstructedLanguage",
        EntityTypes.INST, 1, -1)
        .flatMap((m) -> cv.run(m, false, false)).get();

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    System.out.println(answers);

    Set<String> uris = answers.stream().map((m) -> m.get("C")).collect(Collectors.toSet());
    assertEquals(10, uris.size());
//		assertTrue(answers.stream()
//				.filter( (m) -> "Italian".equals( m.get( "L" ) ) )
//				.anyMatch( (x) -> "it".equals( x.get( "N" ) ) ) );
  }


  @Test
  public void testDCIdentifier() {
    String queryConcept = PREAMBLE +
        "SELECT ?C " +
        "       ?I " +
        " " +
        "WHERE { " +
        "   ?C  a skos:Concept; " +
        "       dct:identifier ?I; " +
        "}";

    cv = new Owl2SkosConverter(NS, Modes.SKOS);
    Model result = run(cv, singletonList("/ontology/individuals.owl"));

    Set<Map<String, String>> answers = JenaUtil.askQuery(result, queryConcept, RDFNode::toString);

    assertEquals(1, answers.size());
  }


}
