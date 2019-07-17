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
import static edu.mayo.ontology.taxonomies.iso639_2_languagecodes._20190201.Language.English;
import static edu.mayo.ontology.taxonomies.kao.knowledgeassetrole._20190801.KnowledgeAssetRole.Operational_Concept_Definition;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.surrogate.ComputableKnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.Dependency;
import edu.mayo.kmdp.metadata.surrogate.InlinedRepresentation;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.metadata.surrogate.SubLanguage;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.kao.knowledgeassetcategory._20190801.KnowledgeAssetCategory;
import edu.mayo.ontology.taxonomies.kao.knowledgeassettype._20190801.KnowledgeAssetType;
import edu.mayo.ontology.taxonomies.kao.knowledgeprocessingtechnique._20190801.KnowledgeProcessingTechnique;
import edu.mayo.ontology.taxonomies.kao.languagerole._20190801.KnowledgeRepresentationLanguageRole;
import edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype._20190801.DependencyType;
import edu.mayo.ontology.taxonomies.kmdo.annotationreltype._20190801.AnnotationRelType;
import edu.mayo.ontology.taxonomies.krformat._20190801.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._20190801.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krprofile._20190801.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.lexicon._20190801.Lexicon;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

public class SurrogateBuilder {

  private KnowledgeAsset surrogate;

  protected SurrogateBuilder(URIIdentifier assetId, boolean root) {
    surrogate = newInstance(root)
        .withAssetId(assetId);
  }

  protected KnowledgeAsset newInstance(boolean root) {
    return root ? new edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset() : new KnowledgeAsset();
  }

  public static SurrogateBuilder newSurrogate(URIIdentifier assetId) {
    return newSurrogate(assetId, true);
  }

  public static SurrogateBuilder newSurrogate(URIIdentifier assetId, boolean root) {
    return new SurrogateBuilder(assetId, root);
  }

  public SurrogateBuilder withName(String name, String descr) {
    surrogate.withName(name)
        .withDescription(descr);
    return this;
  }

  public SurrogateBuilder withValuesetType() {
    get().withFormalCategory(KnowledgeAssetCategory.Terminology_Ontology_And_Assertional_KBs)
        .withFormalType(KnowledgeAssetType.Value_Set);
    return this;
  }

  public SurrogateBuilder withServiceType() {
    get().withFormalCategory(KnowledgeAssetCategory.Rules_Policies_And_Guidelines)
        .withFormalType(KnowledgeAssetType.Service_Description);
    return this;
  }

  public SurrogateBuilder withCohortDefinitionType() {
    get().withFormalCategory(KnowledgeAssetCategory.Assessment_Predictive_And_Inferential_Models)
        .withFormalType(KnowledgeAssetType.Cohort_Definition);
    return this;
  }

  public SurrogateBuilder withQueryType() {
    get().withFormalCategory(KnowledgeAssetCategory.Rules_Policies_And_Guidelines)
        .withFormalType(KnowledgeAssetType.Inquiry_Specification);
    return this;
  }

  public SurrogateBuilder withContentType() {
    get().withFormalCategory(KnowledgeAssetCategory.Terminology_Ontology_And_Assertional_KBs)
        .withFormalType(KnowledgeAssetType.Factual_Knowledge);
    return this;
  }

  public SurrogateBuilder withExpressionType() {
    get().withFormalCategory(KnowledgeAssetCategory.Assessment_Predictive_And_Inferential_Models)
        .withFormalType(KnowledgeAssetType.Functional_Expression);
    return this;
  }

  public SurrogateBuilder withFormalType(KnowledgeAssetCategory cat, KnowledgeAssetType... type) {
    get().withFormalCategory(cat)
        .withFormalType(type);
    return this;
  }

  public SurrogateBuilder withDecisionAidType() {
    get().withFormalCategory(KnowledgeAssetCategory.Assessment_Predictive_And_Inferential_Models)
        .withFormalType(KnowledgeAssetType.Computable_Decision_Model);
    return this;
  }


  public SurrogateBuilder aaS() {
    get().withProcessingMethod(KnowledgeProcessingTechnique.Service_Based_Technique);
    return this;
  }


  public SurrogateBuilder asOperationalDefinition(ConceptIdentifier subject, Term proposition,
      Term... inputs) {
    get().withRole(Operational_Concept_Definition);

    if (proposition != null) {
      this.withAnnotation(AnnotationRelType.Defines.asConcept(), proposition.asConcept());
    }

    if (subject != null) {
      this.withAnnotation(AnnotationRelType.Has_Subject.asConcept(), subject);
    }

    Arrays.stream(inputs).forEach((input) ->
        this.withAnnotation(AnnotationRelType.In_Terms_Of.asConcept(), input.asConcept())
    );

    return this;
  }


  public SurrogateBuilder withDMNExpression(KnowledgeRepresentationLanguage schema) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact().withRepresentation(new Representation()
          .withLanguage(KnowledgeRepresentationLanguage.DMN_1_1)
          .withFormat(SerializationFormat.XML_1_1)
          .withWith(new SubLanguage().withRole(KnowledgeRepresentationLanguageRole.Schema_Language)
              .withSubLanguage(new Representation().withLanguage(schema)))));
    }
    return this;
  }


  public SurrogateBuilder withOpenAPIExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact().withRepresentation(new Representation()
          .withLanguage(KnowledgeRepresentationLanguage.OpenAPI_2_X)
          .withFormat(SerializationFormat.YAML_1_2)));
    }
    return this;
  }

  public SurrogateBuilder withHTMLExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact().withLocalization(English)
          .withRepresentation(new Representation()
              .withLanguage(KnowledgeRepresentationLanguage.HTML)
              .withFormat(SerializationFormat.XML_1_1)));
    }
    return this;
  }

  public SurrogateBuilder withSKOSExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact().withRepresentation(new Representation()
          .withLanguage(KnowledgeRepresentationLanguage.OWL_2)
          .withFormat(SerializationFormat.RDF_1_1)
          .withProfile(KnowledgeRepresentationLanguageProfile.OWL2_DL)
          .withLexicon(Lexicon.SKOS)));
    }
    return this;
  }


  public SurrogateBuilder withCQLExpression(Lexicon lex) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact().withRepresentation(new Representation()
          .withLanguage(KnowledgeRepresentationLanguage.HL7_CQL)
          .withFormat(SerializationFormat.TXT)
          .withLexicon(lex)));
    }
    return this;
  }

  public SurrogateBuilder withFHIRServiceProfileExpression(Lexicon vocab) {
    if (!get().getFormalType().contains(KnowledgeAssetType.Service_Profile)) {
      get().getFormalType().add(KnowledgeAssetType.Service_Profile);
    }
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact().withRepresentation(new Representation()
          .withLanguage(KnowledgeRepresentationLanguage.Service_Profile)
          .withWith(new SubLanguage()
              .withRole(KnowledgeRepresentationLanguageRole.Expression_Language)
              .withSubLanguage(
                  new Representation().withLanguage(KnowledgeRepresentationLanguage.Mustache)))
          .withWith(new SubLanguage()
              .withRole(KnowledgeRepresentationLanguageRole.Schema_Language)
              .withSubLanguage(
                  new Representation().withLanguage(KnowledgeRepresentationLanguage.FHIR_DSTU2)))

          .withFormat(SerializationFormat.XML_1_1)

          .withLexicon(getLexicons(vocab))));
    }
    return this;
  }

  private Lexicon[] getLexicons(Lexicon vocab) {
    if (vocab != null) {
      return new Lexicon[]{Lexicon.PCV, vocab};
    } else {
      return new Lexicon[]{Lexicon.PCV};
    }
  }

  public SurrogateBuilder withNLPServiceProfileExpression() {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact().withRepresentation(new Representation()
          .withLanguage(KnowledgeRepresentationLanguage.Service_Profile)
          .withWith(new SubLanguage()
              .withRole(KnowledgeRepresentationLanguageRole.Expression_Language)
              .withSubLanguage(
                  new Representation().withLanguage(KnowledgeRepresentationLanguage.Mustache)))

          .withFormat(SerializationFormat.XML_1_1)
          .withLexicon(Lexicon.PCV)));
    }
    return this;
  }

  public SurrogateBuilder withInlinedFhirPath(String expr) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact().withRepresentation(new Representation()
          .withLanguage(KnowledgeRepresentationLanguage.FHIRPath_STU1)
          .withFormat(SerializationFormat.TXT))
          .withInlined(new InlinedRepresentation().withExpr(expr)));
    }
    return this;
  }


  public SurrogateBuilder withRepresentation(KnowledgeRepresentationLanguage lang,
      SerializationFormat format) {
    if (get().getCarriers().isEmpty()) {
      get().withCarriers(new ComputableKnowledgeArtifact().withRepresentation(new Representation()
          .withLanguage(lang)
          .withFormat(format)));
    }
    return this;
  }


  public SurrogateBuilder withCarriers(URIIdentifier id, URI loc) {
    if (get().getCarriers().isEmpty()) {
      KnowledgeArtifact carrier = new ComputableKnowledgeArtifact()
          .withArtifactId(id)
          .withLocator(loc);
      get().withCarriers(carrier);
    } else {
      ((KnowledgeArtifact) get().getCarriers().get(0))
          .withArtifactId(id)
          .withLocator(loc);
    }

    return this;
  }

  public SurrogateBuilder withAnnotation(ConceptIdentifier rel, ConceptIdentifier obj) {
    get().withSubject(new SimpleAnnotation().withRel(rel).withExpr(obj));
    return this;
  }

  public SurrogateBuilder withDependency(DependencyType rel, URIIdentifier relatedAsset) {
    get().withRelated(
        new Dependency().withRel(rel).withTgt(new KnowledgeAsset().withAssetId(relatedAsset)));
    return this;
  }

  public SurrogateBuilder withDependency(DependencyType rel, KnowledgeAsset relatedAsset) {
    get().withRelated(new Dependency().withRel(rel).withTgt(relatedAsset));
    return this;
  }


  public KnowledgeAsset get() {
    return surrogate;
  }

  public static URIIdentifier id(String uuid, String versionTag) {
    return uri(construct(validate(uuid)),
        versionTag);
  }
  public static URIIdentifier id(UUID uuid, String versionTag) {
    return uri(construct(uuid.toString()),
        versionTag);
  }

  private static String construct(String id) {
    return Registry.MAYO_ASSETS_BASE_URI + id;
  }

  private static String validate(String uuid) {
    return UUID.fromString(Util.ensureUUIDFormat(uuid).get()).toString();
  }


}
