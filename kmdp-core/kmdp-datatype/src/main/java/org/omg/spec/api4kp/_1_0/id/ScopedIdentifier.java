package org.omg.spec.api4kp._1_0.id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.UUID;
import javax.xml.namespace.QName;

public interface ScopedIdentifier extends Identifier {

  URI getResourceId();
  UUID getUuid();
  URI getNamespaceUri();

  /**
   * compose QName given namespace and tag
   * @return QName
   */
  @JsonIgnore
  default QName getQName() {
    String tag = getTag();
    if(Util.isEmpty(tag)) {
      throw new IllegalStateException("Tag is required to compose QName");
    } else {
      // verify tag format -- cannot start with digit
      if(Character.isDigit(tag.charAt(0))) {
        tag = "_" + tag;
      }
    }
    if(null != getNamespaceUri()) {
      return new QName(getNamespaceUri().toString(), tag);
    } else {
      return new QName(tag);
    }
  }

}
