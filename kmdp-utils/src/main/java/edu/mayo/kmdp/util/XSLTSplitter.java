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

import net.sf.saxon.lib.OutputURIResolver;
import org.w3c.dom.Document;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import java.util.HashMap;
import java.util.Map;

public class XSLTSplitter implements OutputURIResolver {

  private Map<String, Document> map = new HashMap<>();
  private DOMResult base;

  public XSLTSplitter(DOMResult base) {
    this.base = base;
  }

  @Override
  public OutputURIResolver newInstance() {
    return this;
  }

  @Override
  public Result resolve(String base, String href) {
    return new DOMResult(null, base);
  }

  @Override
  public void close(Result result) {
    map.put(result.getSystemId(),
        (Document) ((DOMResult) result).getNode());
  }

  public Map<String, Document> getFragments() {
    if (((Document) base.getNode()).getDocumentElement() != null) {
      map.put(base.getSystemId(), (Document) base.getNode());
    }
    return map;
  }
}
