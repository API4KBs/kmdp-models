package org.omg.spec.api4kp._1_0;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Composite<T,K extends Composite<T,K>> {

  Logger logger = LoggerFactory.getLogger(Composite.class);

  List<T> getComponent();

  Composite<T,K> withComponent(Collection<T> comps);

  T getStruct();

  Composite<T,K> withStruct(T struct);

  default <U,X extends Composite<U,X>> Answer<X> visit(
      Function<? super T, Answer<U>> fun,
      Function<T,U> mapStruct,
      Supplier<X> constructor) {

    final Answer<List<U>> mapped = this.getComponent().stream()
        .map(fun)
        .collect(Answer.toList());

    final U mappedStruct = mapStruct.apply(getStruct());

    return (Answer<X>) mapped.map(
        comps -> constructor.get()
            .withStruct(mappedStruct)
            .withComponent(comps));
  }

}
