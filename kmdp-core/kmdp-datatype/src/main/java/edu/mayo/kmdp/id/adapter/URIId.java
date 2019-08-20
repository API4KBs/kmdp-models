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
package edu.mayo.kmdp.id.adapter;


import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import java.net.URI;

public interface URIId extends VersionedIdentifier {

  URI getVersionId();

  URI getUri();

  @Override
  @JsonIgnore
  default String getVersion() {
    return DatatypeHelper.versionOf(getVersionId(), getUri());
  }

  @Override
  @JsonIgnore
  default String getTag() {
    return getUri().getFragment() != null
        ? getUri().getFragment()
        : getUri().toString().substring(getUri().toString().lastIndexOf('/') + 1,
            getUri().toString().length());
  }

  default String toStringId() {
    return getVersionId() != null ? getVersionId().toString() : getUri().toString();
  }
}
