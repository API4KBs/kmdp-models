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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.terms.example.SCH1;
import edu.mayo.kmdp.terms.example.SomeBean;
import edu.mayo.kmdp.util.JaxbUtil;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ObjectFactory;

public class AdapterTest {

  @Test
  public void testAdapter() {
    SomeBean bean = new SomeBean();
    bean.setSchone(SCH1.Sub_Sub_Concept);
    //System.out.println("ORIGINAL : " + bean);

    ByteArrayOutputStream baos = JaxbUtil.marshall(Collections.singleton(ObjectFactory.class),
        bean,
        JaxbUtil.defaultProperties())
        .orElse(new ByteArrayOutputStream());
    String xml = baos.toString();

    //System.out.println("\nXML-IZED : \n" + xml);

    assertTrue(xml.contains("ref=\"http://test/generator#sub_sub_concept\""));

    SomeBean b2 = JaxbUtil
        .unmarshall(SomeBean.class, SomeBean.class, xml)
        .orElse(null);
    assertNotNull(b2);
    assertEquals(bean.getSchone(), b2.getSchone());
  }
}
