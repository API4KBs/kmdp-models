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
package edu.mayo.kmdp.registry;

import static edu.mayo.kmdp.registry.RegistryUtil.askQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class RegistryOntologyTest extends RegistryTestBase {


  static Model registry;

  @BeforeAll
  static void init() {
    registry = initRegistry("LATEST");
  }

  @Test
  public void testLanguages() {
    String qry = PREAMBLE +
        "SELECT ?L " +
        " " +
        "WHERE { " +
        "   ?L a api4kp:ConstructedLanguage. " +
        "}";

    List<Map<String, String>> ans = askQuery(qry, registry);
    Collection<String> langs = ans.stream().map(Map::values)
        .reduce(new HashSet<>(), (s1, s2) -> {
          s1.addAll(s2);
          return s1;
        });

    assertTrue(langs.contains("https://www.omg.org/spec/DMN/1.2/"));
    assertTrue(langs.contains("http://www.w3.org/ns/owl-profile/QL"));
  }

  @Test
  public void testLanguageIDs() {
    String qry = PREAMBLE +
        "SELECT ?L ?Id ?Ver " +
        " " +
        "WHERE { " +
        "   ?L a api4kp:ConstructedLanguage;"
        + "   dct:identifier ?Id; "
        + "   owl:versionInfo ?Ver. " +
        "}";

    List<Map<String, String>> ans = askQuery(qry, registry);
    Map<String,String> ids = ans.stream().collect(Collectors.toMap(
        res -> res.get("L"),
        res -> res.get("Id") + "-" + res.get("Ver")
    ));

    assertEquals("ccpm-v1",
        ids.get("http://kmdp.mayo.edu/ccpm/1.0"));

  }

  @Test
  public void testLanguageLexicalNamespaces() {
    String qry = PREAMBLE +
        "SELECT ?L ?Id ?Ver " +
        " " +
        "WHERE { " +
        "   ?L a api4kp:ConstructedLanguage;"
        + "   dct:identifier ?Id. "
        + "   OPTIONAL { ?L owl:versionInfo ?Ver }" +
        "}";

    List<Map<String, String>> ans = askQuery(qry, registry);
    Map<String,String> ids = ans.stream().collect(Collectors.toMap(
        res -> res.get("L"),
        res -> res.get("Id") +
            (isEmpty(res.get("Ver")) ? "" : ("-" + res.get("Ver")))
    ));

    assertEquals("ccpm-v1",
        ids.get("http://kmdp.mayo.edu/ccpm/1.0"));
    assertEquals("QL",
        ids.get("http://www.w3.org/ns/owl-profile/QL"));

  }

  private boolean isEmpty(String ver) {
    return ver == null || ver.length() == 0;
  }


  @Test
  public void testLanguageSerializations() {
    String qry = PREAMBLE +
        "SELECT ?L ?Ser ?LangId ?SerId " +
        " " +
        "WHERE { " +
        "   ?L a api4kp:ConstructedLanguage;"
        + "   dct:identifier ?LangId; "
        + "   dol:supportsSerialization ?Ser. "
        + " ?Ser dct:identifier ?SerId. "
        + "}";

    List<Map<String, String>> ans = askQuery(qry, registry);

    Map<String,String> ids = ans.stream().collect(Collectors.toMap(
        res -> res.get("SerId"),
        res -> res.get("LangId")
    ));

    assertEquals("cmmn", ids.get("cmmn-v11+xml"));
    assertEquals("dmn", ids.get("dmn-v11+xml"));
    assertEquals("dmn", ids.get("dmn-v12+xml"));
    assertEquals("owl2", ids.get("rdf/xml"));
    assertEquals("owl2", ids.get("ms"));
  }


  @Test
  public void testUsesLexicon() {
    String qry = PREAMBLE +
        "SELECT ?LangNS ?LexNS " +
        " " +
        "WHERE { " +
        "   ?LangNS a api4kp:ConstructedLanguage; "
        + "   api4kp:uses-lexicon ?LexNS. "
        + ""
        + " ?LexNS dct:identifier ?LexId."
        + "}";

    List<Map<String, String>> ans = askQuery(qry, registry);

    Map<String, Set<String>> uses = new HashMap<>();
    ans.forEach(m -> {
          String lang = m.get("LangNS");
          assertNotNull(lang);
          if (!uses.containsKey(lang)) {
            uses.put(lang, new HashSet<>());
          }
          String lex = m.get("LexNS");
          assertNotNull(lex);
          uses.get(lang).add(lex);
        }
    );

    assertTrue(uses.containsKey("http://hl7.org/fhir/DSTU2"));
    Set<String> fhirLexica = uses.get("http://hl7.org/fhir/DSTU2");
    assertTrue(fhirLexica.contains("http://loinc.org/"));
    assertTrue(fhirLexica.contains("http://snomed.info/sct/900000000000207008/version/20180731"));
    assertTrue(fhirLexica.contains("https://www.nlm.nih.gov/research/umls/rxnorm/"));

    assertEquals(8, uses.keySet().size());

    assertEquals(18,
        uses.getOrDefault("https://www.omg.org/spec/API4KP/1.0/surrogate", new HashSet<>()).size());

  }

  @Test
  public void testMetaFormatUse() {
    String qry = PREAMBLE +
        "SELECT ?LangId ?FmtId " +
        " " +
        "WHERE { " +
        "   ?L a api4kp:ConstructedLanguage; "
        + "   dct:identifier ?LangId; "
        + "   dol:supportsSerialization ?Ser. "
        + ""
        + " ?Ser api4kp:depends-on ?Fmt."
        + ""
        + " ?Fmt a api4kp:MetaFormat; "
        + "   dct:identifier ?FmtId "
        + "}";

    List<Map<String, String>> ans = askQuery(qry, registry);

    Map<String, Set<String>> formats = new HashMap<>();
    ans.forEach(m -> {
          String lang = m.get("LangId");
          assertNotNull(lang);
          if (!formats.containsKey(lang)) {
            formats.put(lang, new HashSet<>());
          }
          String lex = m.get("FmtId");
          assertNotNull(lex);
          formats.get(lang).add(lex);
        }
    );

    assertEquals(new HashSet<>(Arrays.asList("json", "xml")),
        formats.get("api"));

    assertEquals(new HashSet<>(Arrays.asList("json", "xml", "rdf")),
        formats.get("fhir"));

    assertEquals(new HashSet<>(Arrays.asList("json", "yaml")),
        formats.get("openapi"));

  }


  @Test
  public void testGrammarSchemas() {
    String qry = PREAMBLE +
        "SELECT ?LangId ?G " +
        " " +
        "WHERE { " +
        "   ?L a api4kp:ConstructedLanguage; "
        + "   dct:identifier ?LangId; "
        + "   dol:supportsSerialization ?G. "
        + "}";

    List<Map<String, String>> ans = askQuery(qry, registry);
    Map<String,String> grams = ans.stream().collect(Collectors.toMap(
        res -> res.get("G"),
        res -> res.get("LangId")
    ));
    assertEquals( "cmmn",
        grams.get("http://www.omg.org/spec/CMMN/20151109/MODEL"));
  }


}
