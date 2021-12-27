/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.Answer.merge;
import static org.omg.spec.api4kp._20200801.taxonomy.parsinglevel.ParsingLevelSeries.Abstract_Knowledge_Expression;

import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;

class AnswerTest {

  @Test
  void testConstruction() {
    Answer<String> ans = Answer.of("foo");

    assertEquals("foo", ans.getOptionalValue().orElse("Missing"));

    assertNotNull(ans.getExplanation());
    assertTrue(ans.getExplanation().asString().isEmpty());
    assertTrue(ans.isSuccess());
    assertFalse(ans.isFailure());

    assertEquals("", ans.printExplanation());
  }

  @Test
  void testErrorExplanation() {
    Answer<String> ans = Answer.failed(new IllegalStateException("Something bad happened"));

    assertNotNull(ans.getExplanation());
    assertTrue(Abstract_Knowledge_Expression.sameAs(ans.getExplanation().getLevel()));

    assertFalse(ans.isSuccess());

    assertTrue(ans.printExplanation().contains("bad"));
  }

  @Test
  void testSimpleChaining() {
    Answer<String> ans = Answer.of("foo");

    Answer<Integer> iAns = ans
        .map(String::toUpperCase)
        .map(String::length);

    assertEquals(3, iAns.getOptionalValue().orElse(-1));
  }

  @Test
  void testChaining() {
    Answer<String> ans = Answer.of("foo");

    Answer<Integer> iAns = ans
        .flatMap((s) -> Answer.of(s.length()));

    assertEquals(3, iAns.getOptionalValue().orElse(-1));
  }

  @Test
  void testCode() {
    Answer<String> ans1 = Answer.of(202, "foo", Collections.emptyMap());
    Answer<String> ans2 = Answer.of(ResponseCodeSeries.OK.getTag(), "foo", Collections.emptyMap());
    Answer<String> ans3 = Answer.of(ResponseCodeSeries.OK, "foo", Collections.emptyMap());

    assertTrue(ans1.isSuccess());
    assertTrue(ans2.isSuccess());
    assertTrue(ans3.isSuccess());
  }


  @Test
  void testMeta() {
    String backLink = "http://goto.here/123";
    Map<String, List<String>> headers = new HashMap<>();
    headers.put("Link", Collections.singletonList(backLink));

    Answer<String> ans = Answer.of(ResponseCodeSeries.OK, "foo", headers);

    assertEquals(backLink, ans.getMeta("Link").orElse("META not found"));
  }

  @Test
  void testExplanationConstruction() {
    String msg = "This is the history";
    Answer<String> ans = Answer.of(ResponseCodeSeries.OK, "foo").withExplanationMessage(msg);
    assertEquals(msg, ans.printExplanation());
  }


  @Test
  void testWithKCarrier() {
    Answer<? extends KnowledgeCarrier> ans = Answer.of(AbstractCarrier.of("Foo"));

    ans = ans.map(
        kc -> AbstractCarrier.of(
            "mapped " + kc.asString().orElse("n/a")));

    KnowledgeCarrier kc = ans.orElse(null);
    assertNotNull(kc);
    assertEquals("mapped Foo", kc.asString().orElse("n/a"));
  }

  @Test
  void testNullableAnswers() {
    Answer<?> ans = Answer.ofNullable(null);
    assertTrue(ans.isFailure());

    Answer<?> ans2 = Answer.of(Optional.empty());
    assertTrue(ans2.isFailure());
  }

  @Test
  void testOrElseGet() {
    Answer<String> ans = Answer.of("aaa");
    assertEquals("aaa", ans.orElseGet(() -> "fail"));

    Answer<String> ans2 = Answer.failed();
    assertEquals("fail", ans2.orElseGet(() -> "fail"));

    Answer<String> ans3 = Answer.of("bbb");
    assertEquals("bbb", ans3.orElseGet(() -> {
      throw new RuntimeException();
    }));
  }


  @Test
  void testStreamReduce() {
    Answer<?> ans = Answer.of(Stream.of(2, 3, 4, 5));
    assertTrue(ans.isSuccess());

    Answer<Integer> ans2 = ans.reduce(Integer.class, (x, y) -> x * y);
    assertTrue(ans2.isSuccess());

    assertEquals(2 * 3 * 4 * 5, ans2.orElse(-1));
  }

  @Test
  void testListMap() {
    Answer<?> ans = Answer.of(Arrays.asList(2, 3, 4, 5));
    assertTrue(ans.isSuccess());

    Answer<List<Integer>> ans2 = ans.mapList(Integer.class, x -> x * 2);
    assertTrue(ans2.isSuccess());
    assertEquals(Arrays.asList(4, 6, 8, 10), ans2.orElse(Collections.emptyList()));

    Answer<List<Integer>> ans3 = ans.flatList(Integer.class, x -> Answer.of(x * 2));
    assertTrue(ans3.isSuccess());
    assertEquals(Arrays.asList(4, 6, 8, 10), ans3.orElse(Collections.emptyList()));
  }

  @Test
  void testForEeach() {
    Answer<?> ans = Answer.of(Arrays.asList(2, 3, 4, 5));
    assertTrue(ans.isSuccess());

    List<Integer> set = new ArrayList<>();
    ans.forEach(Integer.class, i -> set.add(-i));
    assertEquals(Arrays.asList(-2, -3, -4, -5), set);
  }

  @Test
  void testCollectToList() {
    List<Answer<Integer>> list = Arrays.asList(
        Answer.of(1),
        Answer.of(2)
    );

    Answer<List<Integer>> ans = list.stream().collect(Answer.toList());
    assertEquals(Arrays.asList(1, 2), ans.orElse(Collections.emptyList()));
  }

  @Test
  void testMergeWithExplanation() {
    String xpl = "X";

    Answer<Void> ans1 = Answer.of();
    Answer<Void> ans2 = Answer.of();
    Answer<Void> ans3 = Answer.of();

    Answer<Void> ans = merge(merge(ans1, ans2), ans3);

    assertTrue(ans.getExplanation().asString().isEmpty());

    Answer<Void> ans5 = Answer.of()
        .withExplanationMessage(xpl);

    ans = Answer.merge(ans, ans5);

    assertEquals(xpl, ans.getExplanation().asString().orElse(""));
    assertEquals(xpl, ans5.getExplanation().asString().orElse(""));

    assertTrue(ans1.getExplanation().asString().isEmpty());

  }

  @Test
  void testBiMap() {
    Answer<Integer> a1 = Answer.of(3);
    Answer<Double> a2 = Answer.of(2.0);
    Answer<Double> a3 = a1.biMap(a2, (x, y) -> x * y);

    assertTrue(a3.isSuccess());
    assertEquals(6.0, a3.get());
  }

  @Test
  void testBiMapFail1() {
    Answer<Integer> a1 = Answer.failed();
    Answer<Double> a2 = Answer.of(2.0);
    Answer<Double> a3 = a1.biMap(a2, (x, y) -> x * y);

    assertTrue(a3.isFailure());
  }

  @Test
  void testBiMapFail2() {
    Answer<Integer> a1 = Answer.of(3);
    Answer<Double> a2 = Answer.notFound();
    Answer<Double> a3 = a1.biMap(a2, (x, y) -> x * y);

    assertTrue(a3.isNotFound());
  }

  @Test
  void testBiMapFail3() {
    Answer<Integer> a1 = Answer.failed();
    Answer<Double> a2 = Answer.notFound();
    Answer<Double> a3 = a1.biMap(a2, (x, y) -> x * y);

    assertTrue(a3.isFailure());
    assertFalse(a3.isNotFound());
  }

  @Test
  void testFilterChain() {
    Answer<String> ans = Answer.of("abcdefg");

    boolean b1 = ans.filter(s -> s.contains("a"))
        .filter(s -> s.contains("b"))
        .isSuccess();
    assertTrue(b1);

    boolean b2 = ans.filter(s -> s.contains("z"))
        .filter(s -> s.contains("b"))
        .isSuccess();
    assertFalse(b2);

    boolean b3 = ans.filter(s -> s.contains("z"))
        .filter(s -> s.contains("w"))
        .isSuccess();
    assertFalse(b3);
  }

  @Test
  void testAnyDo() {
    String msg = "A did it";
    Answer<String> ans = Answer.anyDo(
        Arrays.asList("a", "b"),
        x -> {
          if ("a".equals(x)) {
            return Answer.of("a").withAddedExplanationMessage(msg);
          } else {
            return Answer.failed();
          }
        },
        () -> Answer.of("x")
    );
    boolean b = ans.isSuccess();
    String str = ans.get();
    String expl = ans.printExplanation();

    assertTrue(b);
    assertEquals("a", str);
    assertEquals(msg, expl);
    assertEquals(1, ans.getExplanation().components().count());
  }

  @Test
  void testNoneDo() {
    Answer<String> ans = Answer.anyDo(
        Arrays.asList("a", "b"),
        x -> {
          Answer<String> f = Answer.failed();
          f.withExplanationMessage("Unable to do " + x);
          return f;
        },
        () -> Answer.of("x").withExplanation("Compensation!")
    );
    boolean b = ans.isSuccess();
    String str = ans.get();
    String expl = ans.printExplanation();

    assertTrue(b);
    assertEquals("x", str);

    assertTrue(ans.getExplanation().componentList().size() >= 1);
    System.out.println(expl);
  }

}
