package org.omg.spec.api4kp._1_0.id;

import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.UUID;
import javax.xml.namespace.QName;

public interface ScopedIdentifier extends Identifier {

  URI getResourceId();
  UUID getUuid();
  URI getNamespace();

  /**
   * compose QName given namespace and tag
   * @return
   */
  default QName getQName() {
    verifyData(getTag(), getNamespace());
    return QName.valueOf(getNamespace() + ":" + getTag());
  }

  default void verifyData(String tag, URI namespace){
    if(Util.isEmpty(tag)) {
      throw new IllegalStateException("Tag is required to compose QName");
    }
    // TODO: if no namespace, compose from resourceId instead of error?
    if(Util.isEmpty(namespace.toString())) {
      throw new IllegalStateException("Namespace is required to compose QName");
    }
  }

}
