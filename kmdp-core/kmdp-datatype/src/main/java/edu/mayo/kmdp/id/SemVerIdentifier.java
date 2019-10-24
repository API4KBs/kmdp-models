package edu.mayo.kmdp.id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zafarkhaja.semver.Version;

public interface SemVerIdentifier extends VersionedIdentifier {

  @JsonIgnore
  default Version getSemanticVersion() {
    return Version.valueOf(getVersion());
  }

  @Override
  default int compareTo(VersionedIdentifier o) {
    if (o instanceof SemVerIdentifier) {
      return getSemanticVersion().compareTo(((SemVerIdentifier) o).getSemanticVersion());
    }
    return VersionedIdentifier.super.compareTo(o);
  }
}
