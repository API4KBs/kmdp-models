/**
 * Copyright © 2023 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
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
package org.omg.spec.api4kp._20200801.services.repository.artifact;

import static java.lang.String.format;

import edu.mayo.kmdp.util.Util;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KArtfHrefBuilder {

  private static final Logger logger = LoggerFactory.getLogger(KArtfHrefBuilder.class);

  public enum HrefType {
    REPO,
    ARTIFACT_VERSIONS,
    ARTIFACT_VERSION,
    ARTIFACT_SERIES
  }

  protected final Properties cfg;

  public KArtfHrefBuilder(Properties cfg) {
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

  public URI getHref(
      String repoId, String artifactId, String versionTag,
      KArtfHrefBuilder.HrefType hrefType) {
    try {
      switch (hrefType) {
        case REPO:
          return getRepositorytHref(repoId).toURI();
        case ARTIFACT_SERIES:
          return getArtifactSeriesHref(repoId, artifactId).toURI();
        case ARTIFACT_VERSIONS:
          return getArtifactVersionsHref(repoId, artifactId).toURI();
        case ARTIFACT_VERSION:
          return getArtifactVersionHref(
              repoId, artifactId, versionTag).toURI();
        default:
          throw new UnsupportedOperationException("TODO: add Href for " + hrefType.name());
      }
    } catch (URISyntaxException mfe) {
      logger.error(mfe.getMessage(), mfe);
      return null;
    }
  }


  public URL getRepositorytHref(String repositoryId) {
    try {
      return URI.create(format(
          "%s/repos/%s",
          getHost(), repositoryId)).toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public URL getArtifactSeriesHref(String repositoryId, String artifactId) {
    try {
      return URI.create(format(
          "%s/repos/%s/artifacts/%s",
          getHost(), repositoryId, artifactId)).toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public URL getArtifactVersionsHref(String repositoryId, String artifactId) {
    try {
      return URI.create(format(
          "%s/repos/%s/artifacts/%s/versions",
          getHost(), repositoryId, artifactId)).toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public URL getArtifactVersionHref(String repositoryId, String artifactId, String versionTag) {
    try {
      return URI.create(format(
          "%s/repos/%s/artifacts/%s/versions/%s",
          getHost(), repositoryId, artifactId, versionTag)).toURL();
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }
}
