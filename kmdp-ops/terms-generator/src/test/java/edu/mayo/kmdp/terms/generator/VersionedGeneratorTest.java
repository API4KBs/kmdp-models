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
package edu.mayo.kmdp.terms.generator;

import static edu.mayo.kmdp.util.CodeGenTestBase.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestBase.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestBase.initFolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.MockTermsJsonAdapter;
import edu.mayo.kmdp.terms.MockTermsXMLAdapter;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig.EnumGenerationParams;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig;
import edu.mayo.kmdp.terms.generator.config.SkosAbstractionConfig.SkosAbstractionParameters;
import edu.mayo.kmdp.terms.generator.internal.ConceptGraph;
import edu.mayo.kmdp.terms.generator.internal.VersionedConceptGraph;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class VersionedGeneratorTest {

  private static ConceptGraph graph;

  @TempDir
  public Path tmp;

  @BeforeAll
  public static void init() {
    graph = doGenerate();
  }

  @Test
  public void testGenerateConceptSchemes() {
    assertEquals(2, graph.getConceptSchemes().size());
  }


  @Test
  public void testClassCompilationWithVersionedPackage() {
    try {
      File folder = tmp.toFile();

      File src = initFolder(folder, "src");
      File target = initFolder(folder, "output");

      new JavaEnumTermsGenerator().generate(graph,
          new EnumGenerationConfig()
              .with(EnumGenerationParams.JSON_ADAPTER, MockTermsJsonAdapter.class.getName())
              .with(EnumGenerationParams.XML_ADAPTER, MockTermsXMLAdapter.class.getName()),
          src);

//      showDirContent(folder);

      ensureSuccessCompile(src, src, target);

      Class scheme0 = getNamedClass("test.generator.SCH1", target);
      Class scheme1 = getNamedClass("test.generator.v20180210.SCH1", target);
      Class scheme2 = getNamedClass("test.generator.v20191201.SCH1", target);

      assertTrue(scheme0.isInterface());
      assertTrue(scheme1.isEnum());
      assertTrue(scheme2.isEnum());

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  public static ConceptGraph doGenerate() {
    try {
      OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
      OWLOntology o1 = owlOntologyManager.loadOntologyFromOntologyDocument(
          VersionedGeneratorTest.class.getResourceAsStream("/test.owl"));
      OWLOntology o2 = owlOntologyManager.loadOntologyFromOntologyDocument(
          VersionedGeneratorTest.class.getResourceAsStream("/testNew.owl"));

      SkosTerminologyAbstractor abstractor = new SkosTerminologyAbstractor();
      ConceptGraph cg1 = abstractor.traverse(o1, new SkosAbstractionConfig()
          .with(SkosAbstractionParameters.REASON, true));
      ConceptGraph cg2 = abstractor.traverse(o2, new SkosAbstractionConfig()
          .with(SkosAbstractionParameters.REASON, true));

      return new VersionedConceptGraph(cg1).merge(cg2);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

}
