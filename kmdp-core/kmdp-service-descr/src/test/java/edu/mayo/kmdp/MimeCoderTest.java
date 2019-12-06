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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder;

public class MimeCoderTest {

  @Test
  public void testEncode1() {
    SyntacticRepresentation r1 = rep(BPMN_2_0, XML_1_1);
    assertEquals("model/bpmn-v2+xml", ModelMIMECoder.encode(r1));
  }

  @Test
  public void testEncode2() {
    SyntacticRepresentation r2 = rep(DMN_1_1,
        DMN_1_1_XML_Syntax,
        XML_1_1);
    assertEquals("model/dmn-v11+xml", ModelMIMECoder.encode(r2));

  }

  @Test
  public void testEncode3() {
    SyntacticRepresentation r3 = rep(OWL_2,
        OWL2_RL,
        OWL_Manchester_Syntax,
        TXT);
    assertEquals("model/owl2-v20121211[RL]+ms", ModelMIMECoder.encode(r3));
  }

  @Test
  public void testEncode4() {
    SyntacticRepresentation r4 = rep(OWL_2,
        OWL2_RL,
        RDF_XML_Syntax,
        XML_1_1)
        .withLexicon(SNOMED_CT, LOINC);
    assertEquals("model/owl2[RL]+rdf/xml;lex={sct,lnc}", ModelMIMECoder.encode(r4, false));
  }


  @Test
  public void testDecode1() {
    String mime = "model/owl2[QL]+ttl;lex={sct,rxnorm}";
    Optional<SyntacticRepresentation> rep = ModelMIMECoder.decode(mime);
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
  public void testRecodeApplicationCode() {
    String c = "application/xml";
    Optional<String> m = ModelMIMECoder.toModelCode(c, DMN_1_1);
    assertTrue(m.isPresent());

    Optional<SyntacticRepresentation> rep = m.flatMap(ModelMIMECoder::decode);
    assertTrue(rep.isPresent());
    assertSame(DMN_1_1,rep.get().getLanguage());
    assertSame(XML_1_1,rep.get().getFormat());
  }

  @Test
  public void testRecodeApplicationCode2() {
    String c = "text/html";
    Optional<String> m = ModelMIMECoder.toModelCode(c,DMN_1_1);
    assertTrue(m.isPresent());

    Optional<SyntacticRepresentation> rep = m.flatMap(ModelMIMECoder::decode);
    assertTrue(rep.isPresent());
    assertSame(HTML,rep.get().getLanguage());
    assertSame(TXT,rep.get().getFormat());
  }

  @Test
  public void testWeights() {
    String c1 = "model/html;q=0.3;lex={sct}";
    SyntacticRepresentation rep1 = ModelMIMECoder.decode(c1)
        .orElse(new SyntacticRepresentation());
    assertSame(HTML,rep1.getLanguage());
    assertSame(TXT,rep1.getFormat());
    assertTrue(rep1.getLexicon().contains(SNOMED_CT));
  }

  @Test
  public void testWeights2() {
    String c2 = "model/dmn-v11+xml;q=0.21";
    SyntacticRepresentation rep2 = ModelMIMECoder.decode(c2)
        .orElse(new SyntacticRepresentation());
    assertSame(DMN_1_1,rep2.getLanguage());
    assertSame(XML_1_1,rep2.getFormat());
  }


}
