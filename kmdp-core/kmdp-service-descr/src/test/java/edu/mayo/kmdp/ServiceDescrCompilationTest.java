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

import static edu.mayo.kmdp.util.CodeGenTestBase.applyJaxb;
import static edu.mayo.kmdp.util.CodeGenTestBase.deploy;
import static edu.mayo.kmdp.util.CodeGenTestBase.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestBase.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestBase.initGenSourceFolder;
import static edu.mayo.kmdp.util.CodeGenTestBase.initSourceFolder;
import static edu.mayo.kmdp.util.CodeGenTestBase.initTargetFolder;
import static edu.mayo.kmdp.util.CodeGenTestBase.showDirContent;
import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.BPMN_2_0;

import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.services.transrepresentation.Transrepresentation;
import org.omg.spec.api4kp._20200801.services.transrepresentation.Transrepresentator;

public class ServiceDescrCompilationTest {

  @TempDir
  public Path tmp;


  @Test
  public void testJaxbGeneration() {
    File tgt = compile();

    try {

      Class<?> txc = getNamedClass("org.omg.spec.api4kp._20200801.services.transrepresentation.resources.Transrepresentator", tgt);
      Class<?> txr = getNamedClass("org.omg.spec.api4kp._20200801.services.transrepresentation.resources.Transrepresentation", tgt);
      assertNotNull(txc);
      assertNotNull(txr);

      Transrepresentator tp = (Transrepresentator) txc.getConstructor().newInstance();
      Transrepresentation tn = (Transrepresentation) txr.getConstructor().newInstance();
      init(tp, tn);

      String xml = JaxbUtil
          .marshallToString(Collections.singleton(txc), tp, JaxbUtil.defaultProperties());
      //System.out.println(xml);

      Optional<Schema> schema = getSchemas(ServiceDescrCompilationTest.class.getResource(
          "/xsd/API4KP/api4kp/services/transrepresentation/transrepresentation.openapi.xsd"),
          catalogResolver("/xsd/api4kp-catalog.xml",
              "/xsd/terms-catalog.xml"));
      assertTrue(schema.isPresent());
      assertTrue(XMLUtil.validate(xml, schema.get()));

      Optional<Transrepresentator> asTrp = JaxbUtil
          .unmarshall(tp.getClass(), Transrepresentator.class, xml);
      assertTrue(asTrp.isPresent());

      Transrepresentator anew = asTrp.get();
      assertTrue(BPMN_2_0.isSame(
          anew.getTxions().get(0).getConsumes().get(0).getLanguage()));

      Transrepresentator t2 = new Transrepresentator();
      anew.copyTo(t2);

      assertNotSame(tp.getClass(), t2.getClass());

      Transrepresentator t3 = new Transrepresentator();
      tp.copyTo(t3);

      assertEquals(t2.getInstanceId(), t3.getInstanceId());
      assertEquals(t2.getKind(), t3.getKind());

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  private File compile() {
    File folder = tmp.toFile();
    assertTrue(folder.exists());

    File src = initSourceFolder(folder);
    File gen = initGenSourceFolder(folder);
    File tgt = initTargetFolder(folder);

    deploy(src, "/xsd/API4KP/api4kp/id/id.xsd");
    deploy(src, "/xsd/API4KP/api4kp/id/bindings.xjb");
    deploy(src, "/xsd/API4KP/api4kp/datatypes/datatypes.xsd");
    deploy(src, "/xsd/API4KP/api4kp/datatypes/bindings.xjb");

    deploy(src, "/xsd/terms-bindings.xjb");

    deploy(src, "/xsd/org/omg/spec/api4kp/taxonomy/languagerole/KnowledgeRepresentationLanguageRole.series.xsd");
    deploy(src, "/xsd/org/omg/spec/api4kp/taxonomy/krformat/SerializationFormat.series.xsd");
    deploy(src, "/xsd/org/omg/spec/api4kp/taxonomy/krlanguage/KnowledgeRepresentationLanguage.series.xsd");
    deploy(src, "/xsd/org/omg/spec/api4kp/taxonomy/krprofile/KnowledgeRepresentationLanguageProfile.series.xsd");
    deploy(src, "/xsd/org/omg/spec/api4kp/taxonomy/krserialization/KnowledgeRepresentationLanguageSerialization.series.xsd");
    deploy(src, "/xsd/org/omg/spec/api4kp/taxonomy/parsinglevel/ParsingLevel.series.xsd");
    deploy(src, "/xsd/org/omg/spec/api4kp/taxonomy/lexicon/Lexicon.series.xsd");
    deploy(src,
        "/xsd/org/omg/spec/api4kp/taxonomy/iso639_2_languagecode/Language.series.xsd");

    deploy(src, "/xsd/API4KP/api4kp/services/services.xsd");
    deploy(src, "/xsd/API4KP/api4kp/services/services.openapi.xsd");
    deploy(src, "/xsd/API4KP/api4kp/services/transrepresentation/transrepresentation.xsd");
    deploy(src, "/xsd/API4KP/api4kp/services/transrepresentation/transrepresentation.openapi.xsd");

    deploy(src, "/xsd/terms-catalog.xml");

    showDirContent(folder);

    String jaxbPath = src.getPath() + "/xsd/API4KP";
    applyJaxb(Collections.singletonList(new File(jaxbPath)),
        Arrays.asList( new File( src.getPath() + "/xsd/terms-bindings.xjb")),
        gen,
        null,
        new File(src.getPath() + "/xsd/terms-catalog.xml"),
        true,
        false);

    ensureSuccessCompile(src, gen, tgt);

    return tgt;
  }


  private void init(Transrepresentator component, Transrepresentation rep) {
    SyntacticRepresentation syn = new SyntacticRepresentation().withLanguage(BPMN_2_0);
    component
        .withInstanceId(SemanticIdentifier.newId(UUID.randomUUID()))
        .withTxions(new Transrepresentation()
            .withConsumes(syn)
            .withProduces(syn))
        .withKind(SemanticIdentifier.newId(UUID.randomUUID())
            .withTag("aaa")
            .withVersionTag("1")
            .withNamespaceUri(URI.create("edu.test")));
  }

}