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

import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.Util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CatalogGenerator extends BaseEnumGenerator {

  public void generate(String namespace, Collection<CatalogEntry> entries, File outputDir) {
    generate(namespace, entries, outputDir, null);
  }

  public void generate(String namespace, Collection<CatalogEntry> entries, File outputDir,
      String catalogName) {
    Map<String, Object> context = new HashMap<>();

    context.put("targetNamespace", namespace);
    //context.put( "base", NameUtils.namespaceURIToPackage( namespace ).replaceAll( "\\.", "/" ) );
    context.put("entries", entries);

    this.generateCatalog(context,
        outputDir,
        Util.isEmpty(catalogName) ? "terms-catalog.xml" : catalogName);
  }

  private void generateCatalog(Map<String, Object> context, File outputDir, String catalogName) {
    String mainText = fromTemplate("catalog", context);

    //System.out.println( mainText );
    this.writeToFile(mainText,
        new File(outputDir, catalogName));
  }


  public static class CatalogEntry {

    private String id;
    private String uri;

    public CatalogEntry(ConceptScheme scheme) {
      this.id = NameUtils.removeFragment(scheme.getVersionId()).toString();
      this.uri = NameUtils.nameSpaceURIToPackage(scheme.getVersionId())
          .replaceAll("\\.", "/")
          + "/"
          + NameUtils.getTermCodeSystemName(scheme.getLabel())
          .replace("_Scheme", "")
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
  }
}
