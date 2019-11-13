/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.adapters.json.ConceptTermsJsonAdapter;
import edu.mayo.kmdp.terms.adapters.json.URITermsJsonAdapter;
import edu.mayo.kmdp.terms.adapters.json.UUIDTermsJsonAdapter;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.Test;

public class TermsSerializationTest {

  @Test
  public void testXML() {
    String xml = JaxbUtil.marshallToString(Collections.singleton(Foo.class),
        new Foo(KnowledgeAssetTypeSeries.Cognitive_Process_Model,
            KnowledgeRepresentationLanguageSeries.OWL_2),
        JaxbUtil.defaultProperties());

    assertTrue(xml.contains(KnowledgeAssetTypeSeries.Cognitive_Process_Model.getTag()));
    assertTrue(xml.contains(KnowledgeAssetTypeSeries.Cognitive_Process_Model.getLabel()));
    assertTrue(xml.contains(KnowledgeAssetTypeSeries.Cognitive_Process_Model.getRef().toString()));
    assertTrue(xml.contains(KnowledgeRepresentationLanguageSeries.OWL_2.getTag()));
    assertTrue(xml.contains(KnowledgeRepresentationLanguageSeries.OWL_2.getLabel()));
    assertTrue(xml.contains(KnowledgeRepresentationLanguageSeries.OWL_2.getRef().toString()));

    Optional<Foo> f2 = XMLUtil.loadXMLDocument(xml.getBytes())
        .flatMap(dox ->
            JaxbUtil.unmarshall(Collections.singleton(Foo.class), Foo.class, dox));

    assertTrue(f2.isPresent());
    assertSame(KnowledgeAssetTypeSeries.Cognitive_Process_Model, f2.get().getType());
    assertSame(KnowledgeRepresentationLanguageSeries.OWL_2, f2.get().getLang());
  }

  @Test
  public void testJSON() {
    Foo f = new Foo(KnowledgeAssetTypeSeries.Cognitive_Process_Model,
        KnowledgeRepresentationLanguageSeries.OWL_2);

    String json = JSonUtil
        .writeJson(f)
        .flatMap(Util::asString).get();

    assertTrue(json.contains(KnowledgeAssetTypeSeries.Cognitive_Process_Model.getTag()));
    assertTrue(json.contains(KnowledgeAssetTypeSeries.Cognitive_Process_Model.getLabel()));
    assertTrue(json.contains(KnowledgeAssetTypeSeries.Cognitive_Process_Model.getRef().toString()));
    assertTrue(json.contains(KnowledgeRepresentationLanguageSeries.OWL_2.getTag()));
    assertTrue(json.contains(KnowledgeRepresentationLanguageSeries.OWL_2.getLabel()));
    assertTrue(json.contains(KnowledgeRepresentationLanguageSeries.OWL_2.getRef().toString()));

    Optional<Foo> f2 = JSonUtil.readJson(json.getBytes(), Foo.class);

    assertTrue(f2.isPresent());
    assertEquals(KnowledgeAssetTypeSeries.Cognitive_Process_Model, f2.get().getType());
    assertEquals(KnowledgeRepresentationLanguageSeries.OWL_2, f2.get().getLang());

    assertEquals(1, f2.get().getLang().getTags().size());
  }

  @Test
  public void testJSONSimple() {
    Bar b = new Bar();
    b.lang = KnowledgeRepresentationLanguageSeries.DMN_1_2.getLatest();
    b.type = KnowledgeAssetTypeSeries.Decision_Model;

    String s = JSonUtil.writeJson(b)
        .flatMap(Util::asString)
        .orElse("");

    assertTrue(s.contains(KnowledgeAssetTypeSeries.Decision_Model.getConceptUUID().toString()));
    assertTrue(s.contains(
        "\"lang\" : \"" + KnowledgeRepresentationLanguageSeries.DMN_1_2.getConceptUUID() + "\""));

    Bar b2 = JSonUtil.parseJson(s, Bar.class)
        .orElse(null);

    assertNotNull(b2);
    assertEquals(KnowledgeAssetTypeSeries.Decision_Model, b2.getType());
    assertEquals(KnowledgeRepresentationLanguageSeries.DMN_1_2, b2.getLang());
  }

  @Test
  public void testJSONWithInterface() {
    Baz b = new Baz();
    b.format = SerializationFormatSeries.XML_1_1.getLatest();

    String s = JSonUtil.writeJson(b)
        .flatMap(Util::asString)
        .orElse("");
    assertFalse(Util.isEmpty(s));

    Baz b2 = JSonUtil.parseJson(s, Baz.class)
        .orElse(null);
    assertNotNull(b2);

    assertSame(edu.mayo.ontology.taxonomies.krformat.snapshot.SerializationFormat.XML_1_1,
        b2.getFormat());
  }


  @XmlRootElement
  public static class Foo {

    @JsonSerialize(using = ConceptTermsJsonAdapter.Serializer.class)
    @JsonDeserialize(using = AssetDeserializer.class)
    private KnowledgeAssetType type;
    @JsonSerialize(using = ConceptTermsJsonAdapter.Serializer.class)
    @JsonDeserialize(using = LangDeserializer.class)
    private KnowledgeRepresentationLanguage lang;

    public Foo() {
      // needed for deserialization
    }

    public Foo(KnowledgeAssetType type, KnowledgeRepresentationLanguage lang) {
      this.type = type;
      this.lang = lang;
    }

    public KnowledgeAssetType getType() {
      return type;
    }

    public void setType(KnowledgeAssetType type) {
      this.type = type;
    }

    public KnowledgeRepresentationLanguage getLang() {
      return lang;
    }

    public void setLang(KnowledgeRepresentationLanguage lang) {
      this.lang = lang;
    }

    public static class LangDeserializer extends ConceptTermsJsonAdapter.Deserializer {

      @Override
      protected Term[] getValues() {
        return KnowledgeRepresentationLanguageSeries.values();
      }
    }

    public static class AssetDeserializer extends ConceptTermsJsonAdapter.Deserializer {

      @Override
      protected Term[] getValues() {
        return KnowledgeAssetTypeSeries.values();
      }
    }
  }

  public static class Bar {

    protected edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetType type;

    @JsonSerialize(using = UUIDTermsJsonAdapter.Serializer.class)
    @JsonDeserialize(using = BarDeserializer.class)
    protected KnowledgeRepresentationLanguage lang;

    public Bar() {
      // needed for deserialization
    }

    public KnowledgeAssetType getType() {
      return type;
    }

    public void setType(
        KnowledgeAssetType type) {
      this.type = type;
    }

    public KnowledgeRepresentationLanguage getLang() {
      return lang;
    }

    public void setLang(
        KnowledgeRepresentationLanguage lang) {
      this.lang = lang;
    }

    public static class BarDeserializer extends UUIDTermsJsonAdapter.Deserializer {
      @Override
      protected Term[] getValues() {
        return KnowledgeRepresentationLanguageSeries.values();
      }

      @Override
      protected Optional resolveUUID(UUID uuid) {
        return KnowledgeRepresentationLanguageSeries.resolveUUID(uuid);
      }
    }
  }

  @XmlRootElement
  public static class Baz {

    @JsonSerialize(using = URITermsJsonAdapter.Serializer.class)
    protected SerializationFormat format;

    public Baz() {
      // needed for deserialization
    }

    public SerializationFormat getFormat() {
      return format;
    }

    public void setFormat(
        SerializationFormat format) {
      this.format = format;
    }

    public static class BazURIDeserializer extends URITermsJsonAdapter.Deserializer {
      protected Term[] getValues() {
        return SerializationFormatSeries.values();
      }

      @Override
      protected Optional<SerializationFormat> resolveUUID(UUID uuid) {
        return SerializationFormatSeries.resolveUUID(uuid);
      }
    }
  }
}
