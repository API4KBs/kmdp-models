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
import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.omg.spec.api4kp._1_0.identifiers.GAVIdentifier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.language.Transrepresentation;
import org.omg.spec.api4kp._1_0.services.language.Transrepresentator;

public class ServiceDescrCompilationTest {

  @TempDir
  public Path tmp;


  @Test
  public void testJaxbGeneration() {
    File tgt = compile();

    try {

      Class<?> txc = getNamedClass("org.omg.spec.api4kp._1_0.services.language.resources.Transrepresentator", tgt);
      Class<?> txr = getNamedClass("org.omg.spec.api4kp._1_0.services.language.resources.Transrepresentation", tgt);
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
          .unmarshall(tp.getClass(), Transrepresentator.class, xml, JaxbUtil.defaultProperties());
      assertTrue(asTrp.isPresent());

      Transrepresentator anew = asTrp.get();
      assertEquals(KRLanguage.BPMN_2_0_2,
          anew.getTxions().get(0).getConsumes().get(0).getLanguage());

      Transrepresentator t2 = new Transrepresentator();
      anew.copyTo(t2);

      assertFalse(tp.getClass() == t2.getClass());

      assertEquals(t2, tp);
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
    deploy(src, "/xsd/API4KP/api4kp/identifiers/bindings.xjb");

    deploy(src, "/xsd/edu/mayo/kmdp/terms/krformat/_2018/_08/KRFormat.xsd");
    deploy(src, "/xsd/edu/mayo/kmdp/terms/krlanguage/_2018/_08/KRLanguage.xsd");
    deploy(src, "/xsd/edu/mayo/kmdp/terms/krprofile/_2018/_08/KRProfile.xsd");
    deploy(src, "/xsd/edu/mayo/kmdp/terms/krserialization/_2018/_08/KRSerialization.xsd");
    deploy(src, "/xsd/edu/mayo/kmdp/terms/api4kp/parsinglevel/_20190801/ParsingLevel.xsd");
    deploy(src, "/xsd/edu/mayo/kmdp/terms/lexicon/_2018/_08/Lexicon.xsd");
    deploy(src,
        "/xsd/edu/mayo/kmdp/terms/iso639_1_languagecodes/_20170801/ISO639_1_LanguageCodes.xsd");

    deploy(src, "/xsd/API4KP/api4kp/services/services.xsd");
    deploy(src, "/xsd/API4KP/api4kp/services/services.openapi.xsd");
    deploy(src, "/xsd/API4KP/api4kp/services/transrepresentation/transrepresentation.xsd");
    deploy(src, "/xsd/API4KP/api4kp/services/transrepresentation/transrepresentation.openapi.xsd");

    deploy(src, "/xsd/terms-catalog.xml");

    showDirContent(folder);

    String jaxbPath = src.getPath() + "/xsd/API4KP";
    applyJaxb(Collections.singletonList(new File(jaxbPath)),
        Collections.emptyList(),
        gen,
        null,
        new File(src.getPath() + "/xsd/terms-catalog.xml"),
        true,
        false);

    ensureSuccessCompile(src, gen, tgt);

    return tgt;
  }


  private void init(Transrepresentator component, Transrepresentation rep) {
    SyntacticRepresentation syn = new SyntacticRepresentation().withLanguage(KRLanguage.BPMN_2_0_2);
    component
        .withInstanceId(uri("uri:urn:" + UUID.randomUUID()))
        .withTxions(new Transrepresentation()
            .withConsumes(syn)
            .withProduces(syn))
        .withKind(new GAVIdentifier()
            .withTag("aaa")
            .withVersion("1")
            .withNamespace("edu.test"));
  }

}