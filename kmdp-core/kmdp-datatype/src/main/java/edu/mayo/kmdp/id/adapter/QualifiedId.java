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

import edu.mayo.kmdp.id.IDFormats;
import edu.mayo.kmdp.id.Identifier;
import edu.mayo.kmdp.id.ScopedIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.SimpleIdentifier;

import javax.xml.namespace.QName;
import java.net.URI;

public interface QualifiedId extends ScopedIdentifier {

  QName getQName();

  @Override
  default URI getFormat() {
    return IDFormats.QNAME.asURI();
  }

  @Override
  default Identifier getNamespace() {
    return new SimpleIdentifier()
        .withTag(getQName().getNamespaceURI())
        .withFormat(IDFormats.URI.asURI());
  }

  @Override
  default String getTag() {
    return getQName().getLocalPart();
  }

}
