package org.omg.spec.api4kp._20200801;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A composite such that maintains the type of frame (K) and structure (S) as functions are applied.
 * I.e. such that f(K(S,T)) = K(S,f(T))
 *
 * @param <T> the type of the components
 * @param <S> the type of the struct
 * @param <K> the type of the composite
 */
public interface ClosedComposite<T,S, K extends ClosedComposite<T,S,K>> extends Composite<T,S,K> {

  Logger logger = LoggerFactory.getLogger(ClosedComposite.class);

  default Answer<T> visit(Function<? super T, Answer<T>> fun) {
    return (Answer<T>) visit(
        // apply f
        fun,
        // preserve S (immutable)
        t -> t,
        // clone K to hold the transformed components
        () -> (K) createNewInstance());
  }

  Object createNewInstance();

}
