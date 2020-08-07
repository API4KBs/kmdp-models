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
package org.omg.spec.api4kp._1_0.id;

public enum IDFormatsURIs {

  URI("https://www.ietf.org/rfc/rfc3986.txt"),
  IRI("https://www.ietf.org/rfc/rfc3987.txt"),
  QNAME("http://www.w3.org/2001/XMLSchema#QName"),
  UUID("https://www.ietf.org/rfc/rfc4122.txt"),
  OID("https://www.ietf.org/rfc/rfc3001.txt");

  IDFormatsURIs(String uri) {
    this.uri = java.net.URI.create(uri);
  }

  java.net.URI uri;

  public java.net.URI asURI() {
    return uri;
  }
}
