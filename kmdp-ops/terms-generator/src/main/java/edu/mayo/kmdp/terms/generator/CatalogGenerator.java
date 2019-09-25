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

import static edu.mayo.kmdp.util.NameUtils.removeFragment;
import static edu.mayo.kmdp.util.NameUtils.removeTrailingPart;

import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CatalogGenerator extends BaseEnumGenerator {

  public void generate(String namespace, Collection<CatalogEntry> entries, File outputDir) {
    generate(namespace, entries, outputDir, null);
  }

  public void generate(String namespace, Collection<CatalogEntry> entries, File outputDir,
      String catalogName) {
    Map<String, Object> context = new HashMap<>();

    // ensure no duplicates
    entries = new HashSet<>(entries);

    context.put("targetNamespace", namespace);
    context.put("entries", entries);

    this.generateCatalog(context,
        outputDir,
        Util.isEmpty(catalogName) ? "terms-catalog.xml" : catalogName);
  }

  private void generateCatalog(Map<String, Object> context, File outputDir, String catalogName) {
    String mainText = fromTemplate("catalog", context);

    File catalogFile = new File(outputDir, catalogName);
    if (catalogFile.exists()) {
      mainText = mergeCatalogs(
          FileUtil.read(catalogFile)
              .orElse("<catalog/>"),
          mainText);
    }
    this.writeToFile(mainText,catalogFile);
  }

  private String mergeCatalogs(String existingCatalog, String mainText) {
    Document existing = XMLUtil.loadXMLDocument(existingCatalog.getBytes())
        .orElseThrow(IllegalStateException::new);
    Document newCatalog = XMLUtil.loadXMLDocument(mainText.getBytes())
        .orElseThrow(IllegalStateException::new);

    XMLUtil.asElementStream(newCatalog.getDocumentElement().getChildNodes())
        .forEach( n -> migrate(n,existing));
    return new String(XMLUtil.toByteArray(existing));
  }

  private void migrate(Element n, Document existing) {
    existing.getDocumentElement().appendChild(existing.importNode(n,true));
  }


  public static class CatalogEntry {

    private String id;
    private String uri;

    public CatalogEntry(ConceptScheme scheme) {


      this.id = removeFragment(scheme.getVersionId()).toString();
      this.uri = NameUtils.namespaceURIStringToPackage(removeTrailingPart(scheme.getVersionId().toString()))
          .replace(".", "/")
          + "/"
          + NameUtils.getTermCodeSystemName(scheme.getPublicName())
          + ".xsd";
    }

    public CatalogEntry(String id, String uri) {
      this.id = id;
      this.uri = uri;
    }

    public String getId() {
      return id;
    }

    public String getUri() {
      return uri;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof CatalogEntry)) {
        return false;
      }

      CatalogEntry that = (CatalogEntry) o;

      return uri != null ? uri.equals(that.uri) : that.uri == null;
    }

    @Override
    public int hashCode() {
      return uri != null ? uri.hashCode() : 0;
    }
  }
}
