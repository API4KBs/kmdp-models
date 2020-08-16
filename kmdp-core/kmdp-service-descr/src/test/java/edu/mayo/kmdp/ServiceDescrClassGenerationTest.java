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

import static edu.mayo.kmdp.util.FileUtil.streamChildFiles;
import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.identifiers.GAVIdentifier;
import org.omg.spec.api4kp._20200801.identifiers.URIIdentifier;
import org.omg.spec.api4kp._20200801.services.Job;
import org.omg.spec.api4kp._20200801.services.JobStatus;
import org.omg.spec.api4kp._20200801.services.KnowledgePlatformComponent;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.services.repository.KnowledgeArtifactRepository;
import org.omg.spec.api4kp._20200801.services.tranx.Transrepresentation;
import org.omg.spec.api4kp._20200801.services.tranx.TransrepresentationOperator;
import org.omg.spec.api4kp._20200801.services.tranx.Transrepresentator;

public class ServiceDescrClassGenerationTest {

  @Test
  public void testClassExists() {
    KnowledgeArtifactRepository repository = new KnowledgeArtifactRepository();
    assertNotNull(repository);

    TransrepresentationOperator op = new TransrepresentationOperator();
    assertNotNull(op);

    KnowledgePlatformComponent component = new Transrepresentator();
    assertNotNull(component);
  }


  @Test
  public void testSerialization() {
    SyntacticRepresentation syn = new SyntacticRepresentation().withLanguage(KnowledgeRepresentationLanguage.BPMN_2_0);

    Transrepresentator component = new Transrepresentator()
        .withInstanceId(SemanticIdentifier.newId(UUID.randomUUID()))
        .withTxions(new Transrepresentation()
            .withConsumes(syn)
            .withProduces(syn))
        .withKind(SemanticIdentifier.newId(UUID.randomUUID())
            .withTag("aaa")
            .withVersionTag("1")
            .withNamespaceUri(URI.create("edu.test")));

    org.omg.spec.api4kp._20200801.services.tranx.ObjectFactory of = new org.omg.spec.api4kp._20200801.services.tranx.ObjectFactory();
    String xml = JaxbUtil.marshallToString(Collections.singleton(of.getClass()),
        component,
        of::createTransrepresentator,
        JaxbUtil.defaultProperties());

    //System.out.println(xml);

    Optional<Schema> schema = getSchemas(ServiceDescrClassGenerationTest.class
            .getResource("/xsd/API4KP/api4kp/services/transrepresentation/transrepresentation.xsd"),
        catalogResolver("/xsd/api4kp-catalog.xml", "/xsd/terms-catalog.xml"));
    assertTrue(schema.isPresent());
    assertTrue(XMLUtil.validate(xml, schema.get()));

  }


  @Test
  public void testCodegeneration() {
    URL url = ServiceDescrClassGenerationTest.class.getResource("/");
    //System.out.println(url);
    try {
      File f = new File(url.toURI());
      assertTrue(f.exists());

      String genPath = f.getParent() +
          ".generated-sources.xjc.".replaceAll("\\.", Matcher.quoteReplacement(File.separator));
      f = new File(genPath
          + org.omg.spec.api4kp._20200801.services.repository.KnowledgeArtifactRepository.class.getPackage().getName()
          .replaceAll("\\.", Matcher.quoteReplacement(File.separator)));
      assertTrue(f.exists());
      assertTrue(f.isDirectory());

      List<String> files = streamChildFiles(f)
          .map(File::getPath)
          .map((s) -> s.replace(genPath, ""))
          .map((s) -> s.replace(File.separator, "."))
          .map((s) -> s.replace(".java", ""))
          .collect(Collectors.toList());

//      files.forEach(System.out::println);

      assertTrue(
          files.contains(org.omg.spec.api4kp._20200801.services.repository.KnowledgeArtifactRepository.class.getName()));
      assertFalse(files.contains(org.omg.spec.api4kp._20200801.services.tranx.Transrepresentation.class.getName()));

    } catch (URISyntaxException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testJobResource() {
    Job job = new Job().withId(UUID.randomUUID().toString())
        .withStatus(JobStatus.STARTED)
        .withRedirectUrl(URI.create("http://te.st"));

    assertEquals(JobStatus.STARTED, job.getStatus());
  }

  @Test
  public void testTag() {
    ResourceIdentifier id = SemanticIdentifier.newId(URI.create("http://foo.bar/kinda/123456"));
    assertEquals("123456",id.getTag());
  }

}
