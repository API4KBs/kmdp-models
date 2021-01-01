package org.omg.spec.api4kp._20200801.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class TermGrouperTest {

  @Test
  void testGroupByTerm() {

    Term t1 = Term.newTerm(URI.create("urn:foo"),"x", "1.0.0");
    Term t2 = Term.newTerm(URI.create("urn:foo"),"x", "2.0.0");
    Term t3 = Term.newTerm(URI.create("urn:foo"),"y", "2.0.0");

    List<TermHolder> tList = Arrays.asList(
        new TermHolder(t1, "a"),
        new TermHolder(t2, "b"),
        new TermHolder(t3, "c")
    );

    Map<Term, List<TermHolder>> m1 = tList.stream()
        .collect(Collectors.groupingBy(TermHolder::getTerm));
    assertEquals(3, m1.size());
    m1.values().forEach(l -> assertEquals(1, l.size()));

    Map<Term, List<TermHolder>> m2 = tList.stream()
        .collect(Term.groupByConcept(TermHolder::getTerm));
    assertEquals(2, m2.size());
    m2.forEach((t,l) -> {
      if (t2.sameTermAs(t)) {
        assertEquals(2, l.size());
      } else if (t3.sameTermAs(t)) {
        assertEquals(1, l.size());
      } else {
        fail();
      }
    });
  }


  static class TermHolder {
    Term t;
    String s;

    public TermHolder(Term t, String s) {
      this.t = t;
      this.s = s;
    }

    public Term getTerm() {
      return t;
    }

    public String getLabel() {
      return s;
    }
  }
}
