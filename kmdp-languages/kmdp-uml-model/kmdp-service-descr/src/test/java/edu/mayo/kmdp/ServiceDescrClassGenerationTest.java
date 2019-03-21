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

import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.GAVIdentifier;
import org.omg.spec.api4kp._1_0.services.Job;
import org.omg.spec.api4kp._1_0.services.JobStatus;
import org.omg.spec.api4kp._1_0.services.KnowledgePlatformComponent;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.language.Transrepresentation;
import org.omg.spec.api4kp._1_0.services.language.TransrepresentationOperator;
import org.omg.spec.api4kp._1_0.services.language.Transrepresentator;
import org.omg.spec.api4kp._1_0.services.repository.KnowledgeArtifactRepository;

import javax.xml.validation.Schema;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ServiceDescrClassGenerationTest {

  @Test
  public void testClassExists() {
    KnowledgeArtifactRepository repository = new KnowledgeArtifactRepository();

    TransrepresentationOperator op = new TransrepresentationOperator();

    KnowledgePlatformComponent component = new Transrepresentator();
  }


  @Test
  public void testSerialization() {
    SyntacticRepresentation syn = new SyntacticRepresentation().withLanguage(KRLanguage.BPMN_2_0_2);

    Transrepresentator component = new Transrepresentator()
        .withInstanceId(uri("uri:urn:" + UUID.randomUUID()))
        .withTxions(new Transrepresentation()
            .withConsumes(syn)
            .withProduces(syn))
        .withKind(new GAVIdentifier()
            .withTag("aaa")
            .withVersion("1")
            .withNamespace("edu.test"));

    org.omg.spec.api4kp._1_0.services.language.ObjectFactory of = new org.omg.spec.api4kp._1_0.services.language.ObjectFactory();
    String xml = JaxbUtil.marshallToString(Collections.singleton(of.getClass()),
        component,
        of::createTransrepresentator,
        JaxbUtil.defaultProperties());

    System.out.println(xml);

    Optional<Schema> schema = getSchemas(ServiceDescrClassGenerationTest.class
            .getResource("/xsd/API4KP/api4kp/services/transrepresentation/transrepresentation.xsd"),
        catalogResolver("/xsd/api4kp-catalog.xml", "/xsd/terms-catalog.xml"));
    assertTrue(schema.isPresent());
    assertTrue(XMLUtil.validate(xml, schema.get()));

  }


  @Test
  public void testCodegeneration() {
    URL url = ServiceDescrClassGenerationTest.class.getResource("/");
    System.out.println(url);
    try {
      File f = new File(url.toURI());
      assertTrue(f.exists());

      String genPath = f.getParent() +
          ".generated-sources.xjc.".replaceAll("\\.", Matcher.quoteReplacement(File.separator));
      f = new File(genPath
          + edu.mayo.kmdp.common.model.KnowledgeArtifactRepository.class.getPackage().getName()
          .replaceAll("\\.", Matcher.quoteReplacement(File.separator)));
      assertTrue(f.exists());
      assertTrue(f.isDirectory());

      List<String> files = Arrays.stream(f.listFiles())
          .map(File::getPath)
          .map((s) -> s.replace(genPath, ""))
          .map((s) -> s.replace(File.separator, "."))
          .map((s) -> s.replace(".java", ""))
          .collect(Collectors.toList());

      files.forEach(System.out::println);

      assertTrue(
          files.contains(edu.mayo.kmdp.common.model.KnowledgeArtifactRepository.class.getName()));
      assertTrue(files.contains(edu.mayo.kmdp.common.model.Transrepresentation.class.getName()));

    } catch (URISyntaxException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testJobResource() {
    Job job = new edu.mayo.kmdp.common.model.Job().withId(UUID.randomUUID().toString())
        .withStatus(JobStatus.STARTED)
        .withRedirectUrl(URI.create("http://te.st"));

    assertEquals(JobStatus.STARTED, job.getStatus());
  }

}
