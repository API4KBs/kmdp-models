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
package org.omg.spec.api4kp._20200801.surrogate;

import static org.omg.spec.api4kp._20200801.id.IdentifierConstants.VERSION_ZERO;
import static org.omg.spec.api4kp._20200801.taxonomy.iso639_2_languagecode.LanguageSeries.English;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Assessment_Predictive_And_Inferential_Models;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Rules_Policies_And_Guidelines;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategorySeries.Terminology_Ontology_And_Assertional_KBs;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetrole.KnowledgeAssetRoleSeries.Operational_Concept_Definition;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Cohort_Definition;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Computable_Decision_Model;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Factual_Knowledge;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Functional_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Inquiry_Specification;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Service_Description;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetTypeSeries.Value_Set;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.RDF_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.YAML_1_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.DMN_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.FHIRPath_STU1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HL7_CQL;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.Knowledge_Asset_Surrogate;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OWL_2;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.OpenAPI_2_X;
import static org.omg.spec.api4kp._20200801.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries.OWL2_DL;
import static org.omg.spec.api4kp._20200801.taxonomy.languagerole.KnowledgeRepresentationLanguageRoleSeries.Schema_Language;
import static org.omg.spec.api4kp._20200801.taxonomy.lexicon.LexiconSeries.KRR_Technique;
import static org.omg.spec.api4kp._20200801.taxonomy.lexicon.LexiconSeries.SKOS;

import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.kmdo.semanticannotationreltype.SemanticAnnotationRelTypeSeries;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.surrogate.Annotation;
import org.omg.spec.api4kp._20200801.surrogate.Applicability;
import org.omg.spec.api4kp._20200801.taxonomy.dependencyreltype.DependencyType;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassetcategory.KnowledgeAssetCategory;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeassettype.KnowledgeAssetType;
import org.omg.spec.api4kp._20200801.taxonomy.knowledgeprocessingtechnique.KnowledgeProcessingTechniqueSeries;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.Lexicon;

public class SurrogateBuilder {

  private KnowledgeAsset surrogate;

  protected SurrogateBuilder(ResourceIdentifier assetId, boolean root) {
    surrogate = newInstance(root)
        .withAssetId(assetId);
  }

  protected KnowledgeAsset newInstance(boolean root) {
    return root ? new org.omg.spec.api4kp._20200801.surrogate.resources.KnowledgeAsset() : new KnowledgeAsset();
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
        new KnowledgeArtifact()
            .withArtifactId(surrogateId)
            .withRepresentation(new SyntacticRepresentation()
                .withLanguage(Knowledge_Asset_Surrogate))
    );
    return this;
  }


  public SurrogateBuilder withName(String name, String descr) {
    surrogate.withName(name)
        .withDescription(descr);
    return this;
  }

  public SurrogateBuilder withValuesetType() {
    get().withFormalCategory(Terminology_Ontology_And_Assertional_KBs)
        .withFormalType(Value_Set);
    return this;
  }

  public SurrogateBuilder withServiceType() {
    get().withFormalCategory(Rules_Policies_And_Guidelines)
        .withFormalType(Service_Description);
    return this;
  }

  public SurrogateBuilder withCohortDefinitionType() {
    get().withFormalCategory(
        Assessment_Predictive_And_Inferential_Models)
        .withFormalType(Cohort_Definition);
    return this;
  }

  public SurrogateBuilder withQueryType() {
    get().withFormalCategory(Rules_Policies_And_Guidelines)
        .withFormalType(Inquiry_Specification);
    return this;
  }

  public SurrogateBuilder withContentType() {
    get().withFormalCategory(Terminology_Ontology_And_Assertional_KBs)
        .withFormalType(Factual_Knowledge);
    return this;
  }

  public SurrogateBuilder withExpressionType() {
    get().withFormalCategory(
        Assessment_Predictive_And_Inferential_Models)
        .withFormalType(Functional_Expression);
    return this;
  }

  public SurrogateBuilder withFormalType(KnowledgeAssetCategory cat, KnowledgeAssetType type) {
    get().withFormalCategory(cat)
        .withFormalType(type);
    return this;
  }

  public SurrogateBuilder withDecisionAidType() {
    get().withFormalCategory(
        Assessment_Predictive_And_Inferential_Models)
        .withFormalType(Computable_Decision_Model);
    return this;
  }


  public SurrogateBuilder aaS() {
    get().withProcessingMethod(KnowledgeProcessingTechniqueSeries.Computational_Technique);
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
      this.withAnnotation(SemanticAnnotationRelTypeSeries.Defines.asConceptIdentifier(),
          proposition.asConceptIdentifier());
    }

    if (subject != null) {
      this.withAnnotation(SemanticAnnotationRelTypeSeries.Has_Primary_Subject.asConceptIdentifier(),
          subject.asConceptIdentifier());
    }

    Arrays.stream(inputs).forEach(input ->
        this.withAnnotation(SemanticAnnotationRelTypeSeries.In_Terms_Of.asConceptIdentifier(),
            input.asConceptIdentifier())
    );

    return this;
  }


  public SurrogateBuilder withDMNExpression(KnowledgeRepresentationLanguage schema) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(
          new KnowledgeArtifact()
              .withRepresentation(new SyntacticRepresentation()
                  .withLanguage(DMN_1_1)
                  .withFormat(XML_1_1)
                  .withSubLanguage(new SyntacticRepresentation()
                      .withRole(Schema_Language)
                      .withLanguage(schema))));
    }
    return this;
  }


  public SurrogateBuilder withOpenAPIExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(
          new KnowledgeArtifact()
              .withRepresentation(new SyntacticRepresentation()
                  .withLanguage(OpenAPI_2_X)
                  .withFormat(YAML_1_2)));
    }
    return this;
  }

  public SurrogateBuilder withHTMLExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new KnowledgeArtifact()
          .withLocalization(English)
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(HTML)
              .withFormat(TXT)));
    }
    return this;
  }

  public SurrogateBuilder withSKOSExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new KnowledgeArtifact()
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(OWL_2)
              .withFormat(RDF_1_1)
              .withProfile(OWL2_DL)
              .withLexicon(SKOS)));
    }
    return this;
  }


  public SurrogateBuilder withCQLExpression(Lexicon lex) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new KnowledgeArtifact()
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(HL7_CQL)
              .withFormat(TXT)
              .withLexicon(lex)));
    }
    return this;
  }


  public SurrogateBuilder withInlinedFhirPath(String expr) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new KnowledgeArtifact()
          .withArtifactId(artifactId(expr != null ? Util.uuid(expr) : UUID.randomUUID(), "LATEST"))
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(FHIRPath_STU1)
              .withFormat(TXT))
          .withInlinedExpression(expr));
    }
    return this;
  }


  public SurrogateBuilder withRepresentation(KnowledgeRepresentationLanguage lang,
      SerializationFormat format) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new KnowledgeArtifact()
          .withRepresentation(new SyntacticRepresentation()
              .withLanguage(lang)
              .withFormat(format)));
    }
    return this;
  }


  public SurrogateBuilder withCarriers(ResourceIdentifier id, URI loc) {
    if (get().getCarriers().isEmpty()) {
      KnowledgeArtifact carrier = new KnowledgeArtifact()
          .withArtifactId(id)
          .withLocator(loc);
      get().withCarriers(carrier);
    } else {
      get().getCarriers().get(0)
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


  public static ResourceIdentifier assetId(UUID assetId) {
    return SemanticIdentifier.newId(Registry.MAYO_ASSETS_BASE_URI_URI, assetId);
  }

  public static ResourceIdentifier assetId(String uuid, String versionTag) {
    return SemanticIdentifier.newId(Registry.MAYO_ASSETS_BASE_URI_URI, uuid,
        VersionIdentifier.toSemVer(versionTag));
  }

  public static ResourceIdentifier assetId(UUID uuid, String versionTag) {
    return assetId(uuid.toString(),
        VersionIdentifier.toSemVer(versionTag));
  }

  public static ResourceIdentifier randomAssetId() {
    return assetId(UUID.randomUUID(),VERSION_ZERO);
  }

  public static ResourceIdentifier randomAssetId(URI baseNamespace) {
    return SemanticIdentifier.newId(baseNamespace,UUID.randomUUID(),VERSION_ZERO);
  }


  public static ResourceIdentifier artifactId(String uuid, String versionTag) {
    return SemanticIdentifier.newId(Registry.MAYO_ARTIFACTS_BASE_URI_URI, uuid,
        VersionIdentifier.toSemVer(versionTag));
  }

  public static ResourceIdentifier artifactId(UUID uuid, String versionTag) {
    return artifactId(uuid.toString(),
        VersionIdentifier.toSemVer(versionTag));
  }

  public static ResourceIdentifier randomArtifactId() {
    return artifactId(UUID.randomUUID(),VERSION_ZERO);
  }

  public static ResourceIdentifier randomArtifactId(URI baseNamespace) {
    return SemanticIdentifier.newId(baseNamespace,UUID.randomUUID(),VERSION_ZERO);
  }

}
