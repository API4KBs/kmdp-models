package org.omg.spec.api4kp._1_0.id;

import java.net.URI;
import java.util.Date;

public interface Identifier {
  String getTag();

  default URI getFormat() {
    return null;
  }

  String getName();

  Date getEstablishedOn();

  URI getDenotes();

}
