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

import static edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat.TXT;
import static edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage.DMN_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage.HTML;
import static edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage.OWL_2;
import static edu.mayo.ontology.taxonomies.krprofile._20190801.KnowledgeRepresentationLanguageProfile.OWL2_QL;
import static edu.mayo.ontology.taxonomies.krserialization._20190801.KnowledgeRepresentationLanguageSerialization.Turtle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krprofile._20190801.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krserialization._20190801.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.lexicon._20190801.Lexicon;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.tranx.ModelMIMECoder;

public class MimeCoderTest {

  @Test
  public void testEncode1() {
    SyntacticRepresentation r1 = rep(KnowledgeRepresentationLanguage.BPMN_2_0,
        SerializationFormat.XML_1_1);
    assertEquals("model/bpmn-v2+xml", ModelMIMECoder.encode(r1));
  }

  @Test
  public void testEncode2() {
    SyntacticRepresentation r2 = rep(DMN_1_1,
        KnowledgeRepresentationLanguageSerialization.DMN_1_1_XML_Syntax,
        SerializationFormat.XML_1_1);
    assertEquals("model/dmn-v11+xml", ModelMIMECoder.encode(r2));

  }

  @Test
  public void testEncode3() {
    SyntacticRepresentation r3 = rep(OWL_2,
        KnowledgeRepresentationLanguageProfile.OWL2_RL,
        KnowledgeRepresentationLanguageSerialization.OWL_Manchester_Syntax,
        TXT);
    assertEquals("model/owl2-v20121211[RL]+ms", ModelMIMECoder.encode(r3));
  }

  @Test
  public void testEncode4() {
    SyntacticRepresentation r4 = rep(OWL_2,
        KnowledgeRepresentationLanguageProfile.OWL2_RL,
        KnowledgeRepresentationLanguageSerialization.RDF_XML_Syntax,
        SerializationFormat.XML_1_1)
        .withLexicon(Lexicon.SNOMED_CT, Lexicon.LOINC);
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
    assertTrue(r.getLexicon().contains(Lexicon.SNOMED_CT));
    assertTrue(r.getLexicon().contains(Lexicon.RxNORM));
  }


  @Test
  public void testRecodeApplicationCode() {
    String c = "application/xml";
    Optional<String> m = ModelMIMECoder.toModelCode(c, DMN_1_1);
    assertTrue(m.isPresent());

    Optional<SyntacticRepresentation> rep = m.flatMap(ModelMIMECoder::decode);
    assertTrue(rep.isPresent());
    assertSame(DMN_1_1,rep.get().getLanguage());
    assertSame(SerializationFormat.XML_1_1,rep.get().getFormat());
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
    assertTrue(rep1.getLexicon().contains(Lexicon.SNOMED_CT));
  }

  @Test
  public void testWeights2() {
    String c2 = "model/dmn+xml;q=0.21";
    SyntacticRepresentation rep2 = ModelMIMECoder.decode(c2)
        .orElse(new SyntacticRepresentation());
    assertSame(DMN_1_1,rep2.getLanguage());
    assertSame(XML_1_1,rep2.getFormat());
  }


}
