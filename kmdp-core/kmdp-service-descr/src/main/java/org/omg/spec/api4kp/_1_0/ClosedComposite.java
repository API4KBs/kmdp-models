package org.omg.spec.api4kp._1_0;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ClosedComposite<T, K extends ClosedComposite<T,K>> extends Composite<T,K> {

  Logger logger = LoggerFactory.getLogger(ClosedComposite.class);

  default Answer<T> visit(Function<? super T, Answer<T>> fun) {
    return (Answer<T>) visit(fun,
        t -> t,
        () -> (K) createNewInstance());
  }

  Object createNewInstance();

}
