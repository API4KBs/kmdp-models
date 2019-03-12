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
package edu.mayo.kmdp;

import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.id.helper.DatatypeHelper;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IDConversionTest {

  @Test
  public void testURISplitting() {
    URI u0 = URI.create("http:/some/thing");
    URI u1 = URI.create("http:/some/thing/versions/ver");

    URIIdentifier uid1 = DatatypeHelper.uri("http:/some/", "thing", "ver");

    assertEquals(u0, uid1.getUri());
    assertEquals(u1, uid1.getVersionId());

    VersionedIdentifier vid = DatatypeHelper.toVersionIdentifier(uid1);

    assertEquals("ver", vid.getVersion());
    assertEquals("thing", vid.getTag());
  }

  @Test
  public void testURISplittingWithFragment() {
    URIIdentifier uid1 = DatatypeHelper.uri("http:/some/thing/versions/ver#piece");

    VersionedIdentifier vid = DatatypeHelper.toVersionIdentifier(uid1);

    assertEquals("ver", vid.getVersion());
    assertEquals("piece", vid.getTag());
  }

  @Test
  public void testUUIDBasedVersionedID() {
    VersionedIdentifier id = DatatypeHelper.toVersionIdentifier(
        URI.create("http://test.ckm.mock.edu/c1e68d43-4354-3e0c-9068-1f57daa39c3f/versions/7"));
    assertEquals("c1e68d43-4354-3e0c-9068-1f57daa39c3f", id.getTag());
    assertEquals("7", id.getVersion());
  }


  @Test
  public void testUriBasedVersionedID() {
    URIIdentifier id = DatatypeHelper
        .uri("http://test.ckm.mock.edu/c1e68d43-4354-3e0c-9068-1f57daa39c3f", "7");
    assertEquals("c1e68d43-4354-3e0c-9068-1f57daa39c3f", id.getTag());
    assertEquals("7", id.getVersion());
  }
}
