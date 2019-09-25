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
package edu.mayo.kmdp.id.adapter;

import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.XMLUtil;
import org.omg.spec.api4kp._1_0.datatypes.Map;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;
import java.util.Optional;

public class MapAdapter extends XmlAdapter<Map, Map> {


  @Override
  public Map marshal(Map map) {
    map.getAny().clear();
    map.forEach((k, v) -> map.getAny().add(
        new JAXBElement(getKey(k), Object.class, v)));
    return map;
  }


  @Override
  public Map unmarshal(Map adaptedMap) {
    adaptedMap.getAny().stream()
        .filter(Element.class::isInstance)
        .map(Element.class::cast)
        .forEach(el -> adaptedMap.put(getKey(el), getVal(el)));
    adaptedMap.getAny().clear();
    return adaptedMap;
  }

  private Object getVal(Element el) {
    return resolveType(el).flatMap(resolvedType -> JaxbUtil.unmarshall(resolvedType,el))
        .orElse(null);
  }

  private Optional<Class<?>> resolveType(Element el) {
    return XMLUtil.resolveXsiTypeClassName(el).flatMap(n -> {
      try {
        return Optional.ofNullable(Class.forName(n));
      } catch (ClassNotFoundException e) {
        return Optional.of(Object.class);
      }
    });
  }

  private String getKey(Element el) {
    return URIUtil.toURIString(el.getNamespaceURI(), el.getLocalName());
  }


  private QName getKey(Object k) {
    if (k instanceof QName) {
      return (QName) k;
    }
    return URIUtil.asUri(k.toString())
        .flatMap(URIUtil::toQName)
        .orElse(new QName(k.toString()));

  }

}
