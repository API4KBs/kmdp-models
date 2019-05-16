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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class SwaggerToIDLTranslatorTest {

  @Test
  public void testArtifactAPI() {
    String source = "/openapi/v2/org/omg/spec/api4kp/knowledgeArtifactRepository.yaml";
    InputStream input = SwaggerToIDLTranslatorTest.class.getResourceAsStream(source);

    Optional<String> target = (new SwaggerToIDLTranslator()
        .translate(input));

    assertTrue(target.isPresent());
  }

}
