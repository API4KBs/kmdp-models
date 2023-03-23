/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.omg.spec.api4kp._20200801.services.repository.asset;

import edu.mayo.kmdp.util.Util;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see org.omg.spec.api4kp._20200801.services.URIPathHelper
 */
public class KARSHrefBuilder {

  private static final Logger logger = LoggerFactory.getLogger(KARSHrefBuilder.class);

  public enum HrefType {
    ASSET,
    ASSET_VERSION,
    ASSET_CARRIER,
    ASSET_CARRIER_VERSION,
    ASSET_SURROGATE,
    ASSET_SURROGATE_VERSION
  }

  protected final Properties cfg;

  public KARSHrefBuilder(Properties cfg) {
    this.cfg = cfg;
  }

  public String getHost() {
    return "http:/";
  }

  public String getCurrentURL() {
    return "";
  }

  public URI getRelativeURL(String relative) {
    String curr = getCurrentURL();
    if (Util.isEmpty(relative)) {
      return URI.create(curr);
    }
    if (curr.endsWith("/") && relative.startsWith("/")) {
      curr = curr.substring(0, curr.length() - 1);
    }
    return URI.create(curr + relative);
  }

  public URI getHref(ResourceIdentifier assetId, ResourceIdentifier artifactId, HrefType hrefType) {
    try {
      switch (hrefType) {
        case ASSET:
          return getAssetHref(assetId.getUuid()).toURI();
        case ASSET_VERSION:
          return getAssetVersionHref(assetId.getUuid(), assetId.getVersionTag()).toURI();
        case ASSET_CARRIER:
          return getAssetCarrierHref(assetId.getUuid(), assetId.getVersionTag(),
              artifactId.getUuid()).toURI();
        case ASSET_CARRIER_VERSION:
          return getAssetCarrierVersionHref(assetId.getUuid(), assetId.getVersionTag(),
              artifactId.getUuid(), artifactId.getVersionTag()).toURI();
        case ASSET_SURROGATE:
          return getSurrogateRef(assetId.getUuid(), assetId.getVersionTag(),
              artifactId.getUuid()).toURI();
        case ASSET_SURROGATE_VERSION:
          return getSurrogateVersionRef(assetId.getUuid(), assetId.getVersionTag(),
              artifactId.getUuid(), artifactId.getVersionTag()).toURI();
        default:
          throw new UnsupportedOperationException("TODO: add Href for " + hrefType.name());
      }
    } catch (URISyntaxException mfe) {
      logger.error(mfe.getMessage(), mfe);
      return null;
    }
  }

  public URI getHref(ResourceIdentifier assetId, HrefType hrefType) {
    switch (hrefType) {
      case ASSET:
        return getHref(assetId, null, HrefType.ASSET);
      case ASSET_VERSION:
        return getHref(assetId, null, HrefType.ASSET_VERSION);
      default:
        throw new UnsupportedOperationException(hrefType.name() + "requires an ArtifactId as well");
    }
  }

  public URL getAssetHref(UUID id) {
    try {
      return URI.create(String.format("%s/cat/assets/%s", getHost(), id)).toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public URL getAssetVersionHref(UUID id, String version) {
    try {
      return URI.create(String.format("%s/cat/assets/%s/versions/%s", getHost(), id, version))
          .toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public URL getAssetCarrierHref(UUID assetId, String assetVersion, UUID carrierId) {
    try {
      return URI.create(String
          .format("%s/cat/assets/%s/versions/%s/carriers/%s", getHost(), assetId,
              assetVersion, carrierId)).toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public URL getAssetDefaultContent(UUID assetId, String assetVersion) {
    try {
      return URI.create(String
          .format("%s/cat/assets/%s/versions/%s/carrier/content",
              getHost(), assetId, assetVersion)).toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public URL getAssetCarrierVersionHref(UUID assetId, String assetVersion, UUID carrierId,
      String carrierVersion) {
    try {
      return URI.create(String
          .format("%s/cat/assets/%s/versions/%s/carriers/%s/versions/%s", getHost(), assetId,
              assetVersion, carrierId, carrierVersion)).toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public URL getSurrogateRef(UUID assetId, String versionTag, UUID surrogateId) {
    try {
      return URI.create(String
          .format("%s/cat/assets/%s/versions/%s/surrogate/%s", getHost(), assetId,
              versionTag, surrogateId)).toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public URL getSurrogateVersionRef(UUID assetId, String versionTag, UUID surrogateId,
      String surrogateVersionTag) {
    try {
      return URI.create(String
          .format("%s/cat/assets/%s/versions/%s/surrogate/%s/versions/%s", getHost(), assetId,
              versionTag, surrogateId, surrogateVersionTag)).toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

}
