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

import static edu.mayo.kmdp.util.JSonUtil.asMapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.fhir.fhir2_hl7.FHIR2HL7JacksonModule;
import edu.mayo.kmdp.util.fhir.fhir2_hl7.FHIR2HL7JsonAdapter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.instance.model.Age;
import org.hl7.fhir.instance.model.Base;
import org.hl7.fhir.instance.model.BaseResource;
import org.hl7.fhir.instance.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.api.IBase;
import org.junit.jupiter.api.Test;

public class FHIR2HL7JsonSerializationTest {

  private Patient res = (Patient) new Patient()
      .addName(new HumanName().addGiven("John").addFamily("Hurt"))
      .addIdentifier(new Identifier().setValue("p123"))
      .setGender(AdministrativeGender.OTHER)
      .setId("p123");

  private Foo nonRes = new Foo();

  private Module module = new FHIR2HL7JacksonModule();

  @Test
  public void testSerializeFHIRResource() {
    Optional<String> pat = JSonUtil
        .writeJson(res, new FHIR2HL7JacksonModule(), JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(pat.isPresent());
//    System.out.println(pat.get());

    Optional<Patient> p = JSonUtil.parseJson(pat.get(), module, Patient.class);
    assertTrue(p.isPresent());
    Patient reconstructed = p.get();
    assertEquals(
        res.getName().get(0).getFamily().get(0).getValue(),
        reconstructed.getName().get(0).getFamily().get(0).getValue());
    assertEquals(
        res.getIdentifier().get(0).getValue(),
        reconstructed.getIdentifier().get(0).getValue());
    assertEquals(
        res.getGender(),
        reconstructed.getGender());
  }

  @Test
  public void testSerializeFHIRResourceWithInferredType() {
    Optional<String> pat = JSonUtil.writeJson(res, module, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(pat.isPresent());
//    System.out.println(pat.get());

    Optional<?> p = JSonUtil.parseJson(pat.get(), module, BaseResource.class);
    assertTrue(p.isPresent());
    assertTrue(p.get() instanceof Patient);
    Patient reconstructed = (Patient) p.get();
    assertEquals(
        res.getName().get(0).getFamily().get(0).getValue(),
        reconstructed.getName().get(0).getFamily().get(0).getValue());
    assertEquals(reconstructed.getIdentifier().get(0).getValue(),
        res.getIdentifier().get(0).getValue());
    assertEquals(reconstructed.getGender(), res.getGender());
  }

  @Test
  public void testNonFHIRResource() {
    Optional<String> foo = JSonUtil.writeJson(nonRes, module, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(foo.isPresent());
//    System.out.println(foo.get());

    Optional<Foo> p = JSonUtil.parseJson(foo.get(), module, Foo.class);
    assertTrue(p.isPresent());
    Foo reconstructed = p.get();
    assertEquals(reconstructed.getBar(), nonRes.getBar());
  }


  @Test
  public void testNonFHIRMaps() {
    Map<String, String> map = new HashMap<>();
    map.put("a", "b");
    Optional<String> foo = JSonUtil.writeJson(map, module, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(foo.isPresent());
//    System.out.println(foo.get());

    Optional<Map<String, String>> p = JSonUtil
        .parseJson(foo.get(), module, asMapOf(String.class, String.class));
    assertTrue(p.isPresent());
    Map<String, String> reconstructed = p.get();
    assertEquals("b", reconstructed.get("a"));
  }


  @Test
  public void testInnerFHIRResource() {
    Boo boo = new Boo();
    boo.setPat(res);
    boo.setAge((Age) new Age().setValue(BigDecimal.valueOf(42)));

    Optional<String> booStr = JSonUtil.writeJson(boo, module, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(booStr.isPresent());
//    System.out.println(booStr.get());

    Optional<Boo> p = JSonUtil.parseJson(booStr.get(), module, Boo.class);
    assertTrue(p.isPresent());
    Boo reconstructed = p.get();
    assertEquals(reconstructed.getBar(), nonRes.getBar());
    assertEquals(reconstructed.getPat().getGender(), boo.getPat().getGender());
    assertEquals(42, reconstructed.getAge().getValue().intValue());
  }

  @Test
  public void testInnerFHIRResourceWithAnnotationControls() {
    Zoo zoo = new Zoo();
    zoo.setPat(res);

    Optional<String> zooStr = JSonUtil.writeJson(zoo, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(zooStr.isPresent());
//    System.out.println(zooStr.get());

    Optional<Zoo> p = JSonUtil.parseJson(zooStr.get(), Zoo.class);
    assertTrue(p.isPresent());
    Zoo reconstructed = p.get();
    assertEquals(reconstructed.getBar(), nonRes.getBar());
    assertEquals(reconstructed.getPat().getGender(), zoo.getPat().getGender());
  }


  @Test
  public void testFHIRDatatype() {
    Quantity q = new Quantity().setValue(BigDecimal.valueOf(42)).setCode("a").setUnit("yr");

    Optional<String> qStr = JSonUtil.writeJson(q, module, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(qStr.isPresent());
//    System.out.println(qStr.get());

    Optional<Quantity> p = JSonUtil.parseJson(qStr.get(), module, Quantity.class);
    assertTrue(p.isPresent());
    Quantity reconstructed = p.get();
    assertEquals(42, reconstructed.getValue().intValue());
  }

  @Test
  public void testFHIRinMap() {
    Map<String, IBase> map = new HashMap<>();
    map.put("a", res);
    map.put("c", new Quantity().setValue(BigDecimal.valueOf(42)).setCode("a").setUnit("yr"));

    Optional<String> mapStr = JSonUtil.writeJson(map, module, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(mapStr.isPresent());
//    System.out.println(mapStr.get());

    Optional<Map<String, Base>> p = JSonUtil
        .parseJson(mapStr.get(), module, asMapOf(String.class, Base.class));
    assertTrue(p.isPresent());
    Map<String, Base> reconstructed = p.get();
    assertTrue(reconstructed.get("a") instanceof Patient);
    assertTrue(reconstructed.get("c") instanceof Quantity);
  }

  @Test
  public void testFHIRinMapField() {
    Map<String, Base> map = new HashMap<>();
    map.put("a", res);
    map.put("b", new Quantity().setValue(BigDecimal.valueOf(42)).setCode("a").setUnit("yr"));
    Noo noo = new Noo();
    noo.setAtts(map);

    Optional<String> nooStr = JSonUtil.writeJson(noo, module, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(nooStr.isPresent());
//    System.out.println(nooStr.get());

    Optional<Noo> p = JSonUtil.parseJson(nooStr.get(), module, Noo.class);
    assertTrue(p.isPresent());
    Map reconstructed = p.get().getAtts();
    assertTrue(reconstructed.get("a") instanceof Patient);
    assertTrue(reconstructed.get("b") instanceof Quantity);
  }

  private static class Foo {

    private String bar = "bar";

    public String getBar() {
      return bar;
    }

    public void setBar(String bar) {
      this.bar = bar;
    }
  }

  private static class Boo extends Foo {

    private Patient pat;
    private Quantity age;

    public Patient getPat() {
      return pat;
    }

    public void setPat(Patient pat) {
      this.pat = pat;
    }

    public Quantity getAge() {
      return age;
    }

    public void setAge(Quantity age) {
      this.age = age;
    }
  }

  private static class Zoo extends Foo {

    @JsonSerialize(using = FHIR2HL7JsonAdapter.FHIRSerializer.class)
    @JsonDeserialize(using = FHIR2HL7JsonAdapter.FHIRDeserializer.class)
    private Patient pat;

    public Patient getPat() {
      return pat;
    }

    public void setPat(Patient pat) {
      this.pat = pat;
    }
  }


  private static class Noo {

    private Map<String, Base> atts;

    public Map<String, Base> getAtts() {
      return atts;
    }

    public void setAtts(Map<String, Base> atts) {
      this.atts = atts;
    }
  }
}
