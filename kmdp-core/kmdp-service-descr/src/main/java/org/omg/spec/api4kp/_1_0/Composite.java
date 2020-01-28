package org.omg.spec.api4kp._1_0;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Composite<T,S,K extends Composite<T,S,K>> {

  Logger logger = LoggerFactory.getLogger(Composite.class);

  List<T> getComponent();

  Composite<T,S,K> withComponent(Collection<T> comps);

  S getStruct();

  Composite<T,S,K> withStruct(S struct);

  default <U,X extends Composite<U,S,X>> Answer<X> visit(
      Function<? super T, Answer<U>> fun,
      UnaryOperator<S> mapStruct,
      Supplier<X> constructor) {

    final Answer<List<U>> mapped = this.getComponent().stream()
        .map(fun)
        .collect(Answer.toList());

    final S mappedStruct = mapStruct.apply(getStruct());

    return (Answer<X>) mapped.map(
        comps -> constructor.get()
            .withStruct(mappedStruct)
            .withComponent(comps));
  }

}
