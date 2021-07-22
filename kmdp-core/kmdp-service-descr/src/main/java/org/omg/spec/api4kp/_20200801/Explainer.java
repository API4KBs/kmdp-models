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
package org.omg.spec.api4kp._20200801;

import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;

import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.Util;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;

/**
 * Specialization of the Writer monad that handles 'explanations'
 * in the context of knowledge-oriented APIs
 */
public abstract class Explainer {

  public static final String PROV_REGEXP = FileUtil
      .readStatic("/provenance.link.regexp", Explainer.class);

  private static final Pattern REGEXP_PATTERN = Pattern.compile(PROV_REGEXP);

  public static final String EXPL_HEADER = "Link";
  public static final String PROV_KEY = "http://www.w3.org/ns/prov#has_provenance";

  public static final KnowledgeCarrier DEFAULT_NO_EXPL = new KnowledgeCarrier();

  protected KnowledgeCarrier explanation;

  public static Optional<KnowledgeCarrier> extractExplanation(
      Map<String, List<String>> meta) {
    return Optional.ofNullable(meta.get(EXPL_HEADER))
        .flatMap(links -> resolveExplanation(links,meta));
  }

  public static void packExplanation(String msg, Map<String, List<String>> meta) {
    String id = UUID.randomUUID().toString();
    String key = String.format("<urn:uuid:%s>", id);
    meta.put(Explainer.EXPL_HEADER,
        Collections.singletonList(String.format("%s;rel=\"%s\";", key, Explainer.PROV_KEY)));
    meta.put(id, Collections.singletonList(msg));
  }

  protected static Optional<KnowledgeCarrier> resolveExplanation(List<String> links, Map<String, List<String>> meta) {
    if (links == null || links.size() != 1) {
      return Optional.empty();
    }
    Matcher matcher = REGEXP_PATTERN.matcher(links.get(0));
    if (!matcher.matches() || ! PROV_KEY.equals(matcher.group(2))) {
      return Optional.empty();
    }
    String explKey = matcher.group(1);
    // TODO This should resolve a Provenance URI
    return Optional.ofNullable(meta.get(explKey))
        .map(expl -> ofNaturalLanguageRep(Util.concat(expl)));
  }

  private static KnowledgeCarrier ofNaturalLanguageRep(String str) {
    return new org.omg.spec.api4kp._20200801.services.resources.KnowledgeCarrier()
        .withExpression(str)
        .withLevel(Serialized_Knowledge_Expression)
        .withRepresentation(
            // TODO ADD "Natural Language" to the list of languages
            rep(HTML,TXT));
  }

  protected void mergeExplanation(KnowledgeCarrier other) {
    //TODO This should be a "combiner" for KnowledgeCarriers, depending on the adopted representation language
    if (other != null) {
      if (this.explanation == null || this.explanation == DEFAULT_NO_EXPL) {
        this.explanation = other;
      } else {
        KnowledgeCarrier s = explanation;
        String s1 = s.asString().orElse("");
        String s2 = other.asString().orElse("");
        s.setExpression(String.join("\n", s1, s2));
      }
    }
  }

  public Explainer withExplanation(String expl) {
    addExplanation(expl);
    return this;
  }

  protected void addExplanation(String s) {
    KnowledgeCarrier x = ofNaturalLanguageRep(s);
    if (getExplanation() != null) {
      mergeExplanation(x);
    } else {
      setExplanation(x);
    }
  }

  protected void addExplanation(Map<String, List<String>> meta) {
    extractExplanation(meta)
        .ifPresent(this::setExplanation);
  }

  public KnowledgeCarrier getExplanation() {
    return explanation != null ? explanation : DEFAULT_NO_EXPL;
  }

  public String printExplanation() {
    return Optional.ofNullable(explanation)
        .flatMap(KnowledgeCarrier::asString)
        .orElse("n/a");
  }

  public void setExplanation(KnowledgeCarrier explanation) {
    this.explanation = explanation;
  }
}
