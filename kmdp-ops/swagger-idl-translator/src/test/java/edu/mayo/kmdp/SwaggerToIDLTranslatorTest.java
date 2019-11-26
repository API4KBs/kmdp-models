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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.CodeGenTestBase;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SwaggerToIDLTranslatorTest {

  @TempDir
  public Path tmp;

  final static String karSource = "/openapi/v2/org/omg/spec/api4kp/3.0.0/knowledgeArtifactRepository.yaml";
  final static String dataTypeSource = "/yaml/API4KP/api4kp/identifiers/identifiers.yaml";
  final static String repoSource = "/yaml/API4KP/api4kp/services/repository/repository.yaml";


  @Test
  public void testArtifactAPI() {
    List<InputStream> sources = Stream.of(karSource, dataTypeSource, repoSource)
        .map(SwaggerToIDLTranslatorTest.class::getResourceAsStream)
        .collect(Collectors.toList());

    File gen = new File(tmp.toFile(),"gen");
    assertTrue(gen.mkdir());
    File out = new File(tmp.toFile(),"out");
    assertTrue(out.mkdir());

    Optional<String> target = new SwaggerToIDLTranslator()
        .translate(sources);
    assertTrue(target.isPresent());

    System.out.println(target.get());

    String errs = TestIDLCompiler.tryCompileSource(gen, target.orElse(""));
    assertEquals("", errs, errs);
    CodeGenTestBase.ensureSuccessCompile(gen,gen,out);

    CodeGenTestBase.showDirContent(tmp.toFile(),true);
  }

}
