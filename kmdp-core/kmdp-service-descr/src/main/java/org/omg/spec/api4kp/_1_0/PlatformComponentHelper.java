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
package org.omg.spec.api4kp._20200801;

import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN_URI;
import static edu.mayo.kmdp.util.Util.isEmpty;
import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_ZERO;

import edu.mayo.kmdp.ConfigProperties;
import edu.mayo.kmdp.Option;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.xml.namespace.QName;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.ParameterDefinition;
import org.omg.spec.api4kp._20200801.services.repository.KnowledgeArtifactRepository;
import org.omg.spec.api4kp._20200801.services.resources.ParameterDefinitions;

public class PlatformComponentHelper {

  private PlatformComponentHelper() {
    // static methods only
  }

  public static Optional<KnowledgeArtifactRepository> repositoryDescr(String baseNamespace, String identifier,
      String name, URL baseUrl) {
    if (isEmpty(identifier) || isEmpty(baseNamespace) || baseUrl == null) {
      return Optional.empty();
    }
    return Optional.of(new org.omg.spec.api4kp._20200801.services.repository.resources.KnowledgeArtifactRepository()
        .withInstanceId(
            SemanticIdentifier.newId(BASE_UUID_URN_URI, UUID.randomUUID(), VERSION_ZERO))
        .withId(SemanticIdentifier.newId(URI.create(baseNamespace + "/repos/" + identifier)))
        .withAlias(SemanticIdentifier.newId(BASE_UUID_URN_URI, identifier, VERSION_ZERO))
        .withName(name)
        .withHref(URI.create(baseUrl + "/repos/" + identifier)));
  }

  public static <O extends Option<O>> ParameterDefinitions asParamDefinitions(
      ConfigProperties<?, O> config) {
    ParameterDefinitions defs = new ParameterDefinitions();

    Arrays.stream(config.properties()).forEach(
        opt -> defs.withParameterDefinition(
            new ParameterDefinition()
                .withName(opt.getName())
                .withDefinition(opt.getDefinition())
                .withDefaultValue(opt.getDefaultValue())
                .withRequired(opt.isRequired())
                .withType(new QName(opt.getType().getPackage().getName(),
                    opt.getType().getSimpleName()))));

    return defs;
  }

  public static Properties defaults(
      org.omg.spec.api4kp._20200801.services.ParameterDefinitions acceptableParams) {
    Properties prop = new Properties();
    acceptableParams.getParameterDefinition().forEach(p -> {
      if ( !Util.isEmpty(p.getDefaultValue())) {
        prop.put(p.getName(), p.getDefaultValue());
      }
    });
    return prop;
  }
}
