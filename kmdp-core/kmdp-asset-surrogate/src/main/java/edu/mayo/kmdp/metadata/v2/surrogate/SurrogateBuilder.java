/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.metadata.v2.surrogate;

import static edu.mayo.ontology.taxonomies.iso639_2_languagecodes.LanguageSeries.English;
import static edu.mayo.ontology.taxonomies.kao.knowledgeassetrole.KnowledgeAssetRoleSeries.Operational_Concept_Definition;
import static org.omg.spec.api4kp._1_0.id.IdentifierConstants.VERSION_ZERO;

import edu.mayo.kmdp.metadata.v2.surrogate.annotations.Annotation;
import edu.mayo.kmdp.metadata.v2.surrogate.annotations.Applicability;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory.KnowledgeAssetCategorySeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype.KnowledgeAssetTypeSeries;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import edu.mayo.ontology.taxonomies.kao.languagerole.KnowledgeRepresentationLanguageRoleSeries;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyType;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype.AnnotationRelTypeSeries;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import edu.mayo.ontology.taxonomies.lexicon.Lexicon;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.id.SemanticIdentifier;
import org.omg.spec.api4kp._1_0.id.Term;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class SurrogateBuilder {

  private KnowledgeAsset surrogate;

  protected SurrogateBuilder(ResourceIdentifier assetId, boolean root) {
    surrogate = newInstance(root)
        .withAssetId(assetId);
  }

  protected KnowledgeAsset newInstance(boolean root) {
    return root ? new edu.mayo.kmdp.metadata.v2.surrogate.KnowledgeAsset() : new KnowledgeAsset();
  }

  public static SurrogateBuilder newSurrogate(ResourceIdentifier assetId) {
    return newSurrogate(assetId, true);
  }

  public static SurrogateBuilder newSurrogate(ResourceIdentifier assetId, boolean root) {
    return new SurrogateBuilder(assetId, root)
        .withCanonicalSurrogate(assetId);
  }

  private SurrogateBuilder withCanonicalSurrogate(ResourceIdentifier assetId) {
    ResourceIdentifier surrogateId = artifactId(
        Util.uuid(assetId.getResourceId().toString()),
        "1.0.0"
    );
    get().withSurrogate(
        new ComputableKnowledgeArtifact()
            .withArtifactId(surrogateId)
            .withRepresentation(new SyntacticRepresentation()
                .withLanguage(KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate))
    );
    return this;
  }


  public SurrogateBuilder withName(String name, String descr) {
    surrogate.withName(name)
        .withDescription(descr);
    return this;
  }

  public SurrogateBuilder withValuesetType() {
    get().withFormalCategory(KnowledgeAssetCategorySeries.Terminology_Ontology_And_Assertional_KBs)
        .withFormalType(KnowledgeAssetTypeSeries.Value_Set);
    return this;
  }

  public SurrogateBuilder withServiceType() {
    get().withFormalCategory(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines)
        .withFormalType(KnowledgeAssetTypeSeries.Service_Description);
    return this;
  }

  public SurrogateBuilder withCohortDefinitionType() {
    get().withFormalCategory(
        KnowledgeAssetCategorySeries.Assessment_Predictive_And_Inferential_Models)
        .withFormalType(KnowledgeAssetTypeSeries.Cohort_Definition);
    return this;
  }

  public SurrogateBuilder withQueryType() {
    get().withFormalCategory(KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines)
        .withFormalType(KnowledgeAssetTypeSeries.Inquiry_Specification);
    return this;
  }

  public SurrogateBuilder withContentType() {
    get().withFormalCategory(KnowledgeAssetCategorySeries.Terminology_Ontology_And_Assertional_KBs)
        .withFormalType(KnowledgeAssetTypeSeries.Factual_Knowledge);
    return this;
  }

  public SurrogateBuilder withExpressionType() {
    get().withFormalCategory(
        KnowledgeAssetCategorySeries.Assessment_Predictive_And_Inferential_Models)
        .withFormalType(KnowledgeAssetTypeSeries.Functional_Expression);
    return this;
  }

  public SurrogateBuilder withFormalType(KnowledgeAssetCategory cat, KnowledgeAssetType type) {
    get().withFormalCategory(cat)
        .withFormalType(type);
    return this;
  }

  public SurrogateBuilder withDecisionAidType() {
    get().withFormalCategory(
        KnowledgeAssetCategorySeries.Assessment_Predictive_And_Inferential_Models)
        .withFormalType(KnowledgeAssetTypeSeries.Computable_Decision_Model);
    return this;
  }


  public SurrogateBuilder aaS() {
    get().withProcessingMethod(KnowledgeProcessingTechniqueSeries.Service_Based_Technique);
    return this;
  }

  public SurrogateBuilder withApplicability(Term t) {
    if (t == null) {
      return this;
    }
    get().withApplicableIn(new Applicability()
        .withSituation(t.asConceptIdentifier()));
    return this;
  }


  public SurrogateBuilder asOperationalDefinition(Term subject, Term proposition,
      Term... inputs) {
    get().withRole(Operational_Concept_Definition);

    if (proposition != null) {
      this.withAnnotation(AnnotationRelTypeSeries.Defines.asConceptIdentifier(),
          proposition.asConceptIdentifier());
    }

    if (subject != null) {
      this.withAnnotation(AnnotationRelTypeSeries.Has_Primary_Subject.asConceptIdentifier(),
          subject.asConceptIdentifier());
    }

    Arrays.stream(inputs).forEach(input ->
        this.withAnnotation(AnnotationRelTypeSeries.In_Terms_Of.asConceptIdentifier(),
            input.asConceptIdentifier())
    );

    return this;
  }


  public SurrogateBuilder withDMNExpression(KnowledgeRepresentationLanguage schema) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(
          new ComputableKnowledgeArtifact()
              .withRepresentation(new SyntacticRepresentation()
                  .withLanguage(KnowledgeRepresentationLanguageSeries.DMN_1_1)
                  .withFormat(SerializationFormatSeries.XML_1_1)
                  .withSubLanguage(new SyntacticRepresentation()
                      .withRole(KnowledgeRepresentationLanguageRoleSeries.Schema_Language)
                      .withLanguage(schema))));
    }
    return this;
  }


  public SurrogateBuilder withOpenAPIExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(
          new ComputableKnowledgeArtifact()
              .withRepresentation(new SyntacticRepresentation()
                  .withLanguage(KnowledgeRepresentationLanguageSeries.OpenAPI_2_X)
                  .withFormat(SerializationFormatSeries.YAML_1_2)));
    }
    return this;
  }

  public SurrogateBuilder withHTMLExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact()
          .withLocalization(English)
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(KnowledgeRepresentationLanguageSeries.HTML)
              .withFormat(SerializationFormatSeries.TXT)));
    }
    return this;
  }

  public SurrogateBuilder withSKOSExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact()
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(KnowledgeRepresentationLanguageSeries.OWL_2)
              .withFormat(SerializationFormatSeries.RDF_1_1)
              .withProfile(KnowledgeRepresentationLanguageProfileSeries.OWL2_DL)
              .withLexicon(LexiconSeries.SKOS)));
    }
    return this;
  }


  public SurrogateBuilder withCQLExpression(Lexicon lex) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact()
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(KnowledgeRepresentationLanguageSeries.HL7_CQL)
              .withFormat(SerializationFormatSeries.TXT)
              .withLexicon(lex)));
    }
    return this;
  }


  public SurrogateBuilder withInlinedFhirPath(String expr) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact()
          .withArtifactId(artifactId(expr != null ? Util.uuid(expr) : UUID.randomUUID(), "LATEST"))
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(KnowledgeRepresentationLanguageSeries.FHIRPath_STU1)
              .withFormat(SerializationFormatSeries.TXT))
          .withInlinedExpression(expr));
    }
    return this;
  }


  public SurrogateBuilder withRepresentation(KnowledgeRepresentationLanguage lang,
      SerializationFormat format) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact()
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(lang)
              .withFormat(format)));
    }
    return this;
  }


  public SurrogateBuilder withCarriers(ResourceIdentifier id, URI loc) {
    if (get().getCarriers().isEmpty()) {
      KnowledgeArtifact carrier = new ComputableKnowledgeArtifact()
          .withArtifactId(id)
          .withLocator(loc);
      get().withCarriers(carrier);
    } else {
      ((ComputableKnowledgeArtifact) get().getCarriers().get(0))
          .withArtifactId(id)
          .withLocator(loc);
    }

    return this;
  }

  public SurrogateBuilder withAnnotation(Term rel, Term obj) {
    get().withAnnotation(new Annotation()
        .withRel(rel.asConceptIdentifier())
        .withRef(obj.asConceptIdentifier()));
    return this;
  }

  public SurrogateBuilder withDependency(DependencyType rel, ResourceIdentifier relatedAsset) {
    get().withLinks(
        new Dependency()
            .withRel(rel)
            .withHref(relatedAsset));
    return this;
  }


  public KnowledgeAsset get() {
    return surrogate;
  }

  public static ResourceIdentifier assetId(String uuid, String versionTag) {
    return SemanticIdentifier.newId(Registry.MAYO_ASSETS_BASE_URI_URI, uuid, versionTag);
  }

  public static ResourceIdentifier assetId(UUID uuid, String versionTag) {
    return SemanticIdentifier.newId(Registry.MAYO_ASSETS_BASE_URI_URI, uuid, versionTag);
  }

  public static ResourceIdentifier randomAssetId() {
    return assetId(UUID.randomUUID(),VERSION_ZERO);
  }

  public static ResourceIdentifier artifactId(String uuid, String versionTag) {
    return SemanticIdentifier.newId(Registry.MAYO_ARTIFACTS_BASE_URI_URI, uuid, versionTag);
  }

  public static ResourceIdentifier artifactId(UUID uuid, String versionTag) {
    return SemanticIdentifier.newId(Registry.MAYO_ARTIFACTS_BASE_URI_URI, uuid, versionTag);
  }

  public static ResourceIdentifier randomArtifactId() {
    return artifactId(UUID.randomUUID(),VERSION_ZERO);
  }

}
