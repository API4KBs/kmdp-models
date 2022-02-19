package edu.mayo.kmdp;

import static edu.mayo.kmdp.util.Util.isEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.omg.spec.api4kp._20200801.Explainer.GENERIC_ERROR_TYPE;
import static org.omg.spec.api4kp._20200801.Explainer.GENERIC_INFO_TYPE;
import static org.omg.spec.api4kp._20200801.Explainer.newOutcomeProblem;
import static org.omg.spec.api4kp._20200801.Severity.ERR;
import static org.omg.spec.api4kp._20200801.Severity.INF;
import static org.omg.spec.api4kp._20200801.Severity.WRN;
import static org.omg.spec.api4kp._20200801.taxonomy.knowledgeresourceoutcome.KnowledgeResourceOutcomeSeries.Syntactic_Profile_Conformance;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp._20200801.taxonomy.krformat.SerializationFormatSeries.XML_1_1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.AbstractCarrier;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.ComplexProblem;
import org.omg.spec.api4kp._20200801.Explainer;
import org.omg.spec.api4kp._20200801.ServerSideException;
import org.omg.spec.api4kp._20200801.Severity;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.services.KnowledgeCarrier;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;

class ExplanationTest {

  Exception e = new ServerSideException(ResponseCodeSeries.BadRequest);
  Date t0 = new Date();

  Answer<Void> ans1 = Answer.<Void>failed()
      .withExplanationMessage("Epic Fail");
  Answer<Void> ans2 = Answer.<Void>failed()
      .withFormalExplanation(AbstractCarrier.ofAst(t0));
  Answer<Void> ans3 = Answer.<Void>failed()
      .withExplanationDetail(Explainer.newOutcomeProblem(URI.create("my:Issue"), WRN)
          .withTitle("This is a Problem").build());
  Answer<Void> ans4 = Answer.<Void>failed()
      .withExplanationInterrupt(new IllegalStateException("Too bad"));
  Answer<Void> ans5 = Answer.<Void>failed()
      .withExplanationInterrupt(e);

  @Test
  void testStringExplanation() {
    assertNotNull(ans1.getExplanation());
    assertTrue(ans1.getExplanation().getExpression() instanceof String);

    String xpl = ans1.printExplanation();
    assertEquals("Epic Fail", xpl);

    String jsn = ans1.printExplanation(JSON);
    assertEquals(xpl, jsn);
  }

  @Test
  void testComplexExplanation() {
    assertNotNull(ans2.getExplanation());
    assertTrue(ans2.getExplanation().getExpression() instanceof Date);

    String xpl = ans2.printExplanation();
    assertEquals(t0.toString(), xpl);

    String jsn = ans2.printExplanation(JSON);
    assertEquals(JSonUtil.writeJsonAsString(t0).orElse(""), jsn);
  }

  @Test
  void testProblemExplanation() {
    assertNotNull(ans3.getExplanation());
    assertTrue(ans3.getExplanation().getExpression() instanceof DefaultProblem);

    String xpl = ans3.printExplanation();
    assertEquals(format(URI.create("my:Issue"), 200, null, "This is a Problem", WRN), xpl);

    String jsn = ans3.printExplanation(JSON);
    assertTrue(jsn.contains("title"));
    assertTrue(jsn.contains("Problem"));
    assertTrue(jsn.contains("500"));
  }

  @Test
  void testExceptionExplanation() {
    assertNotNull(ans4.getExplanation());
    assertTrue(ans4.getExplanation().getExpression() instanceof ServerSideException);

    String xpl = ans4.printExplanation();
    assertEquals(
        format(GENERIC_ERROR_TYPE, 500, "IllegalStateException", "Too bad", ERR),
        xpl);

    String jsn = ans4.printExplanation(JSON);
    assertTrue(jsn.contains("type"));
    assertTrue(jsn.contains("status"));
  }

  @Test
  void testProblemExceptionExplanation() {
    assertNotNull(ans5.getExplanation());
    assertSame(e, ans5.getExplanation().getExpression());

    String xpl = ans5.printExplanation();
    assertEquals(
        format(GENERIC_ERROR_TYPE, 400, "BadRequest", "BadRequest", ERR),
        xpl);

    String jsn = ans5.printExplanation(JSON);
    assertTrue(jsn.contains("400"));

    String xml = ans5.printExplanation(XML_1_1);
    assertTrue(xml.contains("<type>"));
    assertTrue(xml.contains("<status>"));
    assertFalse(xml.toLowerCase().contains("stacktrace"));
  }


  @Test
  void testMultipleExplanations() {
    Answer<Void> ans = Answer.merge(ans1, ans2);
    ans = Answer.merge(ans3, ans);

    JsonNode jn = JSonUtil.readJson(ans.printExplanation(JSON)).orElseGet(Assertions::fail);
    JsonNode n1 = jn.get("components");
    assertTrue(n1.isArray());

    ArrayNode an = (ArrayNode) n1;
    assertEquals(3, an.size());
    an.forEach(s -> assertTrue(s.isObject()));
  }

  @Test
  void testMultipleExplanationsWithReduce() {
    Answer<Void> all = Stream.of(ans1, ans2, ans3)
        .reduce(Answer::merge)
        .orElseGet(Assertions::fail);

    KnowledgeCarrier kc = all.getExplanation();
    assertEquals(3, kc.componentList().size());
  }

  @Test
  void testMultipleExplanationsWithNestedReduce() {
    Answer<Void> all1 = Stream.of(ans1, ans2)
        .reduce(Answer::merge)
        .orElseGet(Assertions::fail);
    Answer<Void> all2 = Stream.of(ans3, ans4)
        .reduce(Answer::merge)
        .orElseGet(Assertions::fail);
    Answer<Void> all3 = Stream.of(ans1, ans5)
        .reduce(Answer::merge)
        .orElseGet(Assertions::fail);

    Answer<Void> all = Stream.of(all1, all2, all3)
        .reduce(Answer::merge)
        .orElseGet(Assertions::fail);

    assertEquals(2, all1.getExplanation().componentList().size());
    assertEquals(2, all2.getExplanation().componentList().size());
    assertEquals(2, all3.getExplanation().componentList().size());
    assertEquals(6, all.getExplanation().componentList().size());
  }


  @Test
  void testNestedExplanation() {
    Answer<Void> ans = Answer.<Void>failed()
        .withExplanationDetail(
            Explainer.newOutcomeProblem(URI.create("my:Issue"), INF)
                .withTitle("Part1").build())
        .withAddedExplanationDetail(
            Explainer.newOutcomeProblem(URI.create("my:Issue"), INF)
                .withTitle("Part2").build());
    KnowledgeCarrier expl = ans.getExplanation();
    assertEquals(2, expl.components().count());

    String s = ans.printExplanation();
    String j = ans.printExplanation(JSON);
    assertFalse(isEmpty(s));
    assertFalse(isEmpty(j));
  }

  @Test
  void testExplanationDirectCast() {
    Answer<Void> ans = Answer.<Void>failed()
        .withExplanationDetail(
            Explainer.newOutcomeProblem(URI.create("my:Issue"), INF).build());

    assertTrue(ans.isFailure());
    Problem p = ans.getExplanationAs(Problem.class);
    assertNotNull(p);

    assertFalse(isEmpty(ans.printExplanation()));
    assertNull(ans.getExplanationAs(String.class));
  }


  @Test
  void testAutoFlattenExplanation() {
    Answer<Void> ans = Answer.<Void>succeed()
        .withExplanationDetail(
            Explainer.newOutcomeProblem(GENERIC_INFO_TYPE, INF)
                .withTitle("Part1").build())
        .withAddedExplanationDetail(
            Explainer.newOutcomeProblem(GENERIC_ERROR_TYPE, ERR)
                .withTitle("Part2").build());
    Problem expl = ans.getExplanationAsProblem();
    assertTrue(expl instanceof ComplexProblem);
    assertEquals(GENERIC_ERROR_TYPE, expl.getType());
  }


  @Test
  void testProblemMerge() {
    ResourceIdentifier rid = SemanticIdentifier.randomId();
    Answer<Void> ans = Answer.succeed()
        .withExplanationDetail(
            newOutcomeProblem(Syntactic_Profile_Conformance, ERR)
                .withInstance(rid.getVersionId())
                .withTitle("Part1").build())
        .withAddedExplanationDetail(
            newOutcomeProblem(Syntactic_Profile_Conformance, ERR)
                .withInstance(rid.getVersionId())
                .withTitle("Part2").build());
    Problem expl = ans.getExplanationAsProblem();
    assertTrue(expl instanceof ComplexProblem);
    assertEquals(Syntactic_Profile_Conformance.getReferentId(), expl.getType());
    assertEquals(ERR, expl.getParameters().get(Severity.KEY));
    assertEquals(rid.getVersionId(), expl.getInstance());
  }

  @Test
  void testPackExplanation() {
    URI uri = URI.create("my:Issue");
    Answer<Void> ans = Answer.<Void>failed()
        .withExplanationDetail(
            Explainer.newOutcomeProblem(uri, INF).build());

    Map<String, List<String>> mockHeaders = new HashMap<>();
    Explainer.packExplanationIntoHeaders(ans, mockHeaders);

    Answer<Void> rec = Answer.of(ans.getOutcomeType(), null, mockHeaders);
    assertNotNull(rec.getExplanation());
    assertEquals(uri, rec.getExplanationAsProblem().getType());
  }

  private String format(URI type, int code, String title, String msg, Severity severity) {
    return type
        + "{" + code
        + (title != null ? (", " + title) : "")
        + ", " + msg
        + (severity != null ? (", " + Severity.KEY + "=" + severity) : "")
        + "}";
  }

}
