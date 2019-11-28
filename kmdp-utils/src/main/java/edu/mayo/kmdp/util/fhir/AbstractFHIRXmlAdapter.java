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
package edu.mayo.kmdp.util.fhir;

import edu.mayo.kmdp.util.XMLUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class AbstractFHIRXmlAdapter<R> extends XmlAdapter<Object, R> {

  @Override
  @SuppressWarnings("unchecked")
  public R unmarshal(Object v) {
    if (!(v instanceof Element)) {
      return null;
    }
    byte[] b = XMLUtil.asElementStream(((Element) v).getChildNodes())
        .filter(n -> n.getNodeType() == Node.ELEMENT_NODE)
        .findAny()
        .map(XMLUtil::toByteArray)
        .orElse(null);
    if (b == null) {
      return null;
    }
    IBaseResource br = parseResource(new InputStreamReader(new ByteArrayInputStream(b)));
    return (R) br;
  }

  protected abstract IBaseResource parseResource(InputStreamReader inputStreamReader);

  @Override
  public Object marshal(R v) {
    if (v == null) {
      return null;
    }
    byte[] bytes = encodeResource(v).getBytes();
    Document dox = XMLUtil.loadXMLDocument(bytes)
        .orElse(XMLUtil.emptyDocument());
    if (dox == null) {
      return null;
    }
    Element wrapper = dox.createElement("temp");
    wrapper.appendChild(dox.getDocumentElement());
    return wrapper;
  }

  protected abstract String encodeResource(R v);

}
