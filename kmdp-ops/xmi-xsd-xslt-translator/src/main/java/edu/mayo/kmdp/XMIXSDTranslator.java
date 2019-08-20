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

import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.kmdp.xslt.XSLTConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class XMIXSDTranslator {

  private static final Logger logger = LogManager.getLogger(XMIXSDTranslator.class);

  private String defaultXSLT = "/edu/mayo/kmdp/xmi-to-xsd.xsl";

  public Map<String, Document> doTranslate(InputStream source, XSLTConfig properties) {
    return XMLUtil.applyXSLT(source,
        XMIXSDTranslator.class.getResource(defaultXSLT),
        properties);
  }

  Map<String, Document> doTranslate(URL source, String xsltPath, XSLTConfig properties) {
    try {
      return XMLUtil.applyXSLT(source.openStream(),
          source.toString(),
          XMIXSDTranslator.class.getResource(xsltPath),
          properties);
    } catch (IOException e) {
      logger.error(e.getMessage(),e);
      return Collections.emptyMap();
    }
  }

  Map<String, Document> doTranslate(URL source, XSLTConfig properties) {
    return doTranslate(source, defaultXSLT, properties);
  }

}
