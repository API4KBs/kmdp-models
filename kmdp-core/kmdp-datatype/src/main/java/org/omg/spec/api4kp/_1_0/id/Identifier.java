package org.omg.spec.api4kp._1_0.id;

import java.net.URI;
import java.util.Date;

public interface Identifier {
  String getTag();

  // TODO: what is URI for formats?
  // TODO: ask Davide how to make Enums in UML Designer
  default URI getFormat() {

    return null;
  }

  String getName();

  Date getEstablishedOn();

  default URI denotes() { return getResourceId(); }

  URI getResourceId();

}
