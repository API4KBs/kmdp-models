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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.generator.CatalogGenerator.CatalogEntry;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.junit.jupiter.api.Test;
import org.protege.xmlcatalog.XMLCatalog;
import org.protege.xmlcatalog.entry.PublicEntry;
import org.protege.xmlcatalog.entry.SystemEntry;
import org.protege.xmlcatalog.entry.UriEntry;
import org.protege.xmlcatalog.parser.Handler;
import org.xml.sax.InputSource;

public class CatalogGeneratorTest {

  @Test
  public void testCatalog() {
    String namespace = "http://foo.bar";
    URI uri = URI.create("http//foo.bar/baz");
    String id = "baz";

    List<CatalogEntry> entries = new ArrayList<>();
    entries.add(new CatalogEntry(id,uri.toString()));

    Map<String,Object> context = new HashMap<>();
    context.put("targetNamespace",namespace);
    context.put("entries",entries);

    String catalog = new CatalogGenerator().fromTemplate("catalog",context);
    System.out.println(catalog);

    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      Handler handler = new Handler(URI.create(namespace));
      InputSource is = new InputSource(new ByteArrayInputStream(catalog.getBytes()));
      SAXParser parser = factory.newSAXParser();
      parser.parse(is, handler);

      XMLCatalog xmlCatalog = handler.getCatalog();
      assertEquals(3, xmlCatalog.getEntries().size());

      assertTrue(xmlCatalog.getEntries().stream()
          .filter(UriEntry.class::isInstance)
          .map(UriEntry.class::cast)
          .anyMatch((ue) -> ue.getUri().equals(uri) && ue.getName().equals(id)));

      assertTrue(xmlCatalog.getEntries().stream()
          .filter(SystemEntry.class::isInstance)
          .map(SystemEntry.class::cast)
          .anyMatch((ue) -> ue.getUri().equals(uri) && ue.getSystemId().equals(id)));

      assertTrue(xmlCatalog.getEntries().stream()
          .filter(PublicEntry.class::isInstance)
          .map(PublicEntry.class::cast)
          .anyMatch((ue) -> ue.getUri().equals(uri) && ue.getPublicId().equals(id)));

    } catch (Exception e) {
      fail(e.getMessage());
    }

  }

}
