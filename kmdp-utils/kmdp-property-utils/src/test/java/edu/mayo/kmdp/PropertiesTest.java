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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertiesTest {

  @Test
  public void testConfig() {
    FooConfig cfg = new FooConfig()
        .with(BarOpts.A, "aaa")
        .with(BarOpts.B, Boolean.TRUE);

    String a = cfg.get(BarOpts.A).orElse("NOT FOUND");
    boolean b = cfg.getTyped(BarOpts.B, Boolean.class);
    int c = cfg.getTyped(BarOpts.C, Integer.class);

    assertEquals("aaa", a);
    assertEquals(true, b);
    assertEquals(42, c);

  }
}
