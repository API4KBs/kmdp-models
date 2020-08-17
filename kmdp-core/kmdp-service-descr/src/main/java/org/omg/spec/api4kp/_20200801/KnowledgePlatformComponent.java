package org.omg.spec.api4kp._20200801;

import edu.mayo.kmdp.util.StreamUtil;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;

public interface KnowledgePlatformComponent<T extends org.omg.spec.api4kp._20200801.services.KnowledgePlatformComponent> {

  UUID getComponentUuid();

  T toKPComponent(ResourceIdentifier id);

  T getDescriptor();

  default ResourceIdentifier getComponentId() {
    return SemanticIdentifier.newId(getComponentUuid());
  }

  /**
   * Filters a collection of Operators to return only the
   * ones that actually implement a given operation
   *
   * @param operators The list of Operators
   * @param test The filter that determines whether an operator implements an operation
   * @param cast A cast function
   * @param <X> The Operator Type
   * @param <O> The Operation Type
   * @return A list of Operators, cast as Operations
   */
  default <X,O> List<O> getOperations(
      Collection<X> operators,
      Predicate<X> test,
      Function<X, Optional<O>> cast) {
    return operators.stream()
        .filter(test)
        .map(cast)
        .flatMap(StreamUtil::trimStream)
        .collect(Collectors.toList());
  }

}
