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
package edu.mayo.kmdp;

import static edu.mayo.kmdp.LoaderHelper.idSource;
import static edu.mayo.kmdp.LoaderHelper.inferSource;
import static edu.mayo.kmdp.LoaderHelper.karSource;
import static edu.mayo.kmdp.LoaderHelper.kasSource;
import static edu.mayo.kmdp.LoaderHelper.kbconstrSource;
import static edu.mayo.kmdp.LoaderHelper.langSource;
import static edu.mayo.kmdp.LoaderHelper.loadSchemas;
import static edu.mayo.kmdp.LoaderHelper.repoSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.CodeGenTestUtil;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.StreamUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Disabled("Need redo for OAS3, or remove")
public class SwaggerToIDLTranslatorTest {

  @TempDir
  public Path tmp;


  @Test
  public void testArtifactAPI() {
    List<String> sources = Stream.of(karSource, idSource, repoSource).collect(Collectors.toList());
    toIDL("Knowledge Artifact Repository", sources.toArray(String[]::new));
  }

  @Test
  public void testAssetAPI() {
    String title = "Knowledge Asset Repository";
    List<String> sources = loadSchemas();
    sources.add(0, kasSource);
    toIDL(title, sources.toArray(String[]::new));
  }


  @Test
  public void testLangAPI() {
    String title = "Knowledge Asset Transrepresentation";
    List<String> sources = loadSchemas();
    sources.add(0, langSource);
    toIDL(title, sources.toArray(String[]::new));
  }

  @Test
  public void testKBaseAPI() {
    String title = "Knowledge Base Construction";
    List<String> sources = loadSchemas();
    sources.add(0, kbconstrSource);
    toIDL(title, sources.toArray(String[]::new));
  }

  @Test
  public void testInferAPI() {
    String title = "Knowledge Base Inference";
    List<String> sources = loadSchemas();
    sources.add(0, inferSource);
    toIDL(title, sources.toArray(String[]::new));
  }



  private void toIDL(String title, String... sourceFiles) {
    Path systemTmp = Paths.get(System.getProperty("java.io.tmpdir"));

//    Path root = tmp;
    Path root = systemTmp;

    root = Paths.get(root.toString(), title);
    assertTrue(deleteDirectory(root));

    List<byte[]> sources = Stream.of(sourceFiles)
        .map(SwaggerToIDLTranslator.class::getResourceAsStream)
        .map(FileUtil::readBytes)
        .flatMap(StreamUtil::trimStream)
        .collect(Collectors.toList());

    File gen = new File(root.toFile(),"gen");
    assertTrue(gen.mkdirs());
    File out = new File(root.toFile(),"out");
    assertTrue(out.mkdirs());

    List<String> target = new SwaggerToIDLTranslator()
        .translate(sources);
    assertFalse(target.isEmpty());

    // target.forEach(s -> System.out.println("\n\n" + s));

    String errs = MockIDLCompiler.tryCompileSource(title, gen, target);
    assertEquals("", errs, errs);

    // CodeGenTestBase.showDirContent(root.toFile(),true);

    CodeGenTestUtil.ensureSuccessCompile(gen,gen,out);

    CodeGenTestUtil.showDirContent(root.toFile(),true);
  }


  static boolean deleteDirectory(Path path) {
    try {
      if (!Files.exists(path)) {
        return true;
      }
      return Files.walk(path)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .allMatch(File::delete);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
}
