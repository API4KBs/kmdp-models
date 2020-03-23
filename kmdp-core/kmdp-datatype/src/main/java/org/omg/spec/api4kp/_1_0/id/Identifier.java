package org.omg.spec.api4kp._1_0.id;

import static edu.mayo.kmdp.util.Util.isOID;
import static edu.mayo.kmdp.util.Util.isUUID;

import java.net.URI;
import java.util.Date;

public interface Identifier {
  String getTag();

  default IdentifierTagType getFormat() {
    // OID format
    if(isOID(getTag())) {
      return IdentifierTagType.OID_VALUE;
    }
    // UUID format
    if(isUUID(getTag())) {
      return IdentifierTagType.UUID_VALUE;
    }
    // default
    return IdentifierTagType.STRING_VALUE;
  }

  String getName();

  Date getEstablishedOn();

  default URI denotes() { return getResourceId(); }

  URI getResourceId();

}
