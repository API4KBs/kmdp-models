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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.example.MockTermsDirectory;
import edu.mayo.kmdp.terms.generator.SkosTerminologyAbstractor.ConceptGraph;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class GenerateWithImportsTest {

  private static SkosTerminologyAbstractor.ConceptGraph graph;

  @TempDir
  public Path tmp;

  @BeforeAll
  public static void init() {
    graph = doGenerate();
  }


  @Test
  public void testGenerateConceptsHierarchyWithImports() {
    ConceptGraph graph = doGenerate();
    assertEquals(2, graph.getConceptSchemes().size());

    File src = initFolder(tmp.toFile(), "src");
    File target = initFolder(tmp.toFile(), "tgt");

    new JavaEnumTermsGenerator().generate(graph, new EnumGenerationConfig()
            .with(EnumGenerationParams.TERMS_PROVIDER, MockTermsDirectory.provider),
        src);
    showDirContent(tmp.toFile(), true);

    ensureSuccessCompile(src, src, target);

    Class<?> subScheme = getNamedClass("org.foo.child.subscheme.Sub_Scheme", target);
    assertTrue(subScheme.isEnum());
    Class<?> supScheme = getNamedClass("foo.test.taxonomies.parent.superscheme.Top_Of_Super", target);
    assertTrue(supScheme.isEnum());

    Object t = subScheme.getEnumConstants()[0];
    assertTrue(t instanceof Term);

    try {
      assertEquals("123", t.getClass().getMethod("getTag").invoke(t));

      Object ref = subScheme.getMethod("getRef").invoke(t);
      assertEquals("http://foo.org/ontologies/referent/AThing", ref.toString());

      Object anx = subScheme.getMethod("getAncestors").invoke(t);
      assertTrue(anx.getClass().isArray());
      Object[] ancestors = (Object[]) anx;
      assertEquals(1,ancestors.length);

      Object f = ancestors[0];
      assertTrue(f instanceof Term);
      // avoid dealing with classloaders
      assertEquals(supScheme.getName(),f.getClass().getName());

      assertEquals("000", f.getClass().getMethod("getTag").invoke(f));
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      fail(e.getMessage());
    }

  }


  public static SkosTerminologyAbstractor.ConceptGraph doGenerate() {
    try {
      OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
      owlOntologyManager.loadOntologyFromOntologyDocument(
          GenerateWithImportsTest.class.getResourceAsStream("/supVocab.rdf"));
      OWLOntology o = owlOntologyManager.loadOntologyFromOntologyDocument(
          GenerateWithImportsTest.class.getResourceAsStream("/subVocab.rdf"));

      return new SkosTerminologyAbstractor()
          .traverse(o, new SkosAbstractionConfig()
              .with(SkosAbstractionParameters.REASON, false)
              .with(SkosAbstractionParameters.ENFORCE_CLOSURE, true));
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

}
