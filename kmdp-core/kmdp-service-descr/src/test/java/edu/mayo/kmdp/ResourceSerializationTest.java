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
package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._1_0.PlatformComponentHelper.asParamDefinitions;

import com.fasterxml.jackson.databind.JsonNode;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.kmdp.util.XPathUtil;
import edu.mayo.kmdp.util.properties.jaxb.JaxbConfig;
import edu.mayo.kmdp.util.properties.jaxb.JaxbConfig.JaxbOptions;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.services.ParameterDefinitions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ResourceSerializationTest {

  @Test
  public void testParametersJSONSerialization() {
    JaxbConfig cfg = JaxbUtil.defaultProperties();
    ParameterDefinitions pd = asParamDefinitions(cfg);

    Optional<JsonNode> optNode = JSonUtil.printJson(pd)
        .flatMap(JSonUtil::readJson);

    assertTrue(optNode.isPresent());
    JsonNode jNode = optNode.get();

    assertTrue(jNode.isObject());
    optNode = JSonUtil.jNode("parameterDefinition",jNode);
    assertTrue(optNode.isPresent());
    jNode = optNode.get();
    assertTrue(jNode.isArray());
    assertEquals(cfg.properties().length, jNode.size());

    jNode = jNode.get(0);
    assertEquals(JaxbOptions.FORMATTED_OUTPUT.getName(),
        JSonUtil.jString("name", jNode).orElse(null));
    assertEquals(Boolean.FALSE,
        JSonUtil.jBool("required", jNode).orElse(null));
  }



  @Test
  public void testParametersXMLSerialization() {
    JaxbConfig cfg = new JaxbConfig();
    ParameterDefinitions pd = asParamDefinitions(cfg);

    Optional<String> optXML = JaxbUtil
        .marshall(Collections.singletonList(ParameterDefinitions.class), pd,
            JaxbUtil.defaultProperties())
        .flatMap(Util::asString);

    assertTrue(optXML.isPresent());

    Optional<Document> optDox = XMLUtil.loadXMLDocument(optXML.get().getBytes());
    assertTrue(optDox.isPresent());

    NodeList nodes = new XPathUtil().xList(optDox.get(), "//api:parameterDefinitions/api:parameterDefinition/@name");
    Set<String> paramNames = XMLUtil.asAttributeStream(nodes)
        .map(Node::getTextContent)
        .collect(Collectors.toSet());
    assertTrue(paramNames.contains(JaxbOptions.FORMATTED_OUTPUT.getName()));
  }

}
