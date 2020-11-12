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

import static edu.mayo.kmdp.util.CodeGenTestBase.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestBase.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestBase.initFolder;
import static edu.mayo.kmdp.util.CodeGenTestBase.showDirContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.MockTermsJsonAdapter;
import edu.mayo.kmdp.terms.MockTermsXMLAdapter;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import edu.mayo.kmdp.terms.generator.internal.InternalTerm;
import edu.mayo.kmdp.terms.generator.internal.MutableConceptScheme;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.terms.ConceptScheme;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

class TerminologyGeneratorTest {

  private static ConceptGraph graph;

  @TempDir
  Path tmp;

  @BeforeAll
  static void init() {
    graph = doGenerate();
  }

  @Test
  void testGenerateConceptSchemes() {

    assertEquals(1, graph.getConceptSchemes().size());

    ConceptScheme<?> cs = graph.getConceptSchemes().iterator().next();

    assertEquals("SCH1", cs.getLabel());
    assertEquals("http://test/generator#concept_scheme1", cs.getId().toString());
    assertEquals("http://test/generator/v20180210#concept_scheme1", cs.getVersionId().toString());
  }

  @Test
  void testGenerateConceptsWithReasoning() {
    assertEquals(1, graph.getConceptSchemes().size());

    Stream<Term> concepts = graph.getConceptSchemes().iterator().next().getConcepts();

    assertEquals(3, concepts.count());
  }

  @Test
  void testGenerateConceptsPopulated() {
    assertEquals(1, graph.getConceptSchemes().size());

    Stream<Term> concepts = graph.getConceptSchemes().iterator().next().getConcepts();

    concepts.forEach((concept) -> {
      assertNotNull(concept.getTag());
      assertNotNull(concept.getNamespaceUri());
      assertNotNull(concept.getLabel());
    });
  }


  @Test
  void testGenerateConceptsHierarchy() {
    assertEquals(1, graph.getConceptSchemes().size());

    Optional<Term> cd = graph.getConceptSchemes().iterator().next()
        .getConcepts()
        .filter((x) -> x.getTag().equals("sub_sub"))
        .findAny();

    assertTrue(cd.isPresent());

    InternalTerm inner = (InternalTerm) cd.get();
    MutableConceptScheme cs = (MutableConceptScheme) inner
        .getScheme();
    int n = cs.getAncestors(cd.get()).size();
    assertEquals(1, n);

  }


  @Test
  void testClassCompilation() {
    try {
      File folder = tmp.toFile();

      File src = initFolder(folder, "src");
      File target = initFolder(folder, "output");

      new JavaEnumTermsGenerator().generate(graph,
          new EnumGenerationConfig()
              .with(EnumGenerationParams.JSON_ADAPTER, MockTermsJsonAdapter.class.getName())
              .with(EnumGenerationParams.XML_ADAPTER, MockTermsXMLAdapter.class.getName())
              .with(EnumGenerationParams.PACKAGE_OVERRIDES,
                  "test.generator.v20180210=org.foo.test"),
          src);
      showDirContent(folder,true);

      ensureSuccessCompile(src, src, target);

      Class<?> scheme = getNamedClass("org.foo.test.SCH1", target);

      Field ns = scheme.getField("SCHEME_ID");
      assertEquals("concept_scheme1", ns.get(null));

      Object code = scheme.getEnumConstants()[0];
      assertTrue(code instanceof Term);
      assertEquals("6789", ((Term) code).getTag());

      assertEquals(0, scheme.getAnnotations().length);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  @Test
  void testClassCompilationWithVersionedPackage() {
    try {
      File folder = tmp.toFile();

      File src = initFolder(folder,"src");
      File target = initFolder(folder,"output");

      new JavaEnumTermsGenerator().generate(graph,
          new EnumGenerationConfig()
              .with(EnumGenerationParams.JSON_ADAPTER, MockTermsJsonAdapter.class.getName())
              .with(EnumGenerationParams.XML_ADAPTER, MockTermsXMLAdapter.class.getName()),
          src);
      //showDirContent(folder);

      ensureSuccessCompile(src, src, target);

      Class<?> scheme = getNamedClass("test.generator.v20180210.SCH1", target);

      assertEquals("test.generator.v20180210", scheme.getPackage().getName());

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  static ConceptGraph doGenerate() {
    try {
      OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
      OWLOntology o = owlOntologyManager.loadOntologyFromOntologyDocument(
          TerminologyGeneratorTest.class.getResourceAsStream("/test.owl"));

      return new SkosTerminologyAbstractor()
          .traverse(o, new SkosAbstractionConfig()
              .with(SkosAbstractionParameters.REASON, true)
              .with(SkosAbstractionParameters.VERSION_PATTERN, ".*/(.*)"));
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

}
