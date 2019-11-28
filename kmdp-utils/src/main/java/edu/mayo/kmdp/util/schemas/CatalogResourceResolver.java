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
import java.io.Reader;
import java.net.URL;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class CatalogResourceResolver implements LSResourceResolver {

  private static final Logger logger = LoggerFactory.getLogger(CatalogResourceResolver.class);

  private CatalogResolver resolver;

  public CatalogResourceResolver(CatalogResolver resolver) {
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
        path = resolver.getCatalog().resolveURI(namespaceURI);
        if (path == null) {
          return null;
        }
        InputStream is = new URL(path).openStream();

        return new InnerDOMInputImpl(publicId,
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

  private class InnerDOMInputImpl implements LSInput {

    private String publicId;
    private String systemId;
    private InputStream byteStream;
    private String baseURI;
    private String encoding;
    private Reader characterStream;
    private String stringData;
    private boolean certifiedText = false;

    InnerDOMInputImpl(String publicId, String systemId,
        String baseSystemId, InputStream byteStream,
        String encoding) {

      this.publicId = publicId;
      this.systemId = systemId;
      this.baseURI = baseSystemId;
      this.byteStream = byteStream;
      this.encoding = encoding;
    }

    @Override
    public String getPublicId() {
      return publicId;
    }

    @Override
    public String getSystemId() {
      return systemId;
    }

    @Override
    public void setSystemId(String systemId) {
      this.systemId = systemId;
    }

    @Override
    public void setPublicId(String publicId) {
      this.publicId = publicId;
    }

    @Override
    public void setByteStream(InputStream byteStream) {
      this.byteStream = byteStream;
    }

    @Override
    public void setBaseURI(String baseSystemId) {
      this.baseURI = baseSystemId;
    }

    @Override
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    @Override
    public InputStream getByteStream() {
      return byteStream;
    }

    @Override
    public String getBaseURI() {
      return baseURI;
    }

    @Override
    public String getEncoding() {
      return encoding;
    }

    @Override
    public Reader getCharacterStream() {
      return characterStream;
    }

    @Override
    public void setCharacterStream(Reader charStream) {
      this.characterStream = charStream;
    }

    @Override
    public String getStringData() {
      return stringData;
    }

    @Override
    public void setStringData(String stringData) {
      this.stringData = stringData;
    }

    @Override
    public boolean getCertifiedText() {
      return certifiedText;
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
      this.certifiedText = certifiedText;
    }
  }
}

