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

import edu.mayo.kmdp.common.model.Pointer;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class IntegrityTest {

  @Test
  public void noServiceFoundTest() {
    assertThrows(ClassNotFoundException.class,
        () -> {
          Class.forName("org.omg.spec.api4kp._1_0.service.KnowledgePlatformComponent");
        }
    );
  }

  @Test
  public void testCommonPackage() {
    Pointer ptr = new Pointer();
    assertTrue(ptr instanceof org.omg.spec.api4kp._1_0.identifiers.Pointer);

    System.out.println(Pointer.class.getPackage().getName());
    Reflections reflections = new Reflections(Pointer.class.getPackage().getName(),
        new SubTypesScanner(false),
        new TypeAnnotationsScanner());

    Set<Class<?>> allClasses = reflections.getTypesAnnotatedWith(XmlRootElement.class);

    assertTrue(allClasses.contains(Pointer.class));
  }

}

