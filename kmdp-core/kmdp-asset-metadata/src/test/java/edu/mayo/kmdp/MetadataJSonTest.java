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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.metadata.surrogate.Citation;
import edu.mayo.kmdp.metadata.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.Derivative;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import edu.mayo.ontology.taxonomies.kao.rel.citationreltype.BibliographicCitationTypeSeries;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationTypeSeries;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;

//import org.springframework.hateoas.Link;
//import org.springframework.hateoas.Resource;

public class MetadataJSonTest {

  @Test
  void testAssetCore() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withAssetId(uri("http://foo.bar", "234"));

    String jsonTree = toJson(ks);
    //System.out.println(jsonTree);

    Model triples = toTriples(jsonTree);
    int n = JenaUtil.sizeOf(triples);

//    JenaUtil.toSystemOut(triples);

  }

  @Test
  void testSerialization() {
    KnowledgeAsset ks = new KnowledgeAsset()

        .withAssetId(uri("http://foo.bar", "234"))

        .withFormalCategory(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines.getLatest())

        .withDescription("This is a test")

        .withRelated(new Derivative()
                .withRel(DerivationTypeSeries.Abdridgement_Of)
                .withTgt(new ComputableKnowledgeArtifact()
                    .withArtifactId(uri("http://foo.bar/234"))),
            new Derivative()
                .withRel(DerivationTypeSeries.Derived_From)
                .withTgt(new KnowledgeAsset()
                    .withAssetId(uri("http://foo.bar/234"))))
        .withCitations(
            new Citation()
                .withRel(BibliographicCitationTypeSeries.Cites));
    assertNotNull(ks);

    assertNotNull(toJson(ks));
  }


  private String toJson(Object x) {
    assertNotNull(x);

    String json = JSonUtil.writeJsonAsString(x)
        .orElse("");

    Optional<?> y = JSonUtil.readJson(json, x.getClass());
    assertTrue(y.isPresent());

    assertEquals(x, y.get());

    return json;
  }

  private Model toTriples(String json) {
    Optional<Model> model = JenaUtil.fromJsonLD(json);
    assertTrue(model.isPresent());
    return model.get();
  }

}
