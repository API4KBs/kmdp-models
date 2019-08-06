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
package edu.mayo.kmdp.util.fhir3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import edu.mayo.kmdp.util.XMLUtil;
import org.hl7.fhir.dstu3.model.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class FHIR3XmlAdapter extends XmlAdapter<Object, Resource> {

  private static IParser xmlParser = FhirContext.forDstu3().newXmlParser();

  @Override
  public Resource unmarshal(Object v) {
    if (!(v instanceof Element)) {
      return null;
    }
    return XMLUtil.asElementStream(((Element) v).getOwnerDocument().getChildNodes())
        .filter((n) -> n.getNodeType() == Node.ELEMENT_NODE)
        .findAny()
        .map(XMLUtil::toByteArray)
        .map((bytes) -> xmlParser
            .parseResource(Resource.class, new InputStreamReader(new ByteArrayInputStream(bytes))))
        .orElse(null);
  }

  @Override
  public Object marshal(Resource v) {
    if (v == null) {
      return null;
    }
    byte[] bytes = xmlParser.encodeResourceToString(v).getBytes();
    Document dox = XMLUtil.loadXMLDocument(bytes).orElse(XMLUtil.emptyDocument());
    if (dox == null) {
      return null;
    }
    Element wrapper = dox.createElement("temp");
    wrapper.appendChild(dox.getDocumentElement());
    return wrapper;
  }

}
