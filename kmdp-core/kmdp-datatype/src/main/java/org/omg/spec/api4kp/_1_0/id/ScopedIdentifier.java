package org.omg.spec.api4kp._1_0.id;

import static edu.mayo.kmdp.registry.Registry.BASE_UUID_URN_URI;

import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.UUID;
import javax.xml.namespace.QName;

public interface ScopedIdentifier extends Identifier {

  default QName getQName() {
    return QName.valueOf(getNamespace() + ":" + getTag());
  }

  // TODO: does this belong in another interface? Is this even needed?
  default URI getResourceId() {
    verifyData(getTag(), getNamespace(), getUuid());
    if (getTag() != null && getNamespace() != null) {
      return URI.create(getNamespace() + getTag());
    } else {
      return URI.create(getNamespace() + getUuid().toString());
    }
  }

  default void verifyData(String tag, URI namespace, UUID uuid){
    if(Util.isEmpty(tag) && Util.isEmpty(uuid.toString())) {
      throw new IllegalStateException("Tag or UUID required to compose ResourceId");
    }
    if(Util.isEmpty(namespace.toString())) {
      throw new IllegalStateException("Namespace is required to compose ResourceId");
    }
  }

  UUID getUuid();
  URI getNamespace();
}
