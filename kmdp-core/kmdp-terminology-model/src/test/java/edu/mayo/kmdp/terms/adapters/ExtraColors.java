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
package edu.mayo.kmdp.terms.adapters;

import edu.mayo.kmdp.terms.adapters.IColors.IColorsVersion;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.Identifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.terms.EnumeratedConceptTerm;
import org.omg.spec.api4kp._20200801.terms.TermDescription;
import org.omg.spec.api4kp._20200801.terms.model.TermImpl;

/*
	Example of generated 'terminology' class
*
* */
public enum ExtraColors implements IColorsVersion,
    EnumeratedConceptTerm<ExtraColors, IColorsVersion, IColors> {

  BLACK("black"),

  WHITE("white");

  TermDescription trm;


  ExtraColors(String code) {
    UUID uuid = UUID.nameUUIDFromBytes(code.getBytes());
    trm = new TermImpl(
        URI.create("urn:uuid:" + uuid).toString(),
        uuid.toString(),
        "0.0.1",
        code,
        Collections.emptyList(),
        code,
        null,
        new Term[0],
        new Term[0],
        new Date());
  }

  @Override
  public VersionIdentifier getVersionIdentifier() {
    return SemanticIdentifier.newId(trm.getTag(),"0.0.1");
  }

  @Override
  public ResourceIdentifier getNamespace() {
    return null;
  }

  @Override
  public TermDescription getDescription() {
    return trm;
  }

  @Override
  public URI getResourceId() {
    return trm.getResourceId();
  }

  @Override
  public UUID getUuid() {
    return trm.getUuid();
  }

  @Override
  public URI getNamespaceUri() {
    return trm.getNamespaceUri();
  }

  @Override
  public URI getVersionId() {
    return trm.getVersionId();
  }

  @Override
  public String getVersionTag() {
    return null;
  }

  @Override
  public Date getEstablishedOn() {
    return new Date();
  }

  @Override
  public ResourceIdentifier getDefiningScheme() {
    return null;
  }

  @Override
  public Date getVersionEstablishedOn() {
    return new Date();
  }

  @Override
  public Series<IColorsVersion, IColors> asSeries() {
    return null;
  }

  @Override
  public Identifier getIdentifier() {
    return SemanticIdentifier.newVersionId(trm.getResourceId(),trm.getVersionId());
  }
}


