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
package edu.mayo.kmdp.test;

import edu.mayo.kmdp.test.mockdel.Bean;
import edu.mayo.kmdp.test.mockdel.Trean;
import edu.mayo.kmdp.test.mockdel.Vid;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.Util;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonLDTest {

  static final String jsonBean = "{\n" +
      "  \"@context\" : {\n" +
      "    \"@vocab\" : \"http://www.foo.com/bar\",\n" +
      "    \"foo\" : \"http://www.foo.com/bar\"\n" +
      "  },\n" +
      "  \"@type\" : \"foo:Bean\",\n" +
      "  \"@id\" : \"http://foo/123\",\n" +
      "  \"ident\" : {\n" +
      "    \"@context\" : {\n" +
      "      \"@vocab\" : \"http://www.main.org\",\n" +
      "      \"base\" : \"http://www.main.org/base\",\n" +
      "      \"identifier\" : \"base:identifier\",\n" +
      "      \"version\" : \"base:versionIdentifier\"\n" +
      "    },\n" +
      "    \"@type\" : \"base:VersionID\",\n" +
      "    \"identifier\" : \"http://foo\",\n" +
      "    \"version\" : \"http://foo/123\"\n" +
      "  }\n" +
      "}";

  @Test
  public void testSerializeBean() {
    Bean b = new Bean(new Vid("http://foo", "123"));

    Optional<String> json = JSonUtil.writeJsonLD(b).map(ByteArrayOutputStream::toByteArray)
        .map(String::new);

    assertTrue(json.isPresent());
//    System.out.println(json.get());
    assertEquals(Util.clearLineSeparators(jsonBean), Util.clearLineSeparators(json.get()));
  }

  @Test
  public void testSerializeTrean() {
    Bean b = new Bean(new Vid("http://foo", "123"));

    Trean t = new Trean(new Vid("http://foo", "456"));
    t.setRel1(b);
    t.setRel2(Arrays.asList(b));

    b.setT(t);

    Optional<String> json = JSonUtil.writeJson(t)
        .map(ByteArrayOutputStream::toByteArray)
        .map(String::new);

//    System.out.println(json.get());
  }

}
