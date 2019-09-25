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
package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class JsonUtilTest {

  @Test
  public void testSerializeObject() {
    Foo f = new Foo("a");
    String s = JSonUtil.writeJson(f)
        .flatMap(Util::asString)
        .orElse("");
    assertTrue(s.contains("\"bar\" : \"a\""));
  }

  @Test
  public void testSerializeList() {
    List<Foo> foos = Arrays.asList( new Foo("a"), new Foo("b") );

    JsonNode recs = JSonUtil.writeJson(foos)
        .map(ByteArrayOutputStream::toByteArray)
        .map(ByteArrayInputStream::new)
        .flatMap(JSonUtil::readJson)
        .orElse(JsonNodeFactory.instance.nullNode());

    assertTrue(recs.isArray());
    assertTrue(recs instanceof ArrayNode);
    ArrayNode an = (ArrayNode) recs;
    assertEquals(2, an.size());
  }

  @Test
  public void testParseArray() {
    List<Foo> foos = Arrays.asList( new Foo("a"), new Foo("b") );

    List<Foo> recs = JSonUtil.writeJson(foos)
        .map(ByteArrayOutputStream::toByteArray)
        .map(ByteArrayInputStream::new)
        .flatMap(in -> JSonUtil.parseJsonList(in, null, Foo.class))
        .orElse(Collections.emptyList());

    assertEquals(foos,recs);
    assertEquals(2, recs.size());
  }

  public static class Foo {
    private String bar;

    public Foo() {}

    public Foo(String b) {
      this.bar = b;
    }

    public String getBar() {
      return this.bar;
    }

    public void setBar(String bar) {
      this.bar = bar;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Foo)) {
        return false;
      }

      Foo foo = (Foo) o;

      return bar.equals(foo.bar);
    }

    @Override
    public int hashCode() {
      return bar.hashCode();
    }
  }
}
