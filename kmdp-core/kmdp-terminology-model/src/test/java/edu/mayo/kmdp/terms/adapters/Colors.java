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

import edu.mayo.kmdp.id.Identifier;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.series.Series;
import edu.mayo.kmdp.terms.TermDescription;
import edu.mayo.kmdp.terms.impl.model.TermImpl;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.VersionIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.VersionTagType;

/*
	Example of generated 'terminology' class
*
* */
@com.fasterxml.jackson.databind.annotation.JsonSerialize( using = Colors.JsonSerializer.class )
@com.fasterxml.jackson.databind.annotation.JsonDeserialize( using = Colors.JsonDeserializer.class )
public enum Colors implements IColors {

  RED("red", ColorsSeries.RED),

  BLUE("blu", ColorsSeries.BLUE),

  GREEN("grn", ColorsSeries.GREEN);

  TermDescription trm;
  ColorsSeries series;

  Colors(String code, ColorsSeries series) {
    UUID uuid = UUID.nameUUIDFromBytes(code.getBytes());
    trm = new TermImpl(
        URI.create("urn:uuid:" + uuid).toString(),
        uuid.toString(),
        code,
        Collections.emptyList(),
        code,
        null,
        new Term[0],
        new Term[0]);
    this.series = series;
  }

  @Override
  public VersionedIdentifier getVersionIdentifier() {
    return new VersionIdentifier()
        .withTag(trm.getTag())
        .withVersion("0.0.1")
        .withEstablishedOn(new Date())
        .withVersioning(VersionTagType.SEM_VER);
  }

  @Override
  public TermDescription getDescription() {
    return trm;
  }

  @Override
  public Identifier getNamespace() {
    return new NamespaceIdentifier()
        .withId(URI.create("http://colors.foo"))
        .withTag(trm.getTag())
        .withVersion("0.0.1")
        .withEstablishedOn(new Date())
        .withVersioning(VersionTagType.SEM_VER);
  }

  @Override
  public ColorsSeries asEnum() {
    return series;
  }

  @Override
  public Series<IColors> asSeries() {
    return series;
  }

  public static class JsonSerializer extends ConceptTermsJsonAdapter.Serializer<Colors> {
  }

  public static class JsonDeserializer extends ConceptTermsJsonAdapter.Deserializer<Colors> {
    protected Colors[] getValues() {
      return values();
    }
    @Override
    protected Optional<Colors> resolveUUID(UUID uuid) {
      return Colors.resolveUUID(uuid);
    }
  }


  public static Optional<Colors> resolveUUID(final UUID uuid) {
    if (RED.getConceptUUID().equals(uuid)) {
      return Optional.of(RED);
    }
    if (GREEN.getConceptUUID().equals(uuid)) {
      return Optional.of(GREEN);
    }
    if (BLUE.getConceptUUID().equals(uuid)) {
      return Optional.of(BLUE);
    }
    return Optional.empty();
  }

}


