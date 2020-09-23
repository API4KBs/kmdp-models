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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.MockTermsJsonAdapter;
import edu.mayo.kmdp.terms.MockTermsXMLAdapter;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.CLOSURE_MODE;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class GenerateWithImportsTest {

  @TempDir
  public Path tmp;

  @Test
  public void testGenerateConceptsHierarchyWithImports() {
    ConceptGraph graph = doGenerate(CLOSURE_MODE.IMPORTS);
    assertNotNull(graph);
    assertEquals(2, graph.getConceptSchemes().size());

    File src = initFolder(tmp.toFile(), "src");
    File target = initFolder(tmp.toFile(), "tgt");

    new JavaEnumTermsGenerator().generate(graph, new EnumGenerationConfig()
            .with(EnumGenerationParams.JSON_ADAPTER, MockTermsJsonAdapter.class.getName())
            .with(EnumGenerationParams.XML_ADAPTER, MockTermsXMLAdapter.class.getName()),
        src);
    showDirContent(tmp.toFile(), true);

    ensureSuccessCompile(src, src, target);

    Class<?> subScheme = getNamedClass("org.foo.child.Sub_Scheme", target);
    assertTrue(subScheme.isEnum());
    Class<?> supScheme = getNamedClass("foo.test.taxonomies.parent.Top_Of_Super",
        target);
    assertTrue(supScheme.isEnum());

    Object t = Arrays.stream(subScheme.getEnumConstants())
        .filter(x -> ((Enum<?>)x).name().contains("Some"))
        .findAny().orElse(null);
    assertTrue(t instanceof Term);

    try {
      assertEquals("123", t.getClass().getMethod("getTag").invoke(t));

      Object ref = subScheme.getMethod("getReferentId").invoke(t);
      assertEquals("http://foo.org/ontologies/referent/AThing", ref.toString());

      Object anx = subScheme.getMethod("getAncestors").invoke(t);
      assertTrue(anx.getClass().isArray());
      Object[] ancestors = (Object[]) anx;
      assertEquals(1, ancestors.length);

      Object f = ancestors[0];
      assertTrue(f instanceof Term);
      // avoid dealing with classloaders
      assertEquals(supScheme.getName(), f.getClass().getName());

      assertEquals("000", f.getClass().getMethod("getTag").invoke(f));
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }


  @Test
  public void testGenerateConceptsHierarchyWithIncludes() {
    ConceptGraph graph = doGenerate(CLOSURE_MODE.INCLUDES);
    assertNotNull(graph);
    assertEquals(2, graph.getConceptSchemes().size());

    File src = initFolder(tmp.toFile(), "src");
    File target = initFolder(tmp.toFile(), "tgt");

    new JavaEnumTermsGenerator().generate(graph, new EnumGenerationConfig()
            .with(EnumGenerationParams.JSON_ADAPTER, MockTermsJsonAdapter.class.getName())
            .with(EnumGenerationParams.XML_ADAPTER, MockTermsXMLAdapter.class.getName()),
        src);

    //showDirContent(tmp.toFile(), true);

    //printSourceFile(new File(tmp.toFile(),"/src/org/foo/child/Sub_Scheme.java"),System.out);
    ensureSuccessCompile(src, src, target);

    Class<?> subScheme = getNamedClass("org.foo.child.Sub_Scheme", target);
    assertTrue(subScheme.isEnum());
    Class<?> supScheme = getNamedClass("foo.test.taxonomies.parent.Top_Of_Super",
        target);
    assertTrue(supScheme.isEnum());


    try {
      assertEquals(3,subScheme.getEnumConstants().length);

      Object[] enums = subScheme.getEnumConstants();
      Arrays.sort(enums);

      Object t1 = subScheme.getEnumConstants()[0];
      String tag1 = (String) t1.getClass().getMethod("getTag").invoke(t1);

      Object t2 = Arrays.stream(subScheme.getEnumConstants())
          .filter(x -> ((Enum<?>)x).name().contains("Some"))
          .findAny().orElse(null);
      assertNotNull(t2);
      String tag2 = (String) t2.getClass().getMethod("getTag").invoke(t2);

      Object t3 = Arrays.stream(subScheme.getEnumConstants())
          .filter(x -> ((Enum<?>)x).name().contains("Another"))
          .findAny().orElse(null);
      assertNotNull(t3);
      String tag3 = (String) t3.getClass().getMethod("getTag").invoke(t3);

      assertEquals("000", tag1);
      assertEquals("123", tag2);
      assertEquals("124", tag3);

      Object anx2 = subScheme.getMethod("getAncestors").invoke(t2);
      assertTrue(anx2.getClass().isArray());
      Object[] ancestors2 = (Object[]) anx2;

      Object anx3 = subScheme.getMethod("getAncestors").invoke(t3);
      assertTrue(anx3.getClass().isArray());
      Object[] ancestors3 = (Object[]) anx3;

      assertEquals(1, ancestors2.length);
      assertEquals(1, ancestors3.length);

      assertSame(t1,ancestors2[0]);
      assertSame(t1,ancestors3[0]);

      Object prn = ancestors2[0];
      assertNotNull(prn);

      Object sUri = prn.getClass().getMethod("getNamespace").invoke(prn);
      assertTrue(sUri instanceof ResourceIdentifier);

      assertEquals("https://foo.org/child/subScheme",
          ((ResourceIdentifier) sUri).getResourceId().toString());

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }



  public static ConceptGraph doGenerate(CLOSURE_MODE closureMode) {
    try {
      OWLOntologyManager owlOntologyManager = TestHelper.initOWLManager();

      owlOntologyManager.loadOntologyFromOntologyDocument(
          GenerateWithImportsTest.class.getResourceAsStream("/supVocab.rdf"));
      OWLOntology o = owlOntologyManager.loadOntologyFromOntologyDocument(
          GenerateWithImportsTest.class.getResourceAsStream("/subVocab.rdf"));

      return new SkosTerminologyAbstractor()
          .traverse(o, new SkosAbstractionConfig()
              .with(SkosAbstractionParameters.REASON, false)
              .with(SkosAbstractionParameters.ENFORCE_CLOSURE, true)
              .with(SkosAbstractionParameters.CLOSURE_MODE, closureMode));
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

}
