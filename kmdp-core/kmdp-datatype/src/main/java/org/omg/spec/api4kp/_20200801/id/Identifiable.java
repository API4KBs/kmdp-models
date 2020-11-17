package org.omg.spec.api4kp._20200801.id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import javax.xml.bind.annotation.XmlTransient;

public interface Identifiable {

  @JsonIgnore
  @XmlTransient
  Identifier getIdentifier();

  /**
   * Compares this Identifiable to a collection of Identifiables,
   * and returns true if any one of the members of the collection has the same Identifier as this
   * @param others
   * @return
   */
  default <T extends Identifiable> boolean isAnyOf(Collection<T> others) {
    return others != null && others.stream()
        .anyMatch(
            o -> getIdentifier().getResourceId().equals(o.getIdentifier().getResourceId()));
  }

  /**
   * Compares this Identifiable to a collection of Identifiables,
   * and returns true if none of the members of the collection has the same Identifier as this
   * @param others
   * @return
   */
  default <T extends Identifiable>  boolean isNoneOf(Collection<T> others) {
    return others != null && others.stream()
        .noneMatch(
            o -> getIdentifier().getResourceId().equals(o.getIdentifier().getResourceId()));
  }


}
