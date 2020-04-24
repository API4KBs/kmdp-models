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

import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.JSON;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.BPMN_2_0;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries.OWL2_QL;
import static edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries.OWL2_RL;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.DMN_1_1_XML_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Manchester_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;
import static edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries.Turtle;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.LOINC;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.RxNORM;
import static edu.mayo.ontology.taxonomies.lexicon.LexiconSeries.SNOMED_CT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;
import static org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder.decode;
import static org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder.encode;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder;
import org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder.WeightedRepresentation;

public class MimeCoderTest {

  @Test
  void testEncode1() {
    SyntacticRepresentation r1 = rep(BPMN_2_0, XML_1_1);
    assertEquals("model/bpmn-v2+xml", encode(r1));
  }

  @Test
  void testEncode2() {
    SyntacticRepresentation r2 = rep(DMN_1_1,
        DMN_1_1_XML_Syntax,
        XML_1_1);
    assertEquals("model/dmn-v11+xml", encode(r2));

  }

  @Test
  void testEncode3() {
    SyntacticRepresentation r3 = rep(OWL_2,
        OWL2_RL,
        OWL_Manchester_Syntax,
        TXT);
    assertEquals("model/owl2-v20121211[RL]+ms", encode(r3));
  }

  @Test
  void testEncode4() {
    SyntacticRepresentation r4 = rep(OWL_2,
        OWL2_RL,
        RDF_XML_Syntax,
        XML_1_1)
        .withLexicon(SNOMED_CT, LOINC);
    assertEquals("model/owl2[RL]+rdf/xml;lex={sct;lnc}", encode(r4, false));
  }


  @Test
  void testDecode1() {
    String mime = "model/owl2[QL]+ttl;lex={sct;rxnorm}";
    Optional<SyntacticRepresentation> rep = decode(mime);
    if (!rep.isPresent()) {
      fail("Unable to decode " + mime);
    }

    SyntacticRepresentation r = rep.get();
    assertEquals(OWL_2, r.getLanguage());
    assertEquals(OWL2_QL, r.getProfile());
    assertEquals(Turtle, r.getSerialization());
    assertEquals(TXT, r.getFormat());
    assertEquals(2,r.getLexicon().size());
    assertTrue(r.getLexicon().contains(SNOMED_CT));
    assertTrue(r.getLexicon().contains(RxNORM));
  }


  @Test
  void testWeights() {
    String c1 = "model/html;q=0.3;lex={sct}";
    SyntacticRepresentation rep1 = decode(c1)
        .orElse(new SyntacticRepresentation());
    assertSame(HTML,rep1.getLanguage());
    assertTrue(rep1.getLexicon().contains(SNOMED_CT));
  }

  @Test
  void testWeights2() {
    String c2 = "model/dmn-v11+xml;q=0.21";
    SyntacticRepresentation rep2 = decode(c2)
        .orElse(new SyntacticRepresentation());
    assertSame(DMN_1_1,rep2.getLanguage());
    assertSame(XML_1_1,rep2.getFormat());
  }

  @Test
  void testMappingHTML() {
    String m = "text/html";
    assertEquals("model/html-v52+text",
        decode(m)
            .map(ModelMIMECoder::encode)
            .orElse(""));
  }

  @Test
  void testWithCharset() {
    String m = "model/html-v52+text;charset=UTF-8";
    assertEquals("UTF-8", decode(m)
        .map(SyntacticRepresentation::getCharset).orElse("FAIL"));
  }

  @Test
  void testWithEncoding() {
    String m = "model/html-v52+text;enc=default";
    assertEquals("default", decode(m)
        .map(SyntacticRepresentation::getEncoding).orElse("FAIL"));
  }

  @Test
  void testWithCharsetAndEncoding() {
    String m = "model/html-v52+text;charset=UTF-8;enc=default";
    assertEquals("UTF-8", decode(m)
        .map(SyntacticRepresentation::getCharset).orElse("FAIL"));
    assertEquals("default", decode(m)
        .map(SyntacticRepresentation::getEncoding).orElse("FAIL"));
  }

  @Test
  void testFormalWithWeight() {
    String c1 = "model/bpmn+xml;q=0.3;lex={sct}";
    WeightedRepresentation rep = ModelMIMECoder.decodeWeighted(c1);
    assertEquals(0.3f, rep.getWeight());
    assertEquals(c1, rep.getCode());
    assertTrue(rep.getRep().isPresent());
    assertSame(BPMN_2_0, rep.getRep().map(SyntacticRepresentation::getLanguage).orElse(null));
  }

  @Test
  void testClassicWithWeight() {
    String c1 = "text/html;q=0.3;lex={sct}";
    WeightedRepresentation rep = ModelMIMECoder.decodeWeighted(c1);
    assertEquals(0.3f, rep.getWeight());
    assertEquals(c1, rep.getCode());
    assertTrue(rep.getRep().isPresent());
    assertSame(HTML, rep.getRep().map(SyntacticRepresentation::getLanguage).orElse(null));
  }

  @Test
  void testDecodeFormatOnly() {
    String c1 = "model/*+json";
    WeightedRepresentation rep = ModelMIMECoder.decodeWeighted(c1);
    assertEquals(1.0f, rep.getWeight());
    assertEquals(c1, rep.getCode());
    assertTrue(rep.getRep().isPresent());
    assertSame(JSON, rep.getRep().map(SyntacticRepresentation::getFormat).orElseGet(Assertions::fail));
    assertNull(rep.getRep().get().getLanguage());
  }
}
