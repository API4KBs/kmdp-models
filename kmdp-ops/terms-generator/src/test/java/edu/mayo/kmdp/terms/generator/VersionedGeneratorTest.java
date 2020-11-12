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
import edu.mayo.kmdp.terms.generator.internal.VersionedConceptGraph;
import edu.mayo.kmdp.util.DateTimeUtil;
import edu.mayo.kmdp.util.StreamUtil;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.series.Versionable;
import org.omg.spec.api4kp._20200801.terms.VersionableTerm;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

class VersionedGeneratorTest {

  private static ConceptGraph graph;

  @TempDir
  Path tmp;

  @BeforeAll
  static void init() {
    graph = doGenerate();
  }

  @Test
  void testGenerateConceptSchemes() {
    assertEquals(3, graph.getConceptSchemes().size());
  }


  @Test
  @SuppressWarnings({"deprecation","unchecked"})
  void testClassCompilationWithVersionedPackage() {
    try {
      File folder = tmp.toFile();

      File src = initFolder(folder, "src");
      File target = initFolder(folder, "output");

      new JavaEnumTermsGenerator().generate(graph,
          new EnumGenerationConfig()
              .with(EnumGenerationParams.JSON_ADAPTER, MockTermsJsonAdapter.class.getName())
              .with(EnumGenerationParams.XML_ADAPTER, MockTermsXMLAdapter.class.getName()),
          src);

      showDirContent(folder, true);

      ensureSuccessCompile(src, src, target);

      Class<?> scheme0 = getNamedClass("test.generator.SCH1", target);
      Class<?> scheme1 = getNamedClass("test.generator.v20180210.SCH1", target);
      Class<?> scheme2 = getNamedClass("test.generator.v20190605.SCH1", target);
      Class<?> scheme3 = getNamedClass("test.generator.snapshot.SCH1", target);
      Class<?> sseries = getNamedClass("test.generator.SCH1Series", target);

      assertTrue(scheme0.isInterface());
      assertTrue(scheme1.isEnum());
      assertTrue(scheme2.isEnum());
      assertTrue(scheme3.isEnum());

      assertTrue(sseries.isEnum());
      Optional<Enum> x = Arrays.stream(sseries.getEnumConstants())
          .map(Enum.class::cast)
          .filter(e -> "Specific_Concept".equalsIgnoreCase(e.name()))
          .findFirst();
      assertTrue(x.isPresent());

      Field versionURIs = sseries.getDeclaredField("schemeVersionIdentifiers");
      assertNotNull(versionURIs);
      List<?> v = (List<?>) versionURIs.get(null);
      assertEquals(3,v.size());

      Field latestVersionURI = sseries.getDeclaredField("latestVersionIdentifier");
      assertNotNull(latestVersionURI);
      URI u = (URI) latestVersionURI.get(null);
      assertEquals(URI.create("http://test/generator/SNAPSHOT"),u);

      assertTrue(x.get() instanceof Series);
      List<?> versions = ((Series<?,?>)x.get()).getVersions();

      versions.forEach(ver -> assertTrue(ver instanceof Versionable));
      List<Versionable> versionables = versions.stream()
          .flatMap(StreamUtil.filterAs(Versionable.class))
          .collect(Collectors.toList());

      List<Date> releases = versionables.stream()
          .map(Versionable::getVersionEstablishedOn)
          .collect(Collectors.toList());

      List<String> releaseVersions = versionables.stream()
          .map(Versionable::getVersionIdentifier)
          .map(VersionIdentifier::getVersionTag)
          .collect(Collectors.toList());

      assertTrue(releases.get(0).getTime() > releases.get(1).getTime());
      assertTrue(releases.get(1).getTime() > releases.get(2).getTime());

      String today = new SimpleDateFormat("yyyyMMdd").format(DateTimeUtil.today());
      assertTrue(releaseVersions.get(0).startsWith(today));
      assertEquals("v20190605", releaseVersions.get(1));
      assertEquals("v20180210", releaseVersions.get(2));


      Field f = sseries.getDeclaredField("schemeVersions");
      Object schemeVersions = f.get(null);
      assertNotNull(schemeVersions);
      assertTrue(schemeVersions instanceof List);

      List<String> sv = (List<String>) schemeVersions;
      assertEquals(new HashSet<>(releaseVersions),new HashSet<>(sv));

      Date effectiveDate = DateTimeUtil.parseDate(sv.get(1).substring(1),"yyyyMMdd");
      assertNotNull(effectiveDate);
      Optional<?> version = ((Series<?,?>)x.get()).asOf(effectiveDate);
      assertTrue(version.isPresent());
      assertTrue(version.get() instanceof VersionableTerm);
      VersionableTerm<?,?> vt = (VersionableTerm<?,?>) version.get();
      assertEquals(vt.getVersionEstablishedOn(), effectiveDate);

      Date initialDate = DateTimeUtil.parseDate(sv.get(2).substring(1),"yyyyMMdd");
      assertEquals(vt.getVersionIdentifier().getEstablishedOn(), initialDate);

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  static ConceptGraph doGenerate() {
    try {
      OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
      OWLOntology o1 = owlOntologyManager.loadOntologyFromOntologyDocument(
          VersionedGeneratorTest.class.getResourceAsStream("/test.owl"));
      OWLOntology o2 = owlOntologyManager.loadOntologyFromOntologyDocument(
          VersionedGeneratorTest.class.getResourceAsStream("/testNew.owl"));
      OWLOntology o3 = owlOntologyManager.loadOntologyFromOntologyDocument(
          VersionedGeneratorTest.class.getResourceAsStream("/testSnapshot.owl"));

      SkosTerminologyAbstractor abstractor = new SkosTerminologyAbstractor();
      ConceptGraph cg1 = abstractor.traverse(o1, new SkosAbstractionConfig()
          .with(SkosAbstractionParameters.VERSION_PATTERN, ".*/(v.*)$")
          .with(SkosAbstractionParameters.DATE_PATTERN, "'v'yyyyMMdd")
          .with(SkosAbstractionParameters.REASON, true));
      ConceptGraph cg2 = abstractor.traverse(o2, new SkosAbstractionConfig()
          .with(SkosAbstractionParameters.VERSION_PATTERN, ".*/(v.*)$")
          .with(SkosAbstractionParameters.DATE_PATTERN, "'v'yyyyMMdd")
          .with(SkosAbstractionParameters.REASON, true));
      ConceptGraph cg3 = abstractor.traverse(o3, new SkosAbstractionConfig()
          .with(SkosAbstractionParameters.VERSION_PATTERN, ".*/(.*)$")
          .with(SkosAbstractionParameters.REASON, true));

      return new VersionedConceptGraph(cg1).merge(cg2).merge(cg3);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

}
