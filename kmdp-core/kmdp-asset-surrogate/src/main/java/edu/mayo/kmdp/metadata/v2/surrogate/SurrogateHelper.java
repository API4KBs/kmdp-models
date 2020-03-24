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
package edu.mayo.kmdp.metadata.v2.surrogate;

import edu.mayo.kmdp.metadata.v2.surrogate.annotations.Annotation;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRole;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.validation.Schema;
import org.omg.spec.api4kp._1_0.id.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class SurrogateHelper {

  protected SurrogateHelper() {

  }

  public static Optional<Schema> getSchema() {
    return XMLUtil.getSchemas(
        SurrogateHelper.class.getResource("/xsd/metadata/v2/surrogate/surrogate.xsd"),
        XMLUtil.catalogResolver("/xsd/km-metadata-catalog.xml", "/xsd/terms-catalog.xml"));
  }


  public static Optional<ConceptIdentifier> getSimpleAnnotationValue(KnowledgeAsset asset,
      ConceptIdentifier rel) {
    return asset.getAnnotation().stream()
        .filter(ann -> rel == null || rel.getUuid().equals(ann.getRel().getUuid()))
        .map(Annotation::getRef)
        .findAny();
  }


  public static SyntacticRepresentation canonicalRepresentationOf(KnowledgeAsset asset) {
    if (asset == null
        || asset.getCarriers().isEmpty()
        || !(asset.getCarriers().get(0) instanceof ComputableKnowledgeArtifact)) {
      return null;
    }
    ComputableKnowledgeArtifact artifact = (ComputableKnowledgeArtifact) asset.getCarriers().get(0);
    return artifact.getRepresentation();
  }

  public static Stream<SyntacticRepresentation> expandRepresentation(
      SyntacticRepresentation rep,
      KnowledgeRepresentationLanguageRole role) {
    if (rep == null) {
      return Stream.empty();
    }
    return Stream.concat(
        role == null ? Stream.of(rep) : Stream.empty(),
        rep.getSubLanguage() != null
            ? rep.getSubLanguage().stream()
            .filter(sub -> role == null || sub.getRole().asEnum() == role
                || sub.getRole().hasAncestor(role))
            .flatMap(sub -> expandRepresentation(sub, role))
            : Stream.empty());
  }

  public static Set<KnowledgeRepresentationLanguage> getSublanguages(SyntacticRepresentation rep,
      KnowledgeRepresentationLanguageRole role) {
    return expandRepresentation(rep, role)
        .map(SyntacticRepresentation::getLanguage)
        .collect(Collectors.toSet());
  }

  /**
   * Helper method that looks for any ComputableKnowledgeArtifact among the Artifacts
   * referenced in a Knowledge Asset Surrogate
   * @param surr  The Surrogate of the Asset for which a Computable Manifestation is requested
   * @return  Any Computable Variant of the asset, if present
   */
  public static Optional<ComputableKnowledgeArtifact> getComputableCarrier(
      KnowledgeAsset surr) {
    return surr.getCarriers().stream()
        .flatMap(StreamUtil.filterAs(ComputableKnowledgeArtifact.class))
        .findAny();
  }

  /**
   * Helper method that looks for the ComputableKnowledgeArtifact with a given ID and version
   * among the Artifact referenced in a Knowledge Asset Surrogate
   * @param artifactId The ID of the Computable Artifact to retrieve
   * @param artifactVersionTag The version Tag of the Computable Artifact to retrieve
   * @param surr  The Surrogate of the Asset for which a Computable Manifestation is requested
   * @return  The Computable Variant of the asset with the given ID and version, if present
   */
  public static Optional<ComputableKnowledgeArtifact> getComputableCarrier(UUID artifactId,
      String artifactVersionTag,
      KnowledgeAsset surr) {
    return surr.getCarriers().stream()
        .filter(a -> a.getArtifactId().getTag().equals(artifactId.toString())
            && a.getArtifactId().getVersionTag().equals(artifactVersionTag))
        .flatMap(StreamUtil.filterAs(ComputableKnowledgeArtifact.class))
        .findAny();
  }

}