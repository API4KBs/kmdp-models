package org.omg.spec.api4kp._20200801.id;

import static edu.mayo.kmdp.util.Util.isOID;
import static edu.mayo.kmdp.util.Util.isUUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URI;
import java.util.Date;

public interface Identifier {

  String getTag();

  String getName();

  Date getEstablishedOn();

  default URI identifies() { return getResourceId(); }

  URI getResourceId();

  @JsonIgnore
  default IdentifierTagType getTagFormat() {
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

}
