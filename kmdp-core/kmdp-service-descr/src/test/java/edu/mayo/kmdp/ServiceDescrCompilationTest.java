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

import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static edu.mayo.kmdp.util.CodeGenTestBase.applyJaxb;
import static edu.mayo.kmdp.util.CodeGenTestBase.deploy;
import static edu.mayo.kmdp.util.CodeGenTestBase.ensureSuccessCompile;
import static edu.mayo.kmdp.util.CodeGenTestBase.getNamedClass;
import static edu.mayo.kmdp.util.CodeGenTestBase.initGenSourceFolder;
import static edu.mayo.kmdp.util.CodeGenTestBase.initSourceFolder;
import static edu.mayo.kmdp.util.CodeGenTestBase.initTargetFolder;
import static edu.mayo.kmdp.util.CodeGenTestBase.showDirContent;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.GAVIdentifier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.Transrepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.Transrepresentator;

public class ServiceDescrCompilationTest {

  @TempDir
  public Path tmp;


  @Test
  public void testJaxbGeneration() {
    File tgt = compile();

    try {

      Class<?> txc = getNamedClass("org.omg.spec.api4kp._1_0.services.tranx.resources.Transrepresentator", tgt);
      Class<?> txr = getNamedClass("org.omg.spec.api4kp._1_0.services.tranx.resources.Transrepresentation", tgt);
      assertNotNull(txc);
      assertNotNull(txr);

      Transrepresentator tp = (Transrepresentator) txc.newInstance();
      Transrepresentation tn = (Transrepresentation) txr.newInstance();
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
      assertEquals(KnowledgeRepresentationLanguage.BPMN_2_0,
          anew.getTxions().get(0).getConsumes().get(0).getLanguage());

      Transrepresentator t2 = new Transrepresentator();
      anew.copyTo(t2);

      assertFalse(tp.getClass() == t2.getClass());

      Transrepresentator t3 = new Transrepresentator();
      tp.copyTo(t3);

      assertEquals(t2, t3);
      assertEquals(tp.getTxions(), t2.getTxions());

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

    deploy(src, "/xsd/API4KP/api4kp/identifiers/identifiers.openapi.xsd");
    deploy(src, "/xsd/API4KP/api4kp/identifiers/identifiers.xsd");
    deploy(src, "/xsd/API4KP/api4kp/id/id.xsd");
    deploy(src, "/xsd/API4KP/api4kp/identifiers/bindings.xjb");
    deploy(src, "/xsd/API4KP/api4kp/datatypes/datatypes.xsd");
    deploy(src, "/xsd/API4KP/api4kp/datatypes/bindings.xjb");

    deploy(src, "/xsd/terms-bindings.xjb");

    deploy(src, "/xsd/edu/mayo/ontology/taxonomies/kao/languagerole/_20190801/KnowledgeRepresentationLanguageRole.xsd");
    deploy(src, "/xsd/edu/mayo/ontology/taxonomies/krformat/_20190801/SerializationFormat.xsd");
    deploy(src, "/xsd/edu/mayo/ontology/taxonomies/krlanguage/_20190801/KnowledgeRepresentationLanguage.xsd");
    deploy(src, "/xsd/edu/mayo/ontology/taxonomies/krprofile/_20190801/KnowledgeRepresentationLanguageProfile.xsd");
    deploy(src, "/xsd/edu/mayo/ontology/taxonomies/krserialization/_20190801/KnowledgeRepresentationLanguageSerialization.xsd");
    deploy(src, "/xsd/edu/mayo/ontology/taxonomies/api4kp/parsinglevel/_20190801/ParsingLevel.xsd");
    deploy(src, "/xsd/edu/mayo/ontology/taxonomies/lexicon/_20190801/Lexicon.xsd");
    deploy(src,
        "/xsd/edu/mayo/ontology/taxonomies/iso639_2_languagecodes/_20190201/Language.xsd");

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
    SyntacticRepresentation syn = new SyntacticRepresentation().withLanguage(KnowledgeRepresentationLanguage.BPMN_2_0);
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