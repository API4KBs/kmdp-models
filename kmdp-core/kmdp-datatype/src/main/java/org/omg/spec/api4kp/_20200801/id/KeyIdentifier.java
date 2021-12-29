package org.omg.spec.api4kp._20200801.id;

import java.util.UUID;

public interface KeyIdentifier extends Comparable<KeyIdentifier> {

  UUID getUuid();

  int getVersionHash();

  String getVersionTag();

  default boolean isSameEntity(KeyIdentifier other) {
    return this.getUuid() != null &&
        this.getUuid().equals(other.getUuid());
  }
}
