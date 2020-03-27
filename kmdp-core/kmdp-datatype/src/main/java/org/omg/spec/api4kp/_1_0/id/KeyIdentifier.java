package org.omg.spec.api4kp._1_0.id;

import java.util.UUID;

public interface KeyIdentifier {

  UUID getUuid();

  int getVersionHash();

}
