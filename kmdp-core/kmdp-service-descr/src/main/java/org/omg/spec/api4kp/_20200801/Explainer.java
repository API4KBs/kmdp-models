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
package org.omg.spec.api4kp._20200801;

import static edu.mayo.kmdp.util.JSonUtil.writeJsonAsString;
import static edu.mayo.kmdp.util.JSonUtil.writeXMLAsString;
import static java.util.Collections.singletonList;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.emptyCarrier;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Encoded_Knowledge_Expression;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Serialized_Knowledge_Expression;

import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCode;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.omg.spec.api4kp._20200801.services.CompositeKnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormat;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.Status;

/**
 * Specialization of the Writer monad that handles 'explanations' in the context of
 * knowledge-oriented APIs
 */
public abstract class Explainer {

  public static final URI GENERIC_ERROR_TYPE
      = URI.create("api4kp-kp:Error");
  public static final URI GENERIC_INFO_TYPE
      = URI.create("api4kp-ops:Explanation");

  public static final String EXPL_LINK_REGEXP =
      "<(.+)>;\\W*rel=\"(.+)\";(\\W*anchor=\"(.+)\")?";

  private static final Pattern REGEXP_PATTERN = Pattern.compile(EXPL_LINK_REGEXP);

  public static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

  public static final String EXPL_LINK_HEADER = "Link";
  public static final String EXPL_HEADER = "X-Explanation";
  public static final String EXPL_KEY = "https://www.omg.org/spec/API4KP/api4kp-ops/Explanation";

  // TODO add model/nl to the ontology
  private static final SyntacticRepresentation NL_MIME = rep(HTML, TXT);
  // TODO add model/problem to the ontology
  private static final SyntacticRepresentation PROBLEM_MIME = rep(HTML, JSON);

  protected static final ProblemModule pm =
      new ProblemModule().withStackTraces();

  protected KnowledgeCarrier explanation;


  public static Optional<KnowledgeCarrier> extractExplanationFromHeaders(
      Map<String, List<String>> meta) {
    List<String> links = Optional.ofNullable(meta.get(EXPL_LINK_HEADER))
        .orElseGet(Collections::emptyList);

    if (links.size() != 1) {
      return Optional.empty();
    }
    Matcher matcher = REGEXP_PATTERN.matcher(links.get(0));
    if (!matcher.matches() || !EXPL_KEY.equals(matcher.group(2))) {
      return Optional.empty();
    }

    String explKey = matcher.group(1);
    return Optional.ofNullable(meta.get(explKey))
        .map(xpls -> xpls.get(0))
        .filter(Util::isNotEmpty)
        .flatMap(expl -> JSonUtil.parseJson(expl, pm).stream()
            .flatMap(StreamUtil.filterAs(Problem.class))
            .map(Explainer::ofProblem)
            .findFirst()
            .or(() -> Optional.of(ofNaturalLanguageRep(expl))));
  }

  /**
   * Adds "Explanation" metadata to a response header, so that the body can be used for the actual
   * response data. Necessary to preserve type safety when the datatypes are different.
   * <p>
   * Errors, Exceptions and Problems are considered explanations - of failures
   * <p>
   * If the explanation is in object form, it will be serialized as JSON. If it is already in String
   * or binary format, it will be serialized as a String (more formats will be supported in the
   * future).
   * <p>
   * If the server wants to communicate an explanation, but cannot embed it in the header, it should
   * provide a callback URL, which will be embedded in a LINK header with type 'explanation'
   *
   * @param ans  the response to an API operation call
   * @param meta the HTTP headers used to communicate the response on the web
   */
  public static void packExplanationIntoHeaders(Answer<?> ans, Map<String, List<String>> meta) {
    KnowledgeCarrier expl = ans.getExplanation();
    if (expl == null) {
      return;
    }
    if (expl.getExpression() != null) {
      String msg = expl.getLevel().sameAs(Abstract_Knowledge_Expression)
          ? ans.printExplanation(JSON)
          : ans.printExplanation(TXT);
      meta.put(Explainer.EXPL_HEADER, singletonList(msg));
    } else if (expl.getHref() != null) {
      meta.put(Explainer.EXPL_LINK_HEADER,
          singletonList(String.format("<%s>;rel=\"%s\";", expl.getHref(), Explainer.EXPL_KEY)));
    }
  }


  public static KnowledgeCarrier ofNaturalLanguageRep(String str) {
    // don't use AbstractCarrier.of(), which will add an unnecessary asset ID
    return new KnowledgeCarrier()
        .withExpression(str)
        .withLevel(Serialized_Knowledge_Expression)
        .withRepresentation(NL_MIME);
  }

  public static KnowledgeCarrier ofProblem(Problem issue) {
    // don't use AbstractCarrier.of(), which will add an unnecessary asset ID
    return new KnowledgeCarrier()
        .withExpression(issue)
        .withLevel(Abstract_Knowledge_Expression)
        .withRepresentation(PROBLEM_MIME);
  }

  public static ProblemBuilder newProblem() {
    return Problem.builder()
        .withType(GENERIC_ERROR_TYPE)
        .withStatus(Status.INTERNAL_SERVER_ERROR);
  }

  public static KnowledgeCarrier ofThrowable(Throwable cause) {
    if (cause instanceof Problem) {
      return ofProblem((Problem) cause);
    } else {
      return ofProblem(new ServerSideException(cause));
    }
  }

  protected void mergeExplanation(KnowledgeCarrier other) {
    if (explanation == null) {
      this.explanation = other;
    } else if (other != null) {
      this.explanation = mergeInto(this.explanation, other);
    }
  }

  private KnowledgeCarrier mergeInto(KnowledgeCarrier intoExplanation, KnowledgeCarrier other) {
    // both intoExplanation and other are not-null
    Stream<KnowledgeCarrier> intoExpl = intoExplanation.components();
    Stream<KnowledgeCarrier> moreExpl = other.components();
    return new ExplanationCarrier()
        .withComponent(Stream.concat(intoExpl, moreExpl).collect(Collectors.toList()));
  }


  public KnowledgeCarrier getExplanation() {
    return explanation != null ? explanation : emptyCarrier();
  }

  public void setExplanation(KnowledgeCarrier explanation) {
    if (isNotEmpty(explanation)) {
      this.explanation = explanation;
    }
  }

  public void setFormalExplanation(KnowledgeCarrier explanation) {
    setExplanation(explanation);
  }

  protected boolean isNotEmpty(KnowledgeCarrier expl) {
    return expl != null
        && expl.getExpression() != null
        || expl instanceof CompositeKnowledgeCarrier;
  }

  public void setExplanationMessage(String msg) {
    setExplanation(ofNaturalLanguageRep(msg));
  }

  public void setExplanationDetail(Problem issue) {
    setExplanation(ofProblem(issue));
  }

  public void setExplanationInterrupt(Throwable cause) {
    setExplanation(ofThrowable(cause));
  }

  public Explainer withFormalExplanation(KnowledgeCarrier expl) {
    setFormalExplanation(expl);
    return this;
  }

  public Explainer withExplanationMessage(String msg) {
    setExplanationMessage(msg);
    return this;
  }

  public Explainer withExplanationDetail(Problem issue) {
    setExplanationDetail(issue);
    return this;
  }

  public Explainer withExplanationInterrupt(Throwable cause) {
    setExplanationInterrupt(cause);
    return this;
  }


  protected void addFormalExplanation(KnowledgeCarrier expl) {
    mergeExplanation(expl);
  }

  protected void addExplanationMessage(String msg) {
    addFormalExplanation(ofNaturalLanguageRep(msg));
  }

  protected void addExplanationDetail(Problem issue) {
    addFormalExplanation(ofProblem(issue));
  }

  protected void addExplanationDetail(Throwable cause) {
    addFormalExplanation(ofThrowable(cause));
  }

  public String printExplanation() {
    return printExplanation(TXT);
  }

  public String printExplanationAsJson() {
    return printExplanation(JSON);
  }

  public String printExplanation(SerializationFormat fmt) {
    boolean isXML = XML_1_1.sameAs(fmt);
    boolean isJson = JSON.sameAs(fmt);
    boolean isTxt = TXT.sameAs(fmt)
        || Serialized_Knowledge_Expression.sameAs(getExplanation().getLevel())
        || Encoded_Knowledge_Expression.sameAs(getExplanation().getLevel());

    Optional<KnowledgeCarrier> kce = Optional.ofNullable(getExplanation())
        .map(x -> flatten(x, fmt));

    if (isTxt) {
      return kce
          .flatMap(KnowledgeCarrier::asString)
          .orElse("");
    } else if (isJson) {
      return kce.map(KnowledgeCarrier::getExpression)
          .flatMap(x -> writeJsonAsString(x, pm))
          .orElse("");
    } else if (isXML) {
      return kce.map(KnowledgeCarrier::getExpression)
          .flatMap(x -> writeXMLAsString(x, pm))
          .orElse("");
    } else {
      throw new UnsupportedOperationException();
    }
  }

  protected KnowledgeCarrier flatten(KnowledgeCarrier kc, SerializationFormat fmt) {
    if (kc instanceof CompositeKnowledgeCarrier) {
      if (!TXT.sameAs(fmt)
          && kc.components().anyMatch(c -> c.getExpression() instanceof Problem)) {
        Problem complex = flattenAsProblem(kc);
        return ofProblem(complex);
      } else {
        return ofNaturalLanguageRep(flattenAsString(kc));
      }
    } else {
      return kc;
    }
  }

  protected Problem flattenAsProblem(KnowledgeCarrier kc) {
    if (kc instanceof CompositeKnowledgeCarrier) {
      ComplexProblem complex = new ComplexProblem();
      kc.components()
          .map(this::flattenAsProblem)
          .forEach(complex::add);
      return complex;
    } else {
      return kc.as(Problem.class)
          .orElseGet(() -> new InfoProblem(kc.asString().orElseThrow()));
    }
  }

  protected String flattenAsString(KnowledgeCarrier kc) {
    return kc.components()
        .flatMap(c -> c.asString().stream())
        .collect(Collectors.joining("\n"));
  }


  @SuppressWarnings("java:S110")
  public static class ComplexProblem extends AbstractThrowableProblem {

    private static final String KEY = "components";

    public ComplexProblem(String title, String detail, URI instance) {
      super(GENERIC_INFO_TYPE, title, Status.OK, detail, instance, null,
          Map.of(KEY, new ArrayList<>()));
      this.setStackTrace(Explainer.EMPTY_STACK_TRACE);
    }

    public ComplexProblem() {
      this(null, null, null);
    }

    public ComplexProblem(Collection<? extends Problem> problems) {
      this(null, null, null);
      problems.forEach(this::add);
    }

    public void add(Problem component) {
      List<Object> list = (List<Object>) this.getParameters().get(KEY);
      list.add(component);
    }
  }

  @SuppressWarnings("java:S110")
  public static class InfoProblem extends AbstractThrowableProblem {

    public InfoProblem(String msg) {
      this(null, msg, null);
    }

    public InfoProblem(String title, String detail, URI instance) {
      super(GENERIC_INFO_TYPE, title, Status.OK, detail, instance);
      this.setStackTrace(Explainer.EMPTY_STACK_TRACE);
    }
  }

  @SuppressWarnings("java:S110")
  public static class IssueProblem extends AbstractThrowableProblem {

    public IssueProblem(String title, ResponseCode status, String detail, URI instance) {
      super(GENERIC_ERROR_TYPE,
          title,
          Status.valueOf(Integer.parseInt(status.getTag())),
          detail,
          instance);
      this.setStackTrace(Explainer.EMPTY_STACK_TRACE);
    }
  }

  protected static class ExplanationCarrier extends CompositeKnowledgeCarrier {
    // marker class
  }
}
