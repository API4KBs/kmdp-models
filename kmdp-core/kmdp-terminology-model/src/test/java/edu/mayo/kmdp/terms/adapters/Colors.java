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

import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.terms.adapters.IColors.IColorsVersion;
import edu.mayo.kmdp.terms.adapters.json.AbstractTermsJsonAdapter;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.Identifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.terms.EnumeratedConceptTerm;
import org.omg.spec.api4kp._20200801.terms.TermDescription;
import org.omg.spec.api4kp._20200801.terms.model.TermImpl;

/*
	Example of generated 'terminology' class
*
* */
@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = Colors.JsonSerializer.class)
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = Colors.JsonDeserializer.class)
public enum Colors implements IColorsVersion,
    EnumeratedConceptTerm<Colors, IColorsVersion, IColors> {

  RED("red", ColorsSeries.RED),

  BLUE("blu", ColorsSeries.BLUE),

  GREEN("grn", ColorsSeries.GREEN);

  TermDescription trm;

  ColorsSeries series;

  Colors(String code, ColorsSeries series) {
    UUID uuid = UUID.nameUUIDFromBytes(code.getBytes());
    trm = new TermImpl(
        URI.create(Registry.UUID_URN + uuid).toString(),
        uuid.toString(),
        "0.0.1",
        code,
        Collections.emptyList(),
        code,
        null,
        new Term[0],
        new Term[0],
        new Date());
    this.series = series;
  }

  @Override
  public Identifier getIdentifier() {
    return SemanticIdentifier.newVersionId(getResourceId(), getVersionId());
  }

  @Override
  public URI getResourceId() {
    return trm.getResourceId();
  }

  @Override
  public VersionIdentifier getVersionIdentifier() {
    return SemanticIdentifier.newVersionId(trm.getResourceId(), trm.getVersionId());
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
  public ColorsSeries asSeries() {
    return series;
  }

  public static class JsonSerializer extends AbstractTermsJsonAdapter.AbstractSerializer<Colors> {
  }

  public static class JsonDeserializer extends AbstractTermsJsonAdapter.AbstractDeserializer<Colors> {
    protected Colors[] getValues() {
      return values();
    }
    @Override
    protected Optional<Colors> resolveUUID(UUID uuid) {
      return Colors.resolveUUID(uuid);
    }
  }


  public static Optional<Colors> resolveUUID(final UUID uuid) {
    if (RED.getUuid().equals(uuid)) {
      return Optional.of(RED);
    }
    if (GREEN.getUuid().equals(uuid)) {
      return Optional.of(GREEN);
    }
    if (BLUE.getUuid().equals(uuid)) {
      return Optional.of(BLUE);
    }
    return Optional.empty();
  }

}


