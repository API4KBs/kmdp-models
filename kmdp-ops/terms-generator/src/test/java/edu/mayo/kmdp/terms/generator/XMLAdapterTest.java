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
import edu.mayo.kmdp.terms.example.sch1.SCH1Series;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.properties.jaxb.JaxbConfig.JaxbOptions;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.id.ObjectFactory;

class XMLAdapterTest {

  @Test
  void testXMLSerializationWithSpecificVersion() {
    SomeBean bean = new SomeBean();
    bean.setSchone(SCH1Series.Sub_Sub_Concept.getLatest());

    String xml = marshall(bean);
    assertTrue(xml.contains("referentId=\"http://test/generator#sub_sub_concept\""));
    assertTrue(xml.contains("versionTag=\"v01\""));
  }

  @Test
  void testXMLSerializationWithLatest() {
    SomeBean bean = new SomeBean();
    bean.setSchone(SCH1Series.Sub_Sub_Concept.getLatest());

    String xml = marshall(bean);
    assertTrue(xml.contains("referentId=\"http://test/generator#sub_sub_concept\""));
    assertTrue(xml.contains("versionTag=\"v01\""));
  }

  @Test
  void testXMLSerializationWithVersions() {
    SomeBean bean = new SomeBean();
    bean.setSchone(SCH1Series.Specific_Concept.getVersion(0).orElse(null));
    String xml1 = marshall(bean);
    assertTrue(xml1.contains("versionTag=\"v01\""));

    bean.setSchone(SCH1Series.Specific_Concept.getVersion(1).orElse(null));
    String xml2 = marshall(bean);
    assertTrue(xml2.contains("versionTag=\"v00_Ancient\""));
  }

  @Test
  void testXMLSerializationWithSeries() {
    SomeBean bean = new SomeBean();

    bean.setSchone(SCH1Series.Specific_Concept);
    String xml0 = marshall(bean);
    assertTrue(xml0.contains("versionTag=\"v01\""));

    bean.setSchone(SCH1Series.Specific_Concept.getLatest());
    String xml1 = marshall(bean);
    assertTrue(xml1.contains("versionTag=\"v01\""));

    bean.setSchone(SCH1Series.Deprecated_Concept.getLatest());
    String xml2 = marshall(bean);
    assertTrue(xml2.contains("versionTag=\"v00_Ancient\""));

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

  private SomeBean rehydrate(String xml) {
    return JaxbUtil
        .unmarshall(SomeBean.class, SomeBean.class, xml)
        .orElse(null);
  }

  private String marshall(SomeBean bean) {
    ByteArrayOutputStream baos = JaxbUtil.marshall(
        Collections.singleton(ObjectFactory.class),
        bean,
        JaxbUtil.defaultProperties()
            .with(JaxbOptions.FORMATTED_OUTPUT, true))
        .orElse(new ByteArrayOutputStream());
    return baos.toString();
  }
}
