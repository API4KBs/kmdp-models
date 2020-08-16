/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp;

import edu.mayo.kmdp.docx.DocXSerializer;
import edu.mayo.kmdp.idl.Direction;
import edu.mayo.kmdp.idl.Exception;
import edu.mayo.kmdp.idl.IDLNameUtil;
import edu.mayo.kmdp.idl.Interface;
import edu.mayo.kmdp.idl.Module;
import edu.mayo.kmdp.idl.Type;
import edu.mayo.kmdp.idl.ext.Service;
import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.NameUtils;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.responsecodes.ResponseCodeSeries;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.parser.Swagger20Parser;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwaggerToDocXTranslator extends AbstractSwaggerTranslator {

  private static final Logger logger = LoggerFactory.getLogger(SwaggerToDocXTranslator.class);

  public SwaggerToDocXTranslator(String title) {
    this.title = title;
  }

  public byte[] translate(List<InputStream> inputs) {
    return doTranslate(
        inputs.stream()
            .map(FileUtil::read)
            .flatMap(StreamUtil::trimStream)
            .map(this::parse)
            .flatMap(StreamUtil::trimStream)
            .collect(Collectors.toList()));
  }

  private byte[] doTranslate(List<Swagger> swaggers) {
    Swagger swagger = swaggers.get(0);

    Optional<Module> root = initModule(swagger.getBasePath());
    if (!root.isPresent()) {
      return "".getBytes();
    }

    TypeProvider provider = new TypeProvider(swaggers, root.get());

    return root
        .map(m -> this.withOperations(m, swagger, provider))
        .map(m -> this.withStructs(m, swagger, provider))
        .map(m -> new ModuleSorter().sort(m))
        .map(m -> toService(m, swagger))
        .map(DocXSerializer::serialize)
        .orElse("".getBytes());
  }

  @Override
  protected void logError(String message, IOException e) {
    logger.error(message, e);
  }
}