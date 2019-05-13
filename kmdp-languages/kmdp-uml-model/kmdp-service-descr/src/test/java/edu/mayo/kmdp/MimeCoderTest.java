package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.ontology.taxonomies.krformat._2018._08.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krprofile._2018._08.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krserialization._2018._08.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.lexicon._2018._08.Lexicon;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.omg.spec.api4kp._1_0.services.language.ModelMIMECoder;

public class MimeCoderTest {

  @Test
  public void testEncode1() {
    SyntacticRepresentation r1 = rep(KnowledgeRepresentationLanguage.BPMN_2_0,
        SerializationFormat.XML_1_1);
    assertEquals("model/bpmn-v2+xml", ModelMIMECoder.encode(r1));
  }

  @Test
  public void testEncode2() {
    SyntacticRepresentation r2 = rep(KnowledgeRepresentationLanguage.DMN_1_1,
        KnowledgeRepresentationLanguageSerialization.DMN_1_1_XML_Syntax,
        SerializationFormat.XML_1_1);
    assertEquals("model/dmn-v11+xml", ModelMIMECoder.encode(r2));

  }

  @Test
  public void testEncode3() {
    SyntacticRepresentation r3 = rep(KnowledgeRepresentationLanguage.OWL_2,
        KnowledgeRepresentationLanguageProfile.OWL2_RL,
        KnowledgeRepresentationLanguageSerialization.OWL_Manchester_Syntax,
        SerializationFormat.TXT);
    assertEquals("model/owl2-v20121211[RL]+ms", ModelMIMECoder.encode(r3));
  }

  @Test
  public void testEncode4() {
    SyntacticRepresentation r4 = rep(KnowledgeRepresentationLanguage.OWL_2,
        KnowledgeRepresentationLanguageProfile.OWL2_RL,
        KnowledgeRepresentationLanguageSerialization.RDF_XML_Syntax,
        SerializationFormat.XML_1_1)
        .withLexicon(Lexicon.SNOMED_CT, Lexicon.LOINC);
    assertEquals("model/owl2[RL]+rdf/xml+{sct,lnc}", ModelMIMECoder.encode(r4, false));
  }


  @Test
  public void testDecode1() {
    String mime = "model/owl2[QL]+ttl+{sct,rxnorm}";
    Optional<SyntacticRepresentation> rep = ModelMIMECoder.decode(mime);
    if (!rep.isPresent()) {
      fail("Unable to decode " + mime);
    }

    SyntacticRepresentation r = rep.get();
    assertEquals(KnowledgeRepresentationLanguage.OWL_2, r.getLanguage());
    assertEquals(KnowledgeRepresentationLanguageProfile.OWL2_QL, r.getProfile());
    assertEquals(KnowledgeRepresentationLanguageSerialization.Turtle, r.getSerialization());
    assertEquals(SerializationFormat.TXT, r.getFormat());
    assertEquals(2,r.getLexicon().size());
    assertTrue(r.getLexicon().contains(Lexicon.SNOMED_CT));
    assertTrue(r.getLexicon().contains(Lexicon.RxNORM));
  }


}
