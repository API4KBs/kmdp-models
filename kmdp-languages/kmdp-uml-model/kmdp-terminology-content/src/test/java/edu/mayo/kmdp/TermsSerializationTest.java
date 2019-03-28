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

import edu.mayo.kmdp.terms.kao.knowledgeassettype._1_0.KnowledgeAssetType;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import org.junit.jupiter.api.Test;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TermsSerializationTest {

  @Test
  public void testXML() {
    String xml = JaxbUtil.marshallToString(Collections.singleton(Foo.class),
        new Foo(KnowledgeAssetType.Cognitive_Process_Model, KRLanguage.OWL_2),
        JaxbUtil.defaultProperties());
    //System.out.println(xml);
    assertTrue(xml.contains(KnowledgeAssetType.Cognitive_Process_Model.getTag()));
    assertTrue(xml.contains(KnowledgeAssetType.Cognitive_Process_Model.getLabel()));
    assertTrue(xml.contains(KnowledgeAssetType.Cognitive_Process_Model.getRef().toString()));
    assertTrue(xml.contains(KRLanguage.OWL_2.getTag()));
    assertTrue(xml.contains(KRLanguage.OWL_2.getLabel()));
    assertTrue(xml.contains(KRLanguage.OWL_2.getRef().toString()));

    Optional<Foo> f2 = JaxbUtil.unmarshall(Collections.singleton(Foo.class),
        Foo.class,
        XMLUtil.loadXMLDocument(xml.getBytes()).get(),
        JaxbUtil.defaultProperties());
    assertTrue(f2.isPresent());
    assertEquals(KnowledgeAssetType.Cognitive_Process_Model, f2.get().getType());
    assertEquals(KRLanguage.OWL_2, f2.get().getLang());
  }

  @Test
  public void testJSON() {
    String json = JSonUtil
        .writeJson(new Foo(KnowledgeAssetType.Cognitive_Process_Model, KRLanguage.OWL_2))
        .flatMap(Util::asString).get();

//    System.out.println(json);

    assertTrue(json.contains(KnowledgeAssetType.Cognitive_Process_Model.getTag()));
    assertTrue(json.contains(KnowledgeAssetType.Cognitive_Process_Model.getLabel()));
    assertTrue(json.contains(KnowledgeAssetType.Cognitive_Process_Model.getRef().toString()));
    assertTrue(json.contains(KRLanguage.OWL_2.getTag()));
    assertTrue(json.contains(KRLanguage.OWL_2.getLabel()));
    assertTrue(json.contains(KRLanguage.OWL_2.getRef().toString()));

    Optional<Foo> f2 = JSonUtil.readJson(json.getBytes(), Foo.class);

    assertTrue(f2.isPresent());
    assertEquals(KnowledgeAssetType.Cognitive_Process_Model, f2.get().getType());
    assertEquals(KRLanguage.OWL_2, f2.get().getLang());
  }


  @XmlRootElement
  public static class Foo {

    private KnowledgeAssetType type;
    private KRLanguage lang;

    public Foo() {

    }

    public Foo(KnowledgeAssetType type, KRLanguage lang) {
      this.type = type;
      this.lang = lang;
    }

    public KnowledgeAssetType getType() {
      return type;
    }

    public void setType(KnowledgeAssetType type) {
      this.type = type;
    }

    public KRLanguage getLang() {
      return lang;
    }

    public void setLang(KRLanguage lang) {
      this.lang = lang;
    }
  }
}
