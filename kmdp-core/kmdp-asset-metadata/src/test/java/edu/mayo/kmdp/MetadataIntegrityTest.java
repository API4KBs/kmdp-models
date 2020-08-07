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

import static edu.mayo.kmdp.util.FileUtil.streamChildFiles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.metadata.annotations.Annotation;
import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.surrogate.resources.KnowledgeAsset;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataIntegrityTest {

  Logger logger = LoggerFactory.getLogger(MetadataIntegrityTest.class);

  @Test
  public void testIntegrity() {
    Reflections reflections = new Reflections(KnowledgeAsset.class.getPackage().getName(),
        new SubTypesScanner(false),
        new TypeAnnotationsScanner());

    Set<Class<?>> allClasses = reflections.getTypesAnnotatedWith(XmlRootElement.class);

    assertEquals(2,
        allClasses.size(),
        "Defensive programming: a new 'Resource' class may have been declared, removed, " +
            "or the code generation process has been corrupted");
  }

  @Test
  public void testCodegeneration() {
    URL url = MetadataIntegrityTest.class.getResource("/");

    try {
      File f = new File(url.toURI());
      assertTrue(f.exists());
      f = new File(f.getParent() +
          ".generated-sources.xjc.".replaceAll("\\.", Matcher.quoteReplacement(File.separator))
          + KnowledgeAsset.class.getPackage().getName()
          .replaceAll("\\.", Matcher.quoteReplacement(File.separator)));
      assertTrue(f.exists());
      assertTrue(f.isDirectory());

      Set<String> fileNames = streamChildFiles(f).map(File::getName)
          .collect(Collectors.toSet());
      assertTrue(fileNames.contains(KnowledgeAsset.class.getSimpleName() + ".java"));
    } catch (URISyntaxException e) {
      logger.error(e.getMessage(),e);
      fail(e.getMessage());
    }
  }

  @Test
  public void testCodegeneration2() {
    URL url = MetadataIntegrityTest.class.getResource("/");

    try {
      File f = new File(url.toURI());
      assertTrue(f.exists());
      f = new File(f.getParent() +
          ".generated-sources.xjc.".replaceAll("\\.", Matcher.quoteReplacement(File.separator))
          + Annotation.class.getPackage().getName()
          .replaceAll("\\.", Matcher.quoteReplacement(File.separator)));
      assertTrue(f.exists());
      assertTrue(f.isDirectory());

      Set<String> fileNames = streamChildFiles(f).map(File::getName)
          .collect(Collectors.toSet());
      assertTrue(fileNames.contains(SimpleAnnotation.class.getSimpleName() + ".java"));
    } catch (URISyntaxException e) {
      logger.error(e.getMessage(),e);
      fail(e.getMessage());
    }
  }


  @Test
  public void testEpisode() {
    // ConceptIdentifier should have not been regenerated (even more so without bindings!)
    ConceptIdentifier t = new ConceptIdentifier();
    assertNotNull(t);
  }


}
