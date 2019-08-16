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
package edu.mayo.kmdp.util.schemas;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.util.XMLCatalogResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class CatalogResourceResolver implements LSResourceResolver {

  private static final Logger logger = LogManager.getLogger(CatalogResourceResolver.class);

  private XMLCatalogResolver resolver;

  public CatalogResourceResolver(XMLCatalogResolver resolver) {
    this.resolver = resolver;
  }


  @Override
  public LSInput resolveResource(String type,
      String namespaceURI,
      String publicId,
      String systemId,
      String baseURI) {
    if (namespaceURI != null) {
      String path;
      try {
        if (baseURI != null && systemId != null) {
          return null;
        }
        path = resolver.resolveURI(namespaceURI);
        if (path == null) {
          return null;
        }
        InputStream is = new URL(path).openStream();

        return new DOMInputImpl(publicId,
            systemId,
            baseURI,
            is,
            "UTF-8");
      } catch (IOException e) {
        logger.error(e.getMessage(),e);
      }
    }
    return null;
  }

}

