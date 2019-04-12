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

import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.surrogate.Derivative;
import edu.mayo.kmdp.metadata.surrogate.ExternalCatalogEntry;
import edu.mayo.kmdp.metadata.surrogate.Implementation;
import edu.mayo.kmdp.metadata.surrogate.InlinedRepresentation;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeExpression;
import edu.mayo.kmdp.metadata.surrogate.MediaType;
import edu.mayo.kmdp.metadata.surrogate.ObjectFactory;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.terms.TermsHelper;
import edu.mayo.kmdp.terms.kao.knowledgeassetcategory._1_0.KnowledgeAssetCategory;
import edu.mayo.kmdp.terms.kao.knowledgeassettype._1_0.KnowledgeAssetType;
import edu.mayo.kmdp.terms.kao.rel.derivationreltype._20190801.DerivationRelType;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.kmdp.util.properties.jaxb.JaxbOptions;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Optional;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.name;
import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetadataTest {


  @Test
  public void testKS() {
    KnowledgeAssetCategory br = KnowledgeAssetCategory.Rules_Policies_And_Guidelines;
    KnowledgeAsset ks = new KnowledgeAsset()
        .withResourceId(uri("http://foo.bar", "234"))
        .withCategory(br)
        .withSubject(new SimpleAnnotation().withExpr(TermsHelper.sct("mock", "123")));

    ks = checkRoundTrip(ks);
  }


  @Test
  /**
   * Minimal descriptive information is gathered about an asset,
   * which itself is implemented in an external system, typically in
   * a proprietary / not transparent form
   */
  public void testScenario_BasicExternalImpl() {
    KnowledgeAsset ks = new KnowledgeAsset()

        .withResourceId(uri("http://foo.bar", "234"))

        .withCategory(KnowledgeAssetCategory.Rules_Policies_And_Guidelines)
        .withType(KnowledgeAssetType.Clinical_Rule)

        .withName("My Favorite Rule")
        .withDescription("When and Whether to Recommend What")

        .withExpression(new KnowledgeExpression()
            .withResourceId(uri("urn:to:do"))
            .withRepresentation(new Representation()
                .withLanguage(KRLanguage.KNART_1_3))
            // carrier + external catalog is not perfect
            .withCarrier(new ExternalCatalogEntry()
                .withResourceId(uri("urn:TODO"))
                .withMediaType(MediaType.APPLICATION)
                .withCatalogId(name("poc:RUL-12345")))
            .withInlined(new InlinedRepresentation()
                .withExpr("IF so and so DO nothing"))
            .withRelated(new Derivative()
                .withRel(DerivationRelType.Derived_From)
                // should I have an inverse flag here?
                .withTgt(new Implementation()
                    .withResourceId(uri("urn:TODO"))
                    .withMediaType(MediaType.APPLICATION)
                    .withImplId(name("mayo:LGL-123"))
                    .withSystemId(name("epic:mayo")))
            ));

    checkRoundTrip(ks);

    assertEquals(KnowledgeAssetCategory.Rules_Policies_And_Guidelines, ks.getCategory().get(0));
  }


  private KnowledgeAsset checkRoundTrip(KnowledgeAsset ks) {
    ObjectFactory of = new ObjectFactory();

    Optional<String> str = JaxbUtil.marshall(Collections.singleton(of.getClass()),
        ks,
        of::createKnowledgeAsset,
        JaxbUtil.defaultProperties()
            .with(JaxbOptions.SCHEMA_LOCATION,
                "http://kmdp.mayo.edu/metadata/surrogate /xsd/metadata/surrogate/surrogate.xsd"))
        .map(ByteArrayOutputStream::toByteArray)
        .map(String::new);

    assertTrue(str.isPresent());
    assertTrue(str.get().contains("xmlns:surr=\"http://kmdp.mayo.edu/metadata/surrogate\""));

    //System.out.println(str.get());

    Optional<Schema> schema = SurrogateHelper.getSchema();
    assertTrue(schema.isPresent());

    assertTrue(XMLUtil.validate(new StreamSource(new ByteArrayInputStream(str.get().getBytes())),
        schema.get()));

    KnowledgeAsset rec = JaxbUtil.unmarshall(of.getClass(),
        KnowledgeAsset.class,
        XMLUtil.loadXMLDocument(new ByteArrayInputStream(str.get().getBytes())).get(),
        JaxbUtil.defaultProperties()).get();
    assertEquals(ks, rec);
    return rec;
  }
}