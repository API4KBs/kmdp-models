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

import edu.mayo.kmdp.common.model.KnowledgeAsset;
import edu.mayo.kmdp.common.model.Pointer;
import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.springframework.util.PatternMatchUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class IntegrityTest {

  @Test
  public void testIntegrity() {
    KnowledgeAsset kas = new KnowledgeAsset();

    //System.out.println(kas.getClass().getPackage().getName());
    Reflections reflections = new Reflections(Pointer.class.getPackage().getName(),
        new SubTypesScanner(false),
        new TypeAnnotationsScanner());

    Set<Class<?>> allClasses = reflections.getTypesAnnotatedWith(XmlRootElement.class);

    //System.out.println(allClasses);

    assertEquals(11,
        allClasses.size(),
        "Defensive programming: a new 'Resource' class may have been declared, removed, " +
            "or the code generation process has been corrupted");
  }

  @Test
  public void testCodegeneration() {
    URL url = IntegrityTest.class.getResource("/");
    //System.out.println(url);
    try {
      File f = new File(url.toURI());
      assertTrue(f.exists());
      f = new File(f.getParent() +
          ".generated-sources.xjc.".replaceAll("\\.", Matcher.quoteReplacement(File.separator))
          + KnowledgeAsset.class.getPackage().getName()
          .replaceAll("\\.", Matcher.quoteReplacement(File.separator)));
      assertTrue(f.exists());
      assertTrue(f.isDirectory());
//      Arrays.stream(f.listFiles()).forEach(System.out::println);
      Set<String> fileNames = Arrays.stream(f.listFiles()).map(File::getName)
          .collect(Collectors.toSet());
      assertTrue(fileNames.contains(KnowledgeAsset.class.getSimpleName() + ".java"));
      assertTrue(fileNames.contains(SimpleAnnotation.class.getSimpleName() + ".java"));
    } catch (URISyntaxException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


}
