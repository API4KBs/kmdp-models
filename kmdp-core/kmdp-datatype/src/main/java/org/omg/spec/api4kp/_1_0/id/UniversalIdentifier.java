package org.omg.spec.api4kp._1_0.id;

import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public interface UniversalIdentifier extends Identifier {

  UUID getUuid();

  static UUID toUUID(String tag, URI resourceId) {
    if(tag != null) {
      return Util.ensureUUIDFormat(tag)
          .map(UUID::fromString)
          // if tag is not UUID format, create UUID from resourceId
          .orElse(Optional.of(UUID.nameUUIDFromBytes(resourceId.toString().getBytes()))
              .orElseThrow(() ->
                  new IllegalStateException(
                      "UUID Identifier not initialized with a valid UUID " + tag)));
    } else {
      throw new IllegalStateException("Tag value is required for Identifier.");
    }
  }


}
