/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.terms.generator;

import static edu.mayo.kmdp.util.CodeGenTestUtil.applyJaxb;
import static edu.mayo.kmdp.util.CodeGenTestUtil.deploy;
import static edu.mayo.kmdp.util.CodeGenTestUtil.deployResource;
import static edu.mayo.kmdp.util.CodeGenTestUtil.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestUtil.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestUtil.initFolder;
import static edu.mayo.kmdp.util.CodeGenTestUtil.showDirContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.generator.plugin.TermsGeneratorPlugin;
import edu.mayo.kmdp.terms.mireot.MireotConfig;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConfig.OWLtoSKOSTxParams;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.FileUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TerminologyGeneratorPluginTest extends AbstractPluginTest {

  @TempDir
  Path tmp;

  private static final String tns = "http://org.test.terms/cito";

  private File resources;
  private File genSource;
  private File target;

  private String owlPath;

  @BeforeEach
  public void initFolders() {
    File folder = tmp.toFile();
    resources = initFolder(folder, "resources");
    genSource = initFolder(folder, "generated-sources");
    target = initFolder(folder, "target");

    owlPath = deploySkosOntology();
  }

  @Test
  void testPlugin() {
    TermsGeneratorPlugin plugin = initPlugin(
        new File(genSource.getAbsolutePath() + "/xsd"),
        Collections.singletonList(owlPath));
    plugin.setSourceCatalogPaths(
        Collections.singletonList(
            TerminologyGeneratorPluginTest.class.getResource("/test-catalog.xml").getPath()));
    plugin.execute();

    testWithJaxb();

    ensureSuccessCompile(genSource, genSource, target);

    try {
      Class<?> info = getNamedClass("org.tempuri.test.Info", target);
      assertNotNull(info);

      Field fld = info.getDeclaredField("foo");
      assertNotNull(fld);
      Class<?> cito = fld.getType();

      assertNotNull(cito);
      assertEquals("ICito", cito.getSimpleName());
      assertTrue(cito.isInterface());

      Method setter = info.getMethod("setFoo", cito);
      assertNotNull(setter);

      Class<?> citoImpl = getNamedClass("terms.test.org.cito.Cito", target);
      assertNotNull(citoImpl);
      assertTrue(citoImpl.isEnum());
      Class[] intfs = citoImpl.getInterfaces();
      assertEquals(2, intfs.length);

      assertEquals(44, citoImpl.getEnumConstants().length);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }

  private void testWithJaxb() {
    deploy(genSource, "/xsd/API4KP/api4kp/id/id.xsd");
    deploy(genSource, "/xsd/API4KP/api4kp/id/bindings.xjb");
    deploy(genSource, "/xsd/API4KP/api4kp/datatypes/datatypes.xsd");
    deploy(genSource, "/xsd/API4KP/api4kp/datatypes/bindings.xjb");

    deploy(genSource, "/xsd/api4kp-catalog.xml");

    deploy(TerminologyGeneratorPluginTest.class.getResourceAsStream("/schema.xsd"), genSource,
        "/xsd/schema.xsd");

    showDirContent(tmp.toFile());

    applyJaxb(
        Arrays.asList(
            new File(genSource.getPath() + "/xsd/schema.xsd"),
            new File(genSource.getPath() + "/xsd/terms")),
        Arrays.asList(
            new File(genSource.getPath() + "/xsd/terms")),
        genSource,
        null,
        Collections.singletonList(new File(genSource.getPath() + "/xsd/api4kp-catalog.xml")),
        false,
        true);

    showDirContent(tmp.toFile());

    purge(genSource);
  }

  private void purge(File gen) {
    // In real scenarios, one would pass the episode file and the dependency to the already compiled datatypes.
    // For testing purposes, we let JaxB regenerate the files, then delete them before compilation
    File omg = new File(gen.getAbsolutePath() + "/https");
    assertTrue(omg.isDirectory());
    FileUtil.delete(omg);
  }


  private String deploySkosOntology() {
    return deployResource("/cito.rdf", resources, "skosCito.rdf", this::owl2skos);
  }

  private byte[] owl2skos(InputStream inputStream) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    Owl2SkosConfig cfg = new Owl2SkosConfig()
        .with(OWLtoSKOSTxParams.TGT_NAMESPACE, tns)
        .with(OWLtoSKOSTxParams.TOP_CONCEPT_NAME, "Cito")
        .with(OWLtoSKOSTxParams.ADD_IMPORTS, Boolean.TRUE);

    new MireotExtractor()
        .fetch(inputStream,
            URI.create("http://purl.org/spar/cito/cites"),
            new MireotConfig())
        .flatMap((ext) -> new Owl2SkosConverter().apply(ext, cfg))
        .map((model) -> model.write(baos));

    return baos.toByteArray();
  }
}
