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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.contrastors.LexiconContrastor.theLexiconContrastor;
import static org.omg.spec.api4kp._20200801.contrastors.SyntacticRepresentationContrastor.theRepContrastor;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.OWL_Functional_Syntax;
import static org.omg.spec.api4kp._20200801.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries.RDF_XML_Syntax;

import edu.mayo.kmdp.comparator.Contrastor.Comparison;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.services.transrepresentation.ModelMIMECoder;

class RepresentationComparisonTest {

  @Test
  void languageTestWithSpecificVersion() {
    SyntacticRepresentation r1 = rep(OWL_2)
        .withSerialization(RDF_XML_Syntax);
    SyntacticRepresentation r2 = rep(OWL_2)
        .withSerialization(OWL_Functional_Syntax);
    SyntacticRepresentation r3 = rep(DMN_1_1);
    SyntacticRepresentation r4 = rep(OWL_2);

    assertEquals(Comparison.INCOMPARABLE,theRepContrastor.contrast(r1,r2));
    assertEquals(Comparison.INCOMPARABLE,theRepContrastor.contrast(r1,r3));
    assertEquals(Comparison.NARROWER,theRepContrastor.contrast(r1,r4));
  }


  @Test
  void languageTestWithSeries() {
    SyntacticRepresentation r1 = rep(OWL_2)
        .withSerialization(RDF_XML_Syntax);

    SyntacticRepresentation r2 = rep(OWL_2)
        .withSerialization(RDF_XML_Syntax);

    SyntacticRepresentation r3 = rep(OWL_2.getLatest())
        .withSerialization(RDF_XML_Syntax.getLatest());

    assertEquals(Comparison.EQUAL,theRepContrastor.contrast(r1,r2));
    assertEquals(Comparison.EQUIVALENT,theRepContrastor.contrast(r1,r3));
    assertEquals(Comparison.EQUIVALENT,theRepContrastor.contrast(r2,r3));
  }

  @Test
  void languageLessTest() {
    // mimic "application/json" and "application/xml"
    SyntacticRepresentation r1 = rep(null, JSON);
    SyntacticRepresentation r2 = rep(null, JSON);
    SyntacticRepresentation r3 = rep(null, XML_1_1);

    assertEquals(Comparison.EQUAL,theRepContrastor.contrast(r1,r2));
    assertEquals(Comparison.INCOMPARABLE,theRepContrastor.contrast(r1,r3));
    assertEquals(Comparison.INCOMPARABLE,theRepContrastor.contrast(r2,r3));
  }

  @Test
  void concreteRep() {
    SyntacticRepresentation r1 = ModelMIMECoder.decode("model/owl2-v20121211+rdf/xml;charset=UTF-8")
        .orElseGet(Assertions::fail);

    SyntacticRepresentation r2 = ModelMIMECoder.decode("model/owl2-v20121211;lex={skos}")
        .orElseGet(Assertions::fail);

    assertTrue(theRepContrastor.isNarrowerOrEqual(r1,r2));
  }

  @Test
  void lexiconContrast() {
    SyntacticRepresentation r1 = ModelMIMECoder.decode("model/owl2")
        .orElseGet(Assertions::fail);
    SyntacticRepresentation r2 = ModelMIMECoder.decode("model/owl2;lex={skos}")
        .orElseGet(Assertions::fail);
    SyntacticRepresentation r3 = ModelMIMECoder.decode("model/owl2")
        .orElseGet(Assertions::fail);
    SyntacticRepresentation r4 = ModelMIMECoder.decode("model/owl2;lex={sct}")
        .orElseGet(Assertions::fail);
    SyntacticRepresentation r5 = ModelMIMECoder.decode("model/owl2;lex={sct;skos}")
        .orElseGet(Assertions::fail);

    assertSame(Comparison.BROADER,
        theLexiconContrastor.contrast(r1.getLexicon(), r2.getLexicon()));
    assertSame(Comparison.BROADER,
        theLexiconContrastor.contrast(r2.getLexicon(), r5.getLexicon()));

    assertSame(Comparison.NARROWER,
        theLexiconContrastor.contrast(r2.getLexicon(), r1.getLexicon()));
    assertSame(Comparison.NARROWER,
        theLexiconContrastor.contrast(r5.getLexicon(), r4.getLexicon()));

    assertSame(Comparison.EQUAL,
        theLexiconContrastor.contrast(r1.getLexicon(), r3.getLexicon()));
    assertSame(Comparison.IDENTICAL,
        theLexiconContrastor.contrast(r1.getLexicon(), r1.getLexicon()));

    assertSame(Comparison.INCOMPARABLE,
        theLexiconContrastor.contrast(r2.getLexicon(), r4.getLexicon()));
  }

}
