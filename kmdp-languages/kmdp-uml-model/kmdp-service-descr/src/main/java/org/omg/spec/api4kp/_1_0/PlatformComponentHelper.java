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

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import java.net.URL;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.services.repository.KnowledgeArtifactRepository;

public class PlatformComponentHelper {

  public static KnowledgeArtifactRepository repositoryDescr( String baseNamespace, String identifier, String name, URL baseURL ) {
    return new edu.mayo.kmdp.common.model.KnowledgeArtifactRepository()
        .withId(DatatypeHelper.uri(baseNamespace+"/repos/"+identifier))
        .withName( name )
        .withHref(baseURL+"/repos/"+identifier);
  }

}
