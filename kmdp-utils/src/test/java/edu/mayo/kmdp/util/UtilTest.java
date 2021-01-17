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

import static edu.mayo.kmdp.util.Util.paginate;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class UtilTest {

  @Test
  void testTermConceptNameNormalization() {
    String normalized = NameUtils.getTermConceptName( "123", "Something (else)");
    assertEquals("Something_Else", normalized);
  }

  @Test
  void testTermConceptNameNormalizationOnEmpty() {
    String normalized = NameUtils.getTermConceptName( "", "");
    assertEquals("", normalized);
  }

  @Test
  void testPaginate() {
    List<Integer> list = IntStream.range(0,5).boxed().collect(toList());
    assertEquals(list, paginate(list,null,null, null));
    assertEquals(Arrays.asList(2,3,4), paginate(list,2,null, null));
    assertEquals(Arrays.asList(2,3,4), paginate(list,2,8, null));
    assertEquals(Arrays.asList(2,3,4), paginate(list,2,5, null));
    assertEquals(Arrays.asList(0,1,2), paginate(list,null,3, null));
    assertEquals(Arrays.asList(1,2), paginate(list,1,2, null));
    assertEquals(Arrays.asList(3,4), paginate(list,3,3, null));
  }

  @Test
  void testHashUUIDs() {
    UUID u1 = UUID.nameUUIDFromBytes("a".getBytes());
    UUID u2 = UUID.nameUUIDFromBytes("b".getBytes());

    UUID u3 = Util.hashUUID(u1,u2);
    UUID u4 = Util.hashUUID(u2,u1);
    assertEquals(u3,u4);

    UUID u5 = Util.hashUUID(u1,u1);
    assertNotEquals(u5,u1);
    assertNotEquals(u5,u3);
  }

  @Test
  void testHashStrings() {
    String s1 = "a";
    String s2 = "b";

    String s3 = Util.hashString(s1,s2);
    String s4 = Util.hashString(s2,s1);
    assertEquals(s3,s4);

    String s5 = Util.hashString(s1,s1);
    assertNotEquals(s5,s1);
    assertNotEquals(s5,s3);
  }

  @Test
  void testToMap() {
    List<String> values = Arrays.asList("aaaa", "bbb", "c", "ddddd");
    Map<String,String> map = Util.toMap(values, s -> s.substring(0,1));
    assertEquals(
        new HashSet<>(Arrays.asList("a", "b", "c", "d")),
        map.keySet()
    );
    assertEquals("bbb", map.get("b"));
  }

}
