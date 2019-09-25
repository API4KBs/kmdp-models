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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.uhn.fhir.model.api.BaseElement;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.AgeDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContainedDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.model.dstu2.resource.Parameters.Parameter;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Provenance;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.primitive.StringDt;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.fhir.fhir2.FHIR2JacksonModule;
import edu.mayo.kmdp.util.fhir.fhir2.FHIR2JsonAdapter;
import edu.mayo.kmdp.util.fhir.fhir2.FHIR2JsonUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.instance.model.api.IBase;
import org.junit.jupiter.api.Test;

class FHIR2JsonSerializationTest {

  private Patient res = new Patient()
      .setIdentifier(Collections.singletonList(new IdentifierDt().setValue("p123")))
      .setName(Collections.singletonList(new HumanNameDt().addGiven("John").addFamily("Hurt")))
      .setGender(AdministrativeGenderEnum.OTHER);

  private Foo nonRes = new Foo();

  private Module module = new FHIR2JacksonModule();

  @Test
  void testSerializeFHIRResource() {
    Optional<String> pat = JSonUtil
        .writeJson(res, new FHIR2JacksonModule(), JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(pat.isPresent());
//    System.out.println(pat.get());

    Optional<Patient> p = JSonUtil.parseJson(pat.get(), module, Patient.class);
    assertTrue(p.isPresent());
    Patient reconstructed = p.get();
    assertEquals(reconstructed.getName().get(0).getFamily(), res.getName().get(0).getFamily());
    assertEquals(reconstructed.getIdentifier().get(0).getValue(),
        res.getIdentifier().get(0).getValue());
    assertEquals(reconstructed.getGender(), res.getGender());
  }

  @Test
  void testSerializeFHIRResourceWithInferredType() {
    Optional<String> pat = JSonUtil.writeJson(res, module, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(pat.isPresent());
//    System.out.println(pat.get());

    Optional<?> p = JSonUtil.parseJson(pat.get(), module, BaseResource.class);
    assertTrue(p.isPresent());
    assertTrue(p.get() instanceof Patient);
    Patient reconstructed = (Patient) p.get();
    assertEquals(reconstructed.getName().get(0).getFamily(), res.getName().get(0).getFamily());
    assertEquals(reconstructed.getIdentifier().get(0).getValue(),
        res.getIdentifier().get(0).getValue());
    assertEquals(reconstructed.getGender(), res.getGender());
  }

  @Test
  void testNonFHIRResource() {
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
  void testNonFHIRMaps() {
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
  void testInnerFHIRResource() {
    Boo boo = new Boo();
    boo.setPat(res);
    boo.setAge((AgeDt) new AgeDt().setValue(42));

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
  void testInnerFHIRResourceWithAnnotationControls() {
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
  void testFHIRDatatype() {
    QuantityDt q = new QuantityDt().setValue(42).setCode("a").setUnit("yr");

    Optional<String> qStr = JSonUtil.writeJson(q, module, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(qStr.isPresent());
//    System.out.println(qStr.get());

    Optional<QuantityDt> p = JSonUtil.parseJson(qStr.get(), module, QuantityDt.class);
    assertTrue(p.isPresent());
    QuantityDt reconstructed = p.get();
    assertEquals(42, reconstructed.getValue().intValue());
  }

  @Test
  void testFHIRinMap() {
    Map<String, IBase> map = new HashMap<>();
    map.put("a", res);
    map.put("c", new QuantityDt().setValue(42).setCode("a").setUnit("yr"));

    Optional<String> mapStr = JSonUtil.writeJson(map, module, JSonUtil.defaultProperties())
        .flatMap(Util::asString);
    assertTrue(mapStr.isPresent());
//    System.out.println(mapStr.get());

    Optional<Map<String, BaseElement>> p = JSonUtil
        .parseJson(mapStr.get(), module, asMapOf(String.class, BaseElement.class));
    assertTrue(p.isPresent());
    Map<String, BaseElement> reconstructed = p.get();
    assertTrue(reconstructed.get("a") instanceof Patient);
    assertTrue(reconstructed.get("c") instanceof QuantityDt);
  }

  @Test
  void testFHIRinMapField() {
    Map<String, BaseElement> map = new HashMap<>();
    map.put("a", res);
    map.put("b", new QuantityDt().setValue(42).setCode("a").setUnit("yr"));
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
    assertTrue(reconstructed.get("b") instanceof QuantityDt);
  }

  @Test
  void testParameters() {
    Parameters parameters = new Parameters();
    parameters.addParameter()
        .setName("key")
        .setValue(new StringDt().setValue("val"));
    String paramStr = FHIR2JsonUtil.instance.toJsonString(parameters);
    Parameters parameters2 = FHIR2JsonUtil.instance.parse(paramStr.getBytes(), Parameters.class);
    Parameters.Parameter p = parameters2.getParameterFirstRep();
    assertEquals("key", p.getName());
    assertEquals("val",
        ((StringDt) p.getValue()).getValueNotNull());

  }


  @Test
  void testNestedContains() {
    Observation obs = (Observation) new Observation()
        .setCode(new CodeableConceptDt().addCoding(new CodingDt().setCode("x")))
        .setId("#id1");

    Provenance prov = new Provenance();
    prov.addTarget().setReference("#id1");
    ContainedDt innerRes = new ContainedDt();
    innerRes.getContainedResources().add(obs);
    prov.setContained(innerRes);

    Parameters parameters = new Parameters()
        .addParameter(new Parameter().setName("key")
            .setResource(prov));

    String paramStr = FHIR2JsonUtil.instance.toJsonString(parameters);

    Parameters parameters2 = FHIR2JsonUtil.instance.parse(paramStr.getBytes(), Parameters.class);

    IResource r = parameters2.getParameterFirstRep().getResource();
    assertTrue(r instanceof Provenance);
    List<IResource> inner = ((Provenance) r).getContained().getContainedResources();
    assertFalse(inner.isEmpty());
    assertTrue(inner.get(0) instanceof Observation);

    Observation o = (Observation) inner.get(0);
    assertEquals("x", o.getCode().getCodingFirstRep().getCode());
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
    private QuantityDt age;

    public Patient getPat() {
      return pat;
    }

    public void setPat(Patient pat) {
      this.pat = pat;
    }

    public QuantityDt getAge() {
      return age;
    }

    public void setAge(QuantityDt age) {
      this.age = age;
    }
  }

  private static class Zoo extends Foo {

    @JsonSerialize(using = FHIR2JsonAdapter.FHIRSerializer.class)
    @JsonDeserialize(using = FHIR2JsonAdapter.FHIRDeserializer.class)
    private Patient pat;

    public Patient getPat() {
      return pat;
    }

    public void setPat(Patient pat) {
      this.pat = pat;
    }
  }


  private static class Noo {

    private Map<String, BaseElement> atts;

    public Map<String, BaseElement> getAtts() {
      return atts;
    }

    public void setAtts(Map<String, BaseElement> atts) {
      this.atts = atts;
    }
  }
}
