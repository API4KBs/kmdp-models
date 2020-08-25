package org.omg.spec.api4kp._1_0.id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.mayo.kmdp.id.Term;

import java.net.URI;

public interface Link {

  SemanticIdentifier getHref();

  @JsonIgnore
  default URI getHrefURI() {
    return getHref().getResourceId();
  }

  @JsonIgnore
  default URI getHrefVersionURI() {
    return getHref().getVersionId();
  }

  Term getRel();

  @JsonIgnore
  default URI getRelURI() {
    return getRel().getConceptId();
  }
}
