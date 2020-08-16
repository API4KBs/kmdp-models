package org.omg.spec.api4kp._20200801.id;

import java.util.UUID;

public interface KeyIdentifier {

  UUID getUuid();

  int getVersionHash();

}
