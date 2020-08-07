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
package edu.mayo.kmdp.terms.impl.model;

import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.util.Util;
import java.net.URI;
import java.util.Date;
import java.util.List;
import org.omg.spec.api4kp._1_0.id.Term;

public class InternalTerm extends TermImpl {

  protected ConceptScheme<Term> scheme;
  protected String comment;

  public InternalTerm(URI conceptURI, String code, String label, String comment, URI refUri,
      ConceptScheme<Term> scheme, Date establishedOn) {
    // TODO should this call super?
    this.referentId = refUri;
    this.tag = code;
    this.name = label;
    this.comment = Util.isEmpty(comment) ? label : comment;
    this.scheme = scheme;
    this.resourceId = conceptURI;
    this.establishedOn = establishedOn;

    if (scheme != null) {
      this.namespaceUri = scheme.getResourceId();
    }
  }

  public ConceptScheme<Term> getScheme() {
    return scheme;
  }

  public String toString() {
    return getReferentId().toString();
  }
  
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public boolean equals(Object object) {
    return super.equals(object);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
