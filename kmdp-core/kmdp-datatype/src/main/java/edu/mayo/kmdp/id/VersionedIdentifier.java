/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.id;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.util.Date;
import java.util.Optional;
import org.omg.spec.api4kp._1_0.identifiers.VersionTagType;

public interface VersionedIdentifier extends Identifier, Comparable<VersionedIdentifier> {

  String getVersion();

  Date getEstablishedOn();

  @JsonIgnore
  default VersionTagType getVersioning() {
    return VersionTagType.GENERIC;
  }

  @Override
  default int compareTo(VersionedIdentifier o) {
    if (getVersioning() != null && getVersioning() == o.getVersioning()) {
      switch (getVersioning()) {
        case SEM_VER:
          return compareAsSemVer(this, o);
        case TIMESTAMP:
          return compareAsDate(this, o);
        case SEQUENTIAL:
          return compareAsNumber(this,o);
        case GENERIC:
        default:
      }
    }
    return getVersion().compareTo(o.getVersion());
  }

  default int compareAsNumber(VersionedIdentifier i, VersionedIdentifier o) {
    return Integer.parseInt(i.getVersion()) - Integer.parseInt(o.getVersion());
  }

  default int compareAsDate(VersionedIdentifier i, VersionedIdentifier o) {
    return i.getEstablishedOn().compareTo(o.getEstablishedOn());
  }

  default int compareAsSemVer(VersionedIdentifier i, VersionedIdentifier o) {
    return Version.valueOf(i.getVersion())
        .compareTo(Version.valueOf(o.getVersion()));
  }

}
