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
package edu.mayo.kmdp.util;

import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RelativeFileURIResolver implements URIResolver {

  public static final Logger logger = LogManager.getLogger(RelativeFileURIResolver.class);
  
  private String loc;

  public String getLoc() {
    return loc;
  }

  public void setLoc(String loc) {
    this.loc = loc;
  }

  public RelativeFileURIResolver withLoc(String loc) {
    setLoc(loc);
    return this;
  }

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    try {
      URL url = XMLUtil.asFileURL(href);
      if (url == null) {
        url = new URL(loc.substring(0, loc.lastIndexOf('/') + 1) + href);
      }
      return new StreamSource(url.openStream());
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
      return null;
    }
  }
}
