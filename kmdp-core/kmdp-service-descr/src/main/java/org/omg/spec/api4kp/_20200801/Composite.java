package org.omg.spec.api4kp._20200801;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.omg.spec.api4kp._20200801.contrastors.ParsingLevelContrastor;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.services.CompositeStructType;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Composite<T,S,K extends Composite<T,S,K>> {

  Logger logger = LoggerFactory.getLogger(Composite.class);

  List<T> getComponent();

  Composite<T,S,K> withComponent(Collection<T> comps);

  S getStruct();

  Composite<T,S,K> withStruct(S struct);

  CompositeStructType getStructType();

  Composite<T,S,K> withStructType(CompositeStructType structType);

  ResourceIdentifier getRootId();

  Composite<T,S,K> withRootId(ResourceIdentifier rootId);

  SyntacticRepresentation getRepresentation();

  Composite<T,S,K> withRepresentation(SyntacticRepresentation rep);

  Composite<T,S,K> withLevel(ParsingLevel level);


  default <U,X extends Composite<U,S,X>> Answer<X> visit(
      Function<? super T, Answer<U>> fun,
      UnaryOperator<S> mapStruct,
      UnaryOperator<SyntacticRepresentation> mapRepresentation,
      Supplier<X> constructor) {

    final Answer<List<U>> mapped = this.getComponent().stream()
        .map(fun)
        .collect(Answer.toList());

    final S mappedStruct = mapStruct.apply(getStruct());

    SyntacticRepresentation outputRep = mapRepresentation.apply(getRepresentation());

    return (Answer<X>) mapped.map(
        comps -> constructor.get()
            .withStruct(mappedStruct)
            .withStructType(this.getStructType())
            .withRootId(this.getRootId())
            .withRepresentation(outputRep)
            .withLevel(ParsingLevelContrastor.detectLevel(outputRep))
            .withComponent(comps));
  }

}
