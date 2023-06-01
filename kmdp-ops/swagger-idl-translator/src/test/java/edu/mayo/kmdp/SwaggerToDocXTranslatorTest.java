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
package edu.mayo.kmdp;

import static edu.mayo.kmdp.LoaderHelper.idSource;
import static edu.mayo.kmdp.LoaderHelper.inferSource;
import static edu.mayo.kmdp.LoaderHelper.karSource;
import static edu.mayo.kmdp.LoaderHelper.kasSource;
import static edu.mayo.kmdp.LoaderHelper.kbconstrSource;
import static edu.mayo.kmdp.LoaderHelper.langSource;
import static edu.mayo.kmdp.LoaderHelper.loadSchemas;
import static edu.mayo.kmdp.LoaderHelper.repoSource;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Disabled("Need redo for OAS3, or remove")
public class SwaggerToDocXTranslatorTest {

  @TempDir
  public Path tmp;

  @Test
  public void testArtifactAPI() {
    String title = "Knowledge Artifact Repository";
    toDocumentation(title, karSource, idSource, repoSource);
  }

  @Test
  public void testAssetAPI() {
    String title = "Knowledge Asset Repository";
    List<String> sources = loadSchemas();
    sources.add(0, kasSource);
    toDocumentation(title, sources.toArray(String[]::new));
  }

  @Test
  public void testLangAPI() {
    String title = "Knowledge Asset Transrepresentation";
    List<String> sources = loadSchemas();
    sources.add(0, langSource);
    toDocumentation(title, sources.toArray(String[]::new));
  }

  @Test
  public void testKBaseAPI() {
    String title = "Knowledge Base Construction";
    List<String> sources = loadSchemas();
    sources.add(0, kbconstrSource);
    toDocumentation(title, sources.toArray(String[]::new));
  }

  @Test
  public void testInferAPI() {
    String title = "Knowledge Base Inference";
    List<String> sources = loadSchemas();
    sources.add(0, inferSource);
    toDocumentation(title, sources.toArray(String[]::new));
  }



  private void toDocumentation(String title, String... sourceFiles) {
    List<InputStream> sources = Stream.of(sourceFiles)
        .map(SwaggerToDocXTranslator.class::getResourceAsStream)
        .collect(Collectors.toList());

    byte[] target = new SwaggerToDocXTranslator(title)
        .translate(sources);

    assertTrue(target.length > 0);

    preview(title, target);
  }

  private void preview(String title, byte[] data) {
    Path systemTmp = Paths.get(System.getProperty("java.io.tmpdir"));

    Path root = tmp;
//    Path root = systemTmp;

    File tmpFile = new File(root.toString(), title + ".docx");

    try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
      fos.write(data);
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (Desktop.isDesktopSupported() && root == systemTmp) {
      try {
        Desktop.getDesktop().open(tmpFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
