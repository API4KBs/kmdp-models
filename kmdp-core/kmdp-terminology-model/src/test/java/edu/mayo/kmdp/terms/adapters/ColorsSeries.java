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


import static edu.mayo.kmdp.id.helper.DatatypeHelper.indexByUUID;

import edu.mayo.kmdp.terms.adapters.IColors.IColorsVersion;
import edu.mayo.kmdp.terms.adapters.json.AbstractTermsJsonAdapter;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.omg.spec.api4kp._20200801.id.Identifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.series.Series;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.EnumeratedConceptTerm;
import org.omg.spec.api4kp._20200801.terms.TermDescription;

/*
	Example of generated 'terminology' class
*
* */
@com.fasterxml.jackson.databind.annotation.JsonSerialize( using = ColorsSeries.JsonSerializer.class )
@com.fasterxml.jackson.databind.annotation.JsonDeserialize( using = ColorsSeries.JsonDeserializer.class )
public enum ColorsSeries implements IColors, Series<IColorsVersion,IColors>,
    EnumeratedConceptTerm<ColorsSeries, IColorsVersion, IColors> {

  RED(Colors.RED),
  GREEN(Colors.GREEN),
  BLUE(Colors.BLUE);

  public static final Map<UUID, IColors> index = indexByUUID(ColorsSeries.values());

  private List<IColorsVersion> versions;

  ColorsSeries(IColorsVersion... versions) {
    this.versions = Arrays.asList(versions);
  }


  @Override
  public Identifier getIdentifier() {
    return SemanticIdentifier.newId(getDescription().getResourceId());
  }


  @Override
  public ResourceIdentifier getNamespace() {
    return null;
  }

  @Override
  public TermDescription getDescription() {
    return latest().map(ConceptTerm::getDescription)
        .orElse(null);
  }

  @Override
  public ColorsSeries asEnum() {
    return this;
  }


  @Override
  public URI getResourceId() {
    return getDescription().getResourceId();
  }

  @Override
  public List<IColorsVersion> getVersions() {
    return versions;
  }

  @Override
  public UUID getUuid() {
    return getDescription().getUuid();
  }

  @Override
  public URI getNamespaceUri() {
    return null;
  }

  @Override
  public URI getVersionId() {
    return null;
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
  public ColorsSeries asSeries() {
    return this;
  }

  public static class JsonSerializer extends AbstractTermsJsonAdapter.AbstractSerializer {

  }

  public static class JsonDeserializer extends AbstractTermsJsonAdapter.AbstractDeserializer {
    protected Term[] getValues() {
      return values();
    }
    @Override
    protected Optional<IColors> resolveUUID(UUID uuid) {
      return ColorsSeries.resolveUUID(uuid);
    }
  }


  public static ColorsSeries resolve(IColors other) {
    return (ColorsSeries) resolveUUID(other.getUuid()).orElseThrow();
  }

  public static Optional<IColors> resolveUUID(final UUID conceptId) {
    return Optional.of(index.get(conceptId));
  }

}


