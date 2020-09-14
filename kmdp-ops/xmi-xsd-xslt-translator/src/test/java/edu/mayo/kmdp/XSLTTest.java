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

import edu.mayo.kmdp.util.XPathUtil;
import edu.mayo.kmdp.util.XSLTSplitter;
import edu.mayo.kmdp.xslt.XSLTConfig;
import edu.mayo.kmdp.xslt.XSLTConfig.XSLTOptions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XSLTTest {

  @Test
  public void testXSLT() {
    String source = "/test.xmi";

    Map<String, Document> xsd = new XMIXSDTranslator()
        .doTranslate(XSLTTest.class.getResource(source),
            new XSLTConfig()
                .with(XSLTOptions.OUTPUT_RESOLVER, XSLTSplitter.class.getName()));

    assertFalse(xsd.isEmpty());

    assertEquals(1, xsd.size());
    //assertTrue( xsd.containsKey( "Foo/root/root.xsd" ) );

    //xsd.forEach((k, v) -> XMLUtil.streamXMLDocument(v, System.out));
  }

  @Test
  public void testMultiXSLT() {
    XPathUtil xp = new XPathUtil();
    String source = "/complex/main.uml.xmi.xml";

    Map<String, Document> xsd = new XMIXSDTranslator()
        .doTranslate(XSLTTest.class.getResource(source),
            new XSLTConfig()
                .with(XSLTOptions.OUTPUT_RESOLVER, XSLTSplitter.class.getName()));

    assertFalse(xsd.isEmpty());
//    xsd.forEach((k, v) -> {
//      System.out.println(k);
//      XMLUtil.streamXMLDocument(v, System.out);
//    });

    assertEquals(2, xsd.size());

    assertTrue(xsd.containsKey("Root/core/sub/sub.xsd"));
    assertTrue(xsd.containsKey("Root/core/core.xsd"));

    Document dox1 = xsd.get("Root/core/sub/sub.xsd");
    assertNotNull(xp.xNode(dox1, "//xsd:complexType[@name='SomeExtendedType']"));
    assertNotNull(xp.xNode(dox1,
        "//xsd:complexType[@name='SomeExtendedType']//xsd:extension[@base='tns:Surrogate']"));
    assertEquals(1, xp.xList(dox1, "//xsd:import").getLength());

    Document dox = xsd.get("Root/core/core.xsd");
    assertNotNull(xp.xNode(dox, "//xsd:complexType[@name='RootTop']"));
    assertNotNull(xp.xNode(dox, "//xsd:element[@type='tns:URIIdentifier']"));
    assertEquals(1, xp.xList(dox, "//xsd:import").getLength());

  }


  @Test
  public void testSchemaAdapter() {
    XPathUtil xp = new XPathUtil();
    String source = "/complex/withResource.xmi.xml";

    Map<String, Document> xsd = new XMIXSDTranslator()
        .doTranslate(XSLTTest.class.getResource(source),
            new XSLTConfig()
                .with(XSLTOptions.OUTPUT_RESOLVER, XSLTSplitter.class.getName()));

    assertFalse(xsd.isEmpty());
//    xsd.forEach((k, v) -> {
//      System.out.println(k);
//      XMLUtil.streamXMLDocument(v, System.out);
//    });

    assertEquals(1, xsd.size());

    Document dox1 = xsd.get("Test/ids/ids.xsd");
    assertNotNull(xp.xNode(dox1, "//xsd:complexType[@name='Pointer']"));

    Map<String, Document> xsd2 = new XMIXSDTranslator()
        .doTranslate(XSLTTest.class.getResource(source),
            "/edu/mayo/kmdp/xmi-to-xsd-ws.xsl",
            new XSLTConfig().with(XSLTOptions.OUTPUT_RESOLVER, XSLTSplitter.class.getName()));

    assertFalse(xsd2.isEmpty());
//    xsd2.forEach((k, v) -> {
//      System.out.println(k);
//      XMLUtil.streamXMLDocument(v, System.out);
//    });

    assertEquals(1, xsd2.size());

    Document dox2 = xsd2.get("Test/ids/ids.openapi.xsd");
    assertNull(xp.xNode(dox2, "/xsd:schema/xsd:complexType[@name='Pointer']"));
//		assertNotNull( xNode( dox2, "//xsd:complexType[@name='Pointer']" ) );

  }
}
