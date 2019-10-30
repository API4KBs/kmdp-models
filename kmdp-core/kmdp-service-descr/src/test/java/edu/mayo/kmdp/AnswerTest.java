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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.api4kp.parsinglevel.ParsingLevelSeries;
import edu.mayo.ontology.taxonomies.api4kp.responsecodes._2011.ResponseCode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.AbstractCarrier;
import org.omg.spec.api4kp._1_0.Answer;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.KnowledgeCarrier;

public class AnswerTest {

  @Test
  public void testConstruction() {
    Answer<String> ans = Answer.of("foo");

    assertEquals("foo", ans.getOptionalValue().orElse("Missing"));

    KnowledgeCarrier expl = ans.getExplanation();
    assertNotNull(expl);
    assertTrue(expl instanceof ExpressionCarrier);
    assertNotNull(((ExpressionCarrier) expl).getSerializedExpression());
    assertTrue(((ExpressionCarrier) expl).getSerializedExpression().contains("foo"));
    assertEquals(ParsingLevelSeries.Concrete_Knowledge_Expression, expl.getLevel().asSeries());

    assertTrue(ans.isSuccess());
    assertFalse(ans.isFailure());

    assertFalse(Util.isEmpty(ans.printExplanation()));
  }

  @Test
  public void testSimpleChaining() {
    Answer<String> ans = Answer.of("foo");

    Answer<Integer> iAns = ans
        .map(String::toUpperCase)
        .map(String::length);

    assertEquals(3, iAns.getOptionalValue().orElse(-1));
  }

  @Test
  public void testChaining() {
    Answer<String> ans = Answer.of("foo");

    Answer<Integer> iAns = ans
        .flatMap((s) -> Answer.of(s.length()));

    assertEquals(3, iAns.getOptionalValue().orElse(-1));
  }

  @Test
  public void testCode() {
    Answer<String> ans1 = Answer.of(202, "foo", Collections.emptyMap());
    Answer<String> ans2 = Answer.of(ResponseCode.OK.getTag(), "foo", Collections.emptyMap());
    Answer<String> ans3 = Answer.of(ResponseCode.OK, "foo", Collections.emptyMap());

    assertTrue(ans1.isSuccess());
    assertTrue(ans2.isSuccess());
    assertTrue(ans3.isSuccess());
  }


  @Test
  public void testMeta() {
    String backLink = "http://goto.here/123";
    Map<String, List<String>> headers = new HashMap<>();
    headers.put("Link", Collections.singletonList(backLink));

    Answer<String> ans = Answer.of(ResponseCode.OK, "foo", headers);

    assertEquals(backLink, ans.getMeta("Link").orElse("META not found"));
  }

  @Test
  public void testExplanationConstruction() {
    String msg = "This is the history";
    Answer<String> ans = Answer.of(ResponseCode.OK, "foo").withExplanation(msg);
    assertEquals(msg, ans.printExplanation());
  }


  @Test
  //TODO this is clunky, because the map/flatMap operations on KnowledgeCarrier are still TODOs
  public void testWithKCarrier() {
    Answer<? extends KnowledgeCarrier> ans = Answer.of(AbstractCarrier.ofNaturalLanguageRep("Foo"));

    ans = ans.map(
        (kc) -> kc.map( (self) -> KnowledgeCarrier.ofNaturalLanguageRep(
            "mapped " + ((ExpressionCarrier)self).getSerializedExpression() ) ) );

    KnowledgeCarrier kc = ans.getOptionalValue().get();
    assertNotNull(kc);
    assertEquals( "mapped Foo", ((ExpressionCarrier) kc).getSerializedExpression());
  }

}
