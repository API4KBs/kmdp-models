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

import static edu.mayo.kmdp.util.XMLUtil.validate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.v2.surrogate.ObjectFactory;
import edu.mayo.kmdp.metadata.v2.surrogate.SurrogateHelper;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.Annotation;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.SimpleAnnotation;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.kmdp.util.properties.jaxb.JaxbConfig.JaxbOptions;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory._20190801.KnowledgeAssetCategory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Optional;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;

public class RootElementTest {


  @Test
  public void testAnnotationRoundtrip() {
    KnowledgeAssetCategory br = KnowledgeAssetCategory.Rules_Policies_And_Guidelines;
    SimpleAnnotation anno = new edu.mayo.kmdp.metadata.v2.surrogate.annotations.resources.SimpleAnnotation()
        .withExpr(
            new ConceptIdentifier()
                .withRef(br.getRef())
                .withTag("BusinessRuleAsset")
                .withLabel(br.getLabel())
                .withNamespace((NamespaceIdentifier) br.getNamespace()));

    Annotation rec = checkRoundTrip(anno);
    assertNotNull(rec);
    String ann = JaxbUtil.marshallToString(Collections.singleton(rec.getClass()),
        rec,
        JaxbUtil.defaultProperties());

    rec = (Annotation) rec.copyTo(new SimpleAnnotation());

    KnowledgeAsset kas = new KnowledgeAsset().withSubject(rec);
    String str = JaxbUtil.marshallToString(Collections.singleton(kas.getClass()),
        kas,
        new ObjectFactory()::createKnowledgeAsset,
        JaxbUtil.defaultProperties());

    assertTrue(str.contains(br.getTag()));
    assertTrue(ann.contains(br.getTag()));
  }


  private Annotation checkRoundTrip(final Annotation anno) {
    ObjectFactory of = new ObjectFactory();

    Optional<String> str = JaxbUtil.marshall(Collections.singleton(anno.getClass()),
        anno,
        JaxbUtil.defaultProperties()
            .with(JaxbOptions.SCHEMA_LOCATION,
                "http://kmdp.mayo.edu/metadata/surrogate /xsd/metadata/surrogate/surrogate.xsd"))
        .map(ByteArrayOutputStream::toByteArray)
        .map(String::new);

    Optional<Schema> schema = SurrogateHelper.getSchema();
    assertTrue(schema.isPresent());

    assertTrue(str.isPresent());
    schema.ifPresent(sch -> assertTrue(
        validate(
            new StreamSource(new ByteArrayInputStream(str.orElse("").getBytes())),
            sch)));

    Annotation rec = XMLUtil.loadXMLDocument(new ByteArrayInputStream(str.orElse("").getBytes()))
        .flatMap(dox -> JaxbUtil.unmarshall(of.getClass(), anno.getClass(), dox))
        .orElse(null);
    assertEquals(anno, rec);
    return rec;
  }

}