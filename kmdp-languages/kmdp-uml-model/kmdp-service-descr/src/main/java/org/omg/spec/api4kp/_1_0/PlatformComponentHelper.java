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
package org.omg.spec.api4kp._1_0;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static edu.mayo.kmdp.util.Util.isEmpty;

import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.services.repository.KnowledgeArtifactRepository;

public class PlatformComponentHelper {

  public static Optional<KnowledgeArtifactRepository> repositoryDescr(String baseNamespace, String identifier,
      String name, URL baseUrl) {
    if (isEmpty(identifier) || isEmpty(baseNamespace) || baseUrl == null) {
      return Optional.empty();
    }
    return Optional.of(new edu.mayo.kmdp.common.model.KnowledgeArtifactRepository()
        .withInstanceId(uri("uri:uuid:" + UUID.randomUUID()))
        .withId(uri(baseNamespace + "/repos/" + identifier))
        .withAlias(uri("uri:uuid:" + identifier))
        .withName(name)
        .withHref(URI.create(baseUrl + "/repos/" + identifier)));
  }

}