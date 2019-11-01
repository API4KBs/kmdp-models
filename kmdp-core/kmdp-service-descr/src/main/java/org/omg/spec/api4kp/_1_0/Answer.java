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
package org.omg.spec.api4kp._1_0;


import edu.mayo.kmdp.util.StreamUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.responsecodes._2011.ResponseCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monadic class supporting KMDP API functional-style chaining
 *
 * Inherits behavior from classic monads such as:
 *
 * * Try - exception handling,
 * * Writer - explanations
 *
 * @param <T>
 */
public class Answer<T> extends Explainer {

  protected ResponseCode codedOutcome = ResponseCode.NotImplemented;
  protected OutcomeStrategy<T> handler;
  protected T value;

  protected Map<String, List<String>> meta;

  private static Logger logger = LoggerFactory.getLogger(Answer.class);

  /* Constructors (lifters) */

  public static <X> Answer<X> unsupported() {
    return failed(new UnsupportedOperationException("Not Implemented"));
  }

  public static <X> Answer<X> failedOnServer(ServerSideException t) {
    return new Answer<X>()
        .withCodedOutcome(t.getCode())
        .withMeta(t.getHeaders())
        .withValue(null)
        .withExplanation(t.getError().getMessage());
  }

  public static <X> Answer<X> failed(Throwable t) {
    return new Answer<X>()
        .withCodedOutcome(mapCode(t))
        .withMeta(new HashMap<>())
        .withValue(null)
        .withExplanation(t.getMessage());
  }

  public static <X> Answer<X> of(X value) {
    return new Answer<X>()
        .withCodedOutcome(ResponseCode.OK)
        .withMeta(new HashMap<>())
        .withValue(value)
        .withExplanation("OK : Lift " + value.toString());
  }

  public static <X> Answer<X> of(Optional<X> value) {
    return value
        .map(Answer::of)
        .orElse(new Answer<X>()
            .withCodedOutcome(ResponseCode.NotFound)
            .withMeta(new HashMap<>())
            .withValue(null)
            .withExplanation("Optional empty"));
  }

  public static <X> Answer<X> of(String responseCode, X value) {
    return of(resolveCode(responseCode),value,new HashMap<>());
  }

  public static <X> Answer<X> of(Integer responseCode, X value) {
    return of(responseCode.toString(),value,new HashMap<>());
  }

  public static <X> Answer<X> of(ResponseCode responseCode, X value) {
    return of(responseCode,value,new HashMap<>());
  }

  public static <X> Answer<X> of(String responseCode, X value, Map<String, List<String>> meta) {
    return of(resolveCode(responseCode),value,meta);
  }

  public static <X> Answer<X> of(Integer responseCode, X value, Map<String, List<String>> meta) {
    return of(responseCode.toString(),value,meta);
  }

  public static <X> Answer<X> of(ResponseCode responseCode, X value, Map<String, List<String>> meta) {
    return new Answer<X>()
        .withCodedOutcome(responseCode)
        .withMeta(meta)
        .withValue(value)
        .withExplanation(meta);
  }


  /* Binders */

  public <U> Answer<U> flatMap(Function<? super T, Answer<U>> mapper) {
    return getHandler().flatMap(this, mapper);
  }

  public <U> Answer<U> flatOpt(Function<? super T, Optional<U>> mapper) {
    return getHandler().flatOpt(this, mapper);
  }

  public <U> Answer<U> map(Function<? super T, ? extends U> mapper) {
    return getHandler().map(this, mapper);
  }

  public static <U> Answer<U> first(List<U> coll) {
    return coll.isEmpty()
        ? Answer.failed(new NoSuchElementException())
        : Answer.of(coll.iterator().next());
  }


  protected OutcomeStrategy<T> getHandler() {
    if (handler == null) {
      handler = selectHandler(getCodedOutcome());
    }
    return handler;
  }


  /* Common Accessors */


  public Answer<T> filter(Predicate<T> filter) {
    Objects.requireNonNull(filter);
    if (filter.test(value)) {
      return this;
    } else {
      //TODO - an 'explainable' predicate would be needed, to provide meaningful information about what did not qualify
      return failed(new NoSuchElementException(
          "Value " + value + " did not meet criteria " /*+ filter.toString() */));
    }
  }

  public Optional<T> getOptionalValue() {
    return Optional.ofNullable(getValue());
  }

  public T orElse(T alt) {
    return getOptionalValue().orElse(alt);
  }

  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (value != null) {
      return value;
    } else {
      throw exceptionSupplier.get();
    }
  }

  public static <T> Stream<T> trimStream(Answer<T> ans) {
    if (ans == null) {
      return Stream.empty();
    }
    return StreamUtil.trimStream(ans.getOptionalValue());
  }


  public ResponseCode getOutcomeType() {
    return codedOutcome;
  }

  public boolean isInfo() {
    return codedOutcome.getTag().startsWith("1");
  }

  public boolean isSuccess() {
    return codedOutcome.getTag().startsWith("2");
  }

  public boolean isPartial() {
    return codedOutcome.getTag().startsWith("3");
  }

  public boolean isClientFailure() {
    return codedOutcome.getTag().startsWith("4");
  }

  public boolean isServerFailure() {
    return codedOutcome.getTag().startsWith("5");
  }

  public boolean isFailure() {
    return isClientFailure() || isServerFailure();
  }

  public Optional<String> getMeta(String key) {
    return Optional.ofNullable(meta.get(key))
        .map(Util::concat);
  }

  public List<String> getMetas(String key) {
    return meta.containsKey(key)
        ? meta.get(key)
        : Collections.emptyList();
  }

  public Collection<String> listMeta() {
    return meta.keySet();
  }


  /* Internal utilities */

  protected static ResponseCode resolveCode(String responseCode) {
    return ResponseCode.resolve(responseCode)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to resolve unexpected HTTP status code " + responseCode));
  }


  /* Setters and Getters */

  protected Answer<T> withAddedExplanation(KnowledgeCarrier addExplanation) {
    mergeExplanation(addExplanation);
    return this;
  }

  protected Answer<T> withExplanation(KnowledgeCarrier expl) {
    setExplanation(expl);
    return this;
  }

  @Override
  public Answer<T> withExplanation(String msg) {
    super.addExplanation(msg);

    String key = "urn:uuid:" + UUID.randomUUID().toString();
    this.getMeta().put(Explainer.EXPL_HEADER, Arrays
        .asList("<" + key + ">;rel=\"" + Explainer.PROV_KEY+ "\";"));
    this.getMeta().put(key, Arrays.asList(msg));

    return this;
  }

  protected Answer<T> withExplanation(Map<String, List<String>> meta) {
    super.addExplanation(meta);
    return this;
  }


  protected Answer<T> withAddedMeta(Map<String, List<String>> additionalMeta) {
    if (this.meta == null) {
      this.meta = new HashMap<>(additionalMeta);
    }
    additionalMeta.forEach((k, v) -> {
      if (!this.meta.containsKey(k)) {
        this.meta.put(k, new ArrayList<>(v));
      } else {
        if (! EXPL_HEADER.equals(k)) {
          this.meta.get(k).addAll(additionalMeta.get(k));
        }
      }
    });
    return this;
  }

  protected Answer<T> withMeta(Map<String, List<String>> meta) {
    setMeta(new HashMap<>(meta));
    return this;
  }

  protected Answer<T> withValue(T value) {
    setValue(value);
    return this;
  }

  protected Answer<T> withCodedOutcome(ResponseCode code) {
    setCodedOutcome(code);
    this.handler = selectHandler(code);
    return this;
  }

  protected ResponseCode getCodedOutcome() {
    return codedOutcome;
  }

  protected void setCodedOutcome(ResponseCode codedOutcome) {
    this.codedOutcome = codedOutcome;
  }

  protected T getValue() {
    return value;
  }

  protected void setValue(T value) {
    this.value = value;
  }

  protected Map<String, List<String>> getMeta() {
    return meta;
  }

  protected void setMeta(Map<String, List<String>> meta) {
    this.meta = new HashMap<>(meta);
  }


  /* Maps exceptions to status codes */
  protected static ResponseCode mapCode(Throwable t) {
    if (t instanceof UnsupportedOperationException) {
      return ResponseCode.NotImplemented;
    }
    return ResponseCode.InternalServerError;
  }

  protected OutcomeStrategy<T> selectHandler(ResponseCode code) {
    if (isSuccess()) {
      return SuccessOutcomeStrategy.getInstance();
    } else if (Integer.valueOf(code.getTag()) >= 300) {
      return FailureOutcomeStrategy.getInstance();
    } else {
      return SuccessOutcomeStrategy.getInstance();
    }
  }


  public interface OutcomeStrategy<T> {

    <U> Answer<U> map(Answer<T> tAnswer, Function<? super T, ? extends U> mapper);

    <U> Answer<U> flatMap(Answer<T> tAnswer, Function<? super T, Answer<U>> mapper);

    <U> Answer<U> flatOpt(Answer<T> tAnswer, Function<? super T, Optional<U>> mapper);

  }

  public static class SuccessOutcomeStrategy<T> implements OutcomeStrategy<T> {

    protected static final SuccessOutcomeStrategy<?> instance = new SuccessOutcomeStrategy<>();

    public static <T> SuccessOutcomeStrategy<T> getInstance() {
      return (SuccessOutcomeStrategy<T>) instance;
    }

    @Override
    public <U> Answer<U> map(Answer<T> srcAnswer, Function<? super T, ? extends U> mapper) {
      try {
        return new Answer<U>()
            .withValue(mapper.apply(srcAnswer.value))
            .withCodedOutcome(srcAnswer.getCodedOutcome())
            .withExplanation(srcAnswer.explanation)
            .withMeta(srcAnswer.meta);
      } catch (Exception e) {
        return Answer.<U>failed(e)
            .withAddedMeta(srcAnswer.meta)
            .withAddedExplanation(srcAnswer.explanation);
      }
    }

    @Override
    public <U> Answer<U> flatMap(Answer<T> srcAnswer, Function<? super T, Answer<U>> mapper) {
      try {
        return mapper.apply(srcAnswer.value)
            .withAddedMeta(srcAnswer.meta)
            .withAddedExplanation(srcAnswer.explanation);
      } catch (Exception e) {
        logger.error(e.getMessage(),e);
        return Answer.<U>failed(e)
            .withAddedMeta(srcAnswer.meta)
            .withAddedExplanation(srcAnswer.explanation);
      }
    }

    @Override
    public <U> Answer<U> flatOpt(Answer<T> srcAnswer, Function<? super T, Optional<U>> mapper) {
      try {
        return Answer.of(mapper.apply(srcAnswer.value))
            .withAddedMeta(srcAnswer.meta)
            .withAddedExplanation(srcAnswer.explanation);
      } catch (Exception e) {
        logger.error(e.getMessage(),e);
        return Answer.<U>failed(e)
            .withAddedMeta(srcAnswer.meta)
            .withAddedExplanation(srcAnswer.explanation);
      }
    }
  }

  public static class FailureOutcomeStrategy<T> implements OutcomeStrategy<T> {

    protected static final FailureOutcomeStrategy<?> instance = new FailureOutcomeStrategy<>();

    public static <T> FailureOutcomeStrategy<T> getInstance() {
      return (FailureOutcomeStrategy<T>) instance;
    }

    @Override
    public <U> Answer<U> map(Answer<T> tAnswer, Function<? super T, ? extends U> mapper) {
      return new Answer<U>()
          .withCodedOutcome(tAnswer.getCodedOutcome())
          .withExplanation(tAnswer.explanation)
          .withMeta(tAnswer.meta);
    }

    @Override
    public <U> Answer<U> flatMap(Answer<T> tAnswer, Function<? super T, Answer<U>> mapper) {
      return new Answer<U>()
          .withCodedOutcome(tAnswer.getCodedOutcome())
          .withExplanation(tAnswer.explanation)
          .withMeta(tAnswer.meta);
    }

    @Override
    public <U> Answer<U> flatOpt(Answer<T> tAnswer, Function<? super T, Optional<U>> mapper) {
      return new Answer<U>()
          .withCodedOutcome(tAnswer.getCodedOutcome())
          .withExplanation(tAnswer.explanation)
          .withMeta(tAnswer.meta);
    }
  }

}
