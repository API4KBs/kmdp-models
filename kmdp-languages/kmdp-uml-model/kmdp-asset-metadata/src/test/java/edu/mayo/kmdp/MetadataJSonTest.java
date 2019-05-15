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
import edu.mayo.kmdp.metadata.surrogate.Derivative;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JenaUtil;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory._1_0.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.rel.citationreltype._2018_02_16.BibliographicCitationType;
import edu.mayo.ontology.taxonomies.kao.rel.derivationreltype._20190801.DerivationType;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

//import org.springframework.hateoas.Link;
//import org.springframework.hateoas.Resource;

public class MetadataJSonTest {

  @Test
  public void testAssetCore() {
    KnowledgeAsset ks = new KnowledgeAsset()
        .withResourceId(uri("http://foo.bar", "234"));

    String jsonTree = toJson(ks);
    //System.out.println(jsonTree);

    Model triples = toTriples(jsonTree);
    int n = JenaUtil.sizeOf(triples);

//    JenaUtil.toSystemOut(triples);

  }

  @Test
  @Disabled("Role of HATEOAS not yet fully evaluated")
  public void testSerializationWithHateoas() {
    KnowledgeAsset ks = new KnowledgeAsset()

        .withResourceId(uri("http://foo.bar", "234"))

        .withCategory(KnowledgeAssetCategory.Rules_Policies_And_Guidelines)

        .withDescription("This is a test")

        .withRelated(new Derivative()
                .withRel(DerivationType.Abdridgement_Of)
                .withTgt(new KnowledgeArtifact()
                    .withResourceId(uri("http://foo.bar/234"))),
            new Derivative()
                .withRel(DerivationType.Derived_From)
                .withTgt(new KnowledgeAsset()
                    .withResourceId(uri("http://foo.bar/234"))),
            new Citation()
                .withRel(BibliographicCitationType.Cites));

//    Resource<KnowledgeAsset> axx = new Resource<>(ks);
//    axx.add(new Link("http://foo.bax").withRel("goto")
//        .withHref("http://www.google.it.yourself.com")
//        .withTitle("What next")
//        .withType("http://my/onto/type/this_or_that_operation"));

//    String jsonTree = toJsonLD(ks);
//    System.out.println(jsonTree);
//
//		JenaUtil.toSystemOut( toTriples( jsonTree ) );

  }

  private String toJsonLD(Object x) {
    assertNotNull(x);

    Optional<ByteArrayOutputStream> baos = JSonUtil.writeJsonLD(x);
    assertTrue(baos.isPresent());
    Optional<?> y = JSonUtil.readJson(baos.get().toByteArray(), x.getClass());
    assertTrue(y.isPresent());
    assertEquals(x, y.get());

    return new String(baos.get().toByteArray());
  }

  private String toJson(Object x) {
    assertNotNull(x);

    Optional<ByteArrayOutputStream> baos = JSonUtil.writeJson(x);
    assertTrue(baos.isPresent());
    Optional<?> y = JSonUtil.readJson(baos.get().toByteArray(), x.getClass());
    assertTrue(y.isPresent());
    assertEquals(x, y.get());

    return new String(baos.get().toByteArray());
  }

  private Model toTriples(String json) {
    Optional<Model> model = JenaUtil.fromJsonLD(json);
    assertTrue(model.isPresent());
    return model.get();
  }

}
