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

import static edu.mayo.kmdp.util.CodeGenTestBase.deployResource;
import static edu.mayo.kmdp.util.CodeGenTestBase.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestBase.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestBase.initFolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.generator.plugin.TermsGeneratorPlugin;
import edu.mayo.kmdp.util.FileUtil;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TerminologySeriesPluginTest extends AbstractPluginTest {

  @TempDir
  Path tmp;

  private static final String tns = "http://test/generator";

  private File resources;
  private File genSource;
  private File target;

  private List<String> owlPath;

  @BeforeEach
  void initFolders() {
    File folder = tmp.toFile();
    resources = initFolder(folder,"resources");
    genSource = initFolder(folder,"generated-sources");
    target = initFolder(folder,"target");

    owlPath = new ArrayList<>();
    owlPath.add(deploySkosOntology("test.owl"));
    owlPath.add(deploySkosOntology("testNew.owl"));
  }

  @Test
  void testPlugin() {
    TermsGeneratorPlugin plugin =
        initPlugin(new File(genSource.getAbsolutePath() + "/xsd"),
            owlPath);
    plugin.setSourceCatalogPaths(
        Collections.singletonList(
            TerminologySeriesPluginTest.class.getResource("/test-catalog.xml").getPath()));
    plugin.setVersionPattern(".*/(.*)");
    plugin.execute();

    ensureSuccessCompile(genSource, genSource, target);

//    printSourceFile(new File(genSource,"xsd/test/generator/SCH1Series.java"), System.out);

    try {
      Class<?> seriesEnum = getNamedClass("test.generator.SCH1Series", target);
      assertNotNull(seriesEnum);
      assertTrue(seriesEnum.isEnum());

      assertEquals(4,seriesEnum.getEnumConstants().length);

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

  }


  private String deploySkosOntology(String fileName) {
    return deployResource("/" + fileName,
        resources,
        fileName.replace(".owl",".skos.rdf"),
        this::readBytes);
  }

  private byte[] readBytes(InputStream inputStream) {
    Optional<byte[]> bytes = FileUtil.readBytes(inputStream);
    if (!bytes.isPresent()) {
      fail();
    } else {
      return bytes.get();
    }
    return new byte[0];
  }

}
