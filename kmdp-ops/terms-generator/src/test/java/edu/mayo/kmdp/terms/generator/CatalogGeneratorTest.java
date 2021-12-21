/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.terms.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.generator.CatalogGenerator.CatalogEntry;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class CatalogGeneratorTest {

  @Test
  void testCatalog() {
    String namespace = "http://foo.bar";
    String uri = URI.create("http//foo.bar/baz").toString();
    String id = "baz";

    List<CatalogEntry> entries = new ArrayList<>();
    entries.add(new CatalogEntry(id, uri, namespace, namespace));

    Map<String, Object> context = new HashMap<>();
    context.put("targetNamespace", namespace);
    context.put("entries", entries);

    String catalog = new CatalogGenerator().fromTemplate("catalog", context);

    try {
      Document dox = XMLUtil.loadXMLDocument(new ByteArrayInputStream(catalog.getBytes()))
          .orElseGet(Assertions::fail);
      Element root = dox.getDocumentElement();

      NodeList publicEntries = root.getElementsByTagName("public");
      assertEquals(2, publicEntries.getLength());
      XMLUtil.asElementStream(publicEntries)
          .filter(el -> id.equals(el.getAttribute("publicId"))
              && uri.equals(el.getAttribute("uri")))
          .findAny()
          .orElseGet(Assertions::fail);

      NodeList systemEntries = root.getElementsByTagName("system");
      assertEquals(2, systemEntries.getLength());
      XMLUtil.asElementStream(systemEntries)
          .filter(el -> id.equals(el.getAttribute("systemId"))
              && uri.equals(el.getAttribute("uri")))
          .findAny()
          .orElseGet(Assertions::fail);

      NodeList uriEntries = root.getElementsByTagName("uri");
      assertEquals(2, uriEntries.getLength());
      XMLUtil.asElementStream(uriEntries)
          .filter(el -> id.equals(el.getAttribute("name"))
              && uri.equals(el.getAttribute("uri")))
          .findAny()
          .orElseGet(Assertions::fail);

    } catch (Exception e) {
      fail(e.getMessage());
    }

  }

}
