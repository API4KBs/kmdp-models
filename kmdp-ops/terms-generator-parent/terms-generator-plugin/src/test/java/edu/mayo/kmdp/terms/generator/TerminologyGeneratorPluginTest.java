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

import static edu.mayo.kmdp.util.CodeGenTestBase.applyJaxb;
import static edu.mayo.kmdp.util.CodeGenTestBase.deploy;
import static edu.mayo.kmdp.util.CodeGenTestBase.deployResource;
import static edu.mayo.kmdp.util.CodeGenTestBase.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestBase.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestBase.initFolder;
import static edu.mayo.kmdp.util.CodeGenTestBase.printSourceFile;
import static edu.mayo.kmdp.util.CodeGenTestBase.showDirContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.generator.plugin.TermsGeneratorPlugin;
import edu.mayo.kmdp.terms.mireot.MireotExtractor;
import edu.mayo.kmdp.terms.skosifier.Owl2SkosConverter;
import edu.mayo.kmdp.util.FileUtil;
import example.MockTermsDirectory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

public class TerminologyGeneratorPluginTest {

  @TempDir
  Path tmp;

  private static final String tns = "http://org.test.terms/cito";

  private File resources;
  private File genSource;
  private File target;

  private File owl;

  @BeforeEach
  public void initFolders() {
    File folder = tmp.toFile();
    resources = initFolder(folder,"resources");
    genSource = initFolder(folder,"generated-sources");
    target = initFolder(folder,"target");

    owl = deploySkosOntology();
  }

  @Test
  public void testPlugin() {
    File folder = tmp.toFile();

    TermsGeneratorPlugin plugin = initPlugin(new File(genSource.getAbsolutePath() + "/xsd"));
    try {
      plugin.execute();
    } catch (MojoExecutionException | MojoFailureException e) {
      fail(e.getMessage());
    }

    testWithJaxb();

    showDirContent(folder);

    ensureSuccessCompile(genSource, genSource, target);

//    printSourceFile(new File(genSource.getAbsolutePath() + "/xsd/terms/test/org/cito/Cito.xsd"),
//        System.out);

    try {
      Class<?> info = getNamedClass("org.tempuri.test.Info", target);
      assertNotNull(info);

      Field fld = info.getDeclaredField("foo");
      assertNotNull(fld);
      Class<?> cito = fld.getType();

      assertNotNull(cito);
      assertEquals("Cito", cito.getSimpleName());
      assertTrue(cito.isEnum());

      assertEquals(44, cito.getEnumConstants().length);

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }

  private void testWithJaxb() {
    deploy(genSource, "/xsd/API4KP/api4kp/identifiers/identifiers.xsd");
    deploy(genSource, "/xsd/API4KP/api4kp/identifiers/bindings.xjb");
    deploy(genSource, "/xsd/api4kp-catalog.xml");

    deploy(TerminologyGeneratorPluginTest.class.getResourceAsStream("/schema.xsd"), genSource,
        "/xsd/schema.xsd");

    showDirContent(tmp.toFile());

    applyJaxb(Arrays.asList(new File(genSource.getPath() + "/xsd/schema.xsd"),
        new File(genSource.getPath() + "/xsd/terms")),
        Arrays.asList(new File(genSource.getPath() + "/xsd/terms")),
        genSource,
        null,
        new File(genSource.getPath() + "/xsd/api4kp-catalog.xml"),
        false,
        true);

    purge(genSource);
  }

  private void purge(File gen) {
    // In real scenarios, one would pass the episode file and the dependency to the already compiled datatypes.
    // For testing purposes, we let JaxB regenerate the files, then delete them before compilation
    File omg = new File(gen.getAbsolutePath() + "/org/omg");
    assertTrue(omg.isDirectory());
    FileUtil.delete(omg);
  }


  private File deploySkosOntology() {
    return deployResource("/cito.rdf", resources, "skosCito.rdf", this::owl2skos);
  }

  private byte[] owl2skos(InputStream inputStream) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    MireotExtractor exporter = new MireotExtractor(inputStream, "http://purl.org/spar/cito/");
    Owl2SkosConverter converter = new Owl2SkosConverter(tns);

    exporter.fetch("http://purl.org/spar/cito/cites", false)
        .flatMap((ext) -> converter.run(ext, true, true))
        .map((model) -> model.write(baos));

    return baos.toByteArray();
  }


  private TermsGeneratorPlugin initPlugin(File genSrc) {
    TermsGeneratorPlugin plugin = new TermsGeneratorPlugin();

    plugin.setReason(false);
    plugin.setJaxb(true);
    plugin.setTermsProvider(MockTermsDirectory.provider);
    plugin.setOutputDirectory(genSrc);
    plugin.setOwlFiles(Collections.singletonList(owl.getAbsolutePath()));
    plugin.setSourceCatalogPath("/test-catalog.xml");

    return plugin;
  }
}
