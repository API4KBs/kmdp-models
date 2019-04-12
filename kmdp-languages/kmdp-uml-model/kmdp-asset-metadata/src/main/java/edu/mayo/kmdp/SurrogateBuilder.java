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
import static edu.mayo.kmdp.terms.iso639_1_languagecodes._20170801.ISO639_1_LanguageCodes.English;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.metadata.annotations.SimpleAnnotation;
import edu.mayo.kmdp.metadata.surrogate.Dependency;
import edu.mayo.kmdp.metadata.surrogate.InlinedRepresentation;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeExpression;
import edu.mayo.kmdp.metadata.surrogate.Representation;
import edu.mayo.kmdp.metadata.surrogate.SubLanguage;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.terms.AssetVocabulary;
import edu.mayo.kmdp.terms.kao.knowledgeassetcategory._1_0.KnowledgeAssetCategory;
import edu.mayo.kmdp.terms.kao.knowledgeassettype._1_0.KnowledgeAssetType;
import edu.mayo.kmdp.terms.kao.knowledgeprocessingtechnique._1_0.KnowledgeProcessingTechnique;
import edu.mayo.kmdp.terms.kao.languagerole._1_0.LanguageRole;
import edu.mayo.kmdp.terms.kao.rel.dependencyreltype._20190801.DependencyRelType;
import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import edu.mayo.kmdp.terms.krprofile._2018._08.KRProfile;
import edu.mayo.kmdp.terms.lexicon._2018._08.Lexicon;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

public class SurrogateBuilder {

  private KnowledgeAsset surrogate;

  protected SurrogateBuilder(URIIdentifier assetId, boolean root) {
    surrogate = newInstance(root)
        .withResourceId(assetId);
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
    get().withCategory(KnowledgeAssetCategory.Terminology_Ontology_And_Assertional_KBs)
        .withType(KnowledgeAssetType.Value_Set);
    return this;
  }

  public SurrogateBuilder withServiceType() {
    get().withCategory(KnowledgeAssetCategory.Rules_Policies_And_Guidelines)
        .withType(KnowledgeAssetType.Service_Description);
    return this;
  }

  public SurrogateBuilder withCohortDefinitionType() {
    get().withCategory(KnowledgeAssetCategory.Assessment_Predictive_And_Inferential_Models)
        .withType(KnowledgeAssetType.Cohort_Definition);
    return this;
  }

  public SurrogateBuilder withQueryType() {
    get().withCategory(KnowledgeAssetCategory.Rules_Policies_And_Guidelines)
        .withType(KnowledgeAssetType.Inquiry_Specification);
    return this;
  }

  public SurrogateBuilder withContentType() {
    get().withCategory(KnowledgeAssetCategory.Terminology_Ontology_And_Assertional_KBs)
        .withType(KnowledgeAssetType.Factual_Knowledge);
    return this;
  }

  public SurrogateBuilder withExpressionType() {
    get().withCategory(KnowledgeAssetCategory.Assessment_Predictive_And_Inferential_Models)
        .withType(KnowledgeAssetType.Functional_Expression);
    return this;
  }

  public SurrogateBuilder withType(KnowledgeAssetCategory cat, KnowledgeAssetType... type) {
    get().withCategory(cat)
        .withType(type);
    return this;
  }

  public SurrogateBuilder withDecisionAidType() {
    get().withCategory(KnowledgeAssetCategory.Assessment_Predictive_And_Inferential_Models)
        .withType(KnowledgeAssetType.Computable_Decision_Model);
    return this;
  }


  public SurrogateBuilder aaS() {
    get().withMethod(KnowledgeProcessingTechnique.Service_Based_Technique);
    return this;
  }


  public SurrogateBuilder asOperationalDefinition(ConceptIdentifier subject, Term proposition,
      Term... inputs) {
    get().withType(KnowledgeAssetType.Operational_Concept_Defintion);

    if (proposition != null) {
      this.withAnnotation(AssetVocabulary.DEFINES.asConcept(), proposition.asConcept());
    }

    if (subject != null) {
      this.withAnnotation(AssetVocabulary.HAS_SUBJECT.asConcept(), subject);
    }

    Arrays.stream(inputs).forEach((input) ->
        this.withAnnotation(AssetVocabulary.IN_TERMS_OF.asConcept(), input.asConcept())
    );

    return this;
  }


  public SurrogateBuilder withDMNExpression(KRLanguage schema) {
    if (get().getExpression() == null) {
      get().withExpression(new KnowledgeExpression().withRepresentation(new Representation()
          .withLanguage(KRLanguage.DMN_1_1)
          .withFormat(KRFormat.XML_1_1)
          .withWith(new SubLanguage().withRole(LanguageRole.Schema_Language)
              .withSubLanguage(new Representation().withLanguage(schema)))));
    }
    return this;
  }


  public SurrogateBuilder withOpenAPIExpression() {
    if (get().getExpression() == null) {
      get().withExpression(new KnowledgeExpression().withRepresentation(new Representation()
          .withLanguage(KRLanguage.OpenAPI_2_3)
          .withFormat(KRFormat.YAML_1_2)));
    }
    return this;
  }

  public SurrogateBuilder withHTMLExpression() {
    if (get().getExpression() == null) {
      get().withExpression(new KnowledgeExpression().withLocale(English)
          .withRepresentation(new Representation()
              .withLanguage(KRLanguage.HTML)
              .withFormat(KRFormat.XML_1_1)));
    }
    return this;
  }

  public SurrogateBuilder withSKOSExpression() {
    if (get().getExpression() == null) {
      get().withExpression(new KnowledgeExpression().withRepresentation(new Representation()
          .withLanguage(KRLanguage.OWL_2)
          .withFormat(KRFormat.RDF_1_1)
          .withProfile(KRProfile.OWL_2_DL)
          .withLexicon(Lexicon.SKOS)));
    }
    return this;
  }


  public SurrogateBuilder withCQLExpression(Lexicon lex) {
    if (get().getExpression() == null) {
      get().withExpression(new KnowledgeExpression().withRepresentation(new Representation()
          .withLanguage(KRLanguage.CQL_1_2)
          .withFormat(KRFormat.TXT)
          .withLexicon(lex)));
    }
    return this;
  }

  public SurrogateBuilder withFHIRServiceProfileExpression(Lexicon vocab) {
    if (!get().getType().contains(KnowledgeAssetType.Service_Profile)) {
      get().getType().add(KnowledgeAssetType.Service_Profile);
    }
    if (get().getExpression() == null) {
      get().withExpression(new KnowledgeExpression().withRepresentation(new Representation()
          .withLanguage(KRLanguage.Service_Profile)
          .withWith(new SubLanguage()
              .withRole(LanguageRole.Expression_Language)
              .withSubLanguage(new Representation().withLanguage(KRLanguage.Mustache)))
          .withWith(new SubLanguage()
              .withRole(LanguageRole.Schema_Language)
              .withSubLanguage(new Representation().withLanguage(KRLanguage.FHIR_DSTU2)))

          .withFormat(KRFormat.XML_1_1)

          .withLexicon(getLexicons(vocab))));
    }
    return this;
  }

  private Lexicon[] getLexicons(Lexicon vocab) {
    if (vocab != null) {
      return new Lexicon[]{Lexicon.Activity_Context, vocab};
    } else {
      return new Lexicon[]{Lexicon.Activity_Context};
    }
  }

  public SurrogateBuilder withNLPServiceProfileExpression() {
    if (get().getExpression() == null) {
      get().withExpression(new KnowledgeExpression().withRepresentation(new Representation()
          .withLanguage(KRLanguage.Service_Profile)
          .withWith(new SubLanguage()
              .withRole(LanguageRole.Expression_Language)
              .withSubLanguage(new Representation().withLanguage(KRLanguage.Mustache)))

          .withFormat(KRFormat.XML_1_1)
          .withLexicon(Lexicon.Activity_Context)));
    }
    return this;
  }

  public SurrogateBuilder withInlinedFhirPath(String expr) {
    if (get().getExpression() == null) {
      get().withExpression(new KnowledgeExpression().withRepresentation(new Representation()
          .withLanguage(KRLanguage.FHIRPath_STU1)
          .withFormat(KRFormat.TXT))
          .withInlined(new InlinedRepresentation().withExpr(expr)));
    }
    return this;
  }


  public SurrogateBuilder withRepresentation(KRLanguage lang, KRFormat format) {
    if (get().getExpression() == null) {
      get().withExpression(new KnowledgeExpression().withRepresentation(new Representation()
          .withLanguage(lang)
          .withFormat(format)));
    }
    return this;
  }


  public SurrogateBuilder withCarrier(URIIdentifier id, URI loc) {
    KnowledgeArtifact carrier = new KnowledgeArtifact()
        .withResourceId(id)
        .withMasterLocation(loc);
    if (get().getExpression() == null) {
      get().setExpression(new KnowledgeExpression());
    }
    get().getExpression().withCarrier(carrier);

    return this;
  }

  public SurrogateBuilder withAnnotation(ConceptIdentifier rel, ConceptIdentifier obj) {
    get().withSubject(new SimpleAnnotation().withRel(rel).withExpr(obj));
    return this;
  }

  public SurrogateBuilder withDependency(DependencyRelType rel, URIIdentifier relatedAsset) {
    get().withRelated(
        new Dependency().withRel(rel).withTgt(new KnowledgeAsset().withResourceId(relatedAsset)));
    return this;
  }

  public SurrogateBuilder withDependency(DependencyRelType rel, KnowledgeAsset relatedAsset) {
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

  private static String construct(String id) {
    return Registry.MAYO_ASSETS_BASE_URI + id;
  }

  private static String validate(String uuid) {
    return UUID.fromString(Util.ensureUUIDFormat(uuid).get()).toString();
  }


}
