package org.omg.spec.api4kp._20200801.id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.xml.bind.annotation.XmlTransient;

public interface Identifiable {

  @JsonIgnore
  @XmlTransient
  Identifier getIdentifier();

}
