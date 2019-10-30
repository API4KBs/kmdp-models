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
package edu.mayo.kmdp.terms.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.example.SomeBean;
import edu.mayo.kmdp.terms.example.sch1.SCH1;
import edu.mayo.kmdp.terms.example.sch1.SCH1Series;
import edu.mayo.kmdp.util.JSonUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JsonAdapterTest {

  @Test
  void testJSONSerializationWithSpecificVersion() {
    SomeBean bean = new SomeBean();
    bean.setSchone(SCH1Series.Sub_Sub_Concept.getLatest());

    String json = marshall(bean);
    assertTrue(json.contains("\"ref\" : \"http://test/generator#sub_sub_concept\""));
    assertTrue(json.contains("\"version\" : \"v01\""));
  }

  @Test
  void testJSONSerializationWithLatest() {
    SomeBean bean = new SomeBean();
    bean.setSchone(SCH1Series.Sub_Sub_Concept.getLatest());

    String json = marshall(bean);
    assertTrue(json.contains("\"ref\" : \"http://test/generator#sub_sub_concept\""));
    assertTrue(json.contains("\"version\" : \"v01\""));
  }

  @Test
  void testJSONSerializationWithVersions() {
    SomeBean bean = new SomeBean();
    bean.setSchone(SCH1Series.Specific_Concept.getVersion(0).orElse(null));
    String json1 = marshall(bean);
    assertTrue(json1.contains("\"version\" : \"v01\""));

    bean.setSchone(SCH1Series.Specific_Concept.getVersion(1).orElse(null));
    String json2 = marshall(bean);
    assertTrue(json2.contains("\"version\" : \"v00_Ancient\""));
  }

  @Test
  void testJSONSerializationWithSeries() {
    SomeBean bean = new SomeBean();

    bean.setSchone(SCH1Series.Specific_Concept);
    String json1 = marshall(bean);
    assertFalse(json1.contains("\"version\" : \"v01\""));

    bean.setSchone(SCH1Series.Deprecated_Concept);
    String json2 = marshall(bean);
    assertFalse(json2.contains("\"version\" : \"v00_Ancient\""));

    bean.setSchone(SCH1Series.Deprecated_Concept.getLatest());
    String json3 = marshall(bean);
    assertTrue(json3.contains("\"version\" : \"v00_Ancient\""));

  }

  @Test
  void testJSONSerializationWithSeriesRoundtrip() {

    SomeBean bean2 = new SomeBean();
    bean2.setSchone(SCH1Series.Specific_Concept);
    bean2 = roundtrip(bean2);
    assertTrue(bean2.getSchone() instanceof SCH1Series);
    bean2 = roundtrip(bean2);
    assertTrue(bean2.getSchone() instanceof SCH1Series);

    SomeBean bean1 = new SomeBean();
    bean1.setSchone(SCH1Series.Specific_Concept.getLatest());
    bean1 = roundtrip(bean1);
    assertTrue(bean1.getSchone() instanceof SCH1);
  }

  @Test
  void testAdapter() {
    SomeBean bean = new SomeBean();
    bean.setSchone(SCH1Series.Sub_Sub_Concept.getLatest());

    SomeBean b2 = rehydrate(marshall(bean));

    assertNotNull(b2);
    assertEquals(bean.getSchone(), b2.getSchone());
  }

  @Test
  void testAdapterWithVersions() {
    SomeBean bean = new SomeBean();
    bean.setSchone(
        SCH1Series.Specific_Concept.getVersion(1).orElseThrow(IllegalStateException::new));

    SomeBean b2 = rehydrate(marshall(bean));

    assertNotNull(b2);
    assertEquals(bean.getSchone(), b2.getSchone());
  }

  private SomeBean rehydrate(String json) {
    SomeBean bean = JSonUtil.parseJson(json,SomeBean.class).orElse(null);
    assertNotNull(bean);
    return bean;
  }

  private String marshall(SomeBean bean) {
    String s = JSonUtil.writeJsonAsString(bean).orElse(null);
    System.out.println(s);
    assertNotNull(s);
    return s;
  }

  private SomeBean roundtrip(SomeBean b) {
    return rehydrate(marshall(b));
  }
}
