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
package edu.mayo.kmdp.test.mockdel;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

  private final static QName _Bean_QNAME = new QName("http://foo.com/bar", "bean");
  private final static QName _Trean_QNAME = new QName("http://foo.com/bar", "trean");
  private final static QName _Vid_QNAME = new QName("http://foo.com/bar", "vid");

  public ObjectFactory() {
  }

  public Bean createBean() {
    return new Bean();
  }

  public Trean createTrean() {
    return new Trean();
  }

  public Vid createVid() {
    return new Vid();
  }

  @XmlElementDecl(namespace = "http://foo.com/bar", name = "bean")
  public JAXBElement<Bean> createBean(Bean value) {
    return new JAXBElement<Bean>(_Bean_QNAME, Bean.class, null, value);
  }

  @XmlElementDecl(namespace = "http://foo.com/bar", name = "trean")
  public JAXBElement<Trean> createTrean(Trean value) {
    return new JAXBElement<Trean>(_Trean_QNAME, Trean.class, null, value);
  }

  @XmlElementDecl(namespace = "http://foo.com/bar", name = "vid")
  public JAXBElement<Vid> createVid(Vid value) {
    return new JAXBElement<Vid>(_Vid_QNAME, Vid.class, null, value);
  }


}