package org.omg.spec.api4kp._20200801.id;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class TermGrouperTest {

  @Test
  void testGroupByTerm() {

    Term t1 = Term.newTerm(URI.create("urn:foo"), "x", "1.0.0", "X");
    Term t2 = Term.newTerm(URI.create("urn:foo"), "x", "2.0.0", "Y");
    Term t3 = Term.newTerm(URI.create("urn:foo"), "y", "2.0.0", "Z");

    List<TermHolder> tList = asList(
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
    m2.forEach((t, l) -> {
      if (t2.sameTermAs(t)) {
        assertEquals(2, l.size());
      } else if (t3.sameTermAs(t)) {
        assertEquals(1, l.size());
      } else {
        fail();
      }
    });
  }

  @Test
  void testMultiGroupByTerm() {

    Term t1 = Term.newTerm(URI.create("urn:foo"), "x");
    Term t2 = Term.newTerm(URI.create("urn:foo"), "z");
    Term t3 = Term.newTerm(URI.create("urn:foo"), "y");
    Term t4 = Term.newTerm(URI.create("urn:foo"), "w");
    Term t5 = Term.newTerm(URI.create("urn:foo"), "t");

    List<TermHolder> tList = asList(
        new TermHolder("a", t1, t2),
        new TermHolder("b", t1, t2, t3),
        new TermHolder("c", t3),
        new TermHolder("d", t2, t3),
        new TermHolder("e", t5)
    );

    Map<Term, List<TermHolder>> m2 = tList.stream()
        .collect(Term.multiGroupByConcept(TermHolder::getTerms));
    assertEquals(4, m2.size());

    Set<String> s1 = m2.getOrDefault(t1, emptyList()).stream()
        .map(TermHolder::getLabel).collect(Collectors.toSet());
    Set<String> s2 = m2.getOrDefault(t2, emptyList()).stream()
        .map(TermHolder::getLabel).collect(Collectors.toSet());
    Set<String> s3 = m2.getOrDefault(t3, emptyList()).stream()
        .map(TermHolder::getLabel).collect(Collectors.toSet());
    Set<String> s4 = m2.getOrDefault(t4, emptyList()).stream()
        .map(TermHolder::getLabel).collect(Collectors.toSet());
    Set<String> s5 = m2.getOrDefault(t5, emptyList()).stream()
        .map(TermHolder::getLabel).collect(Collectors.toSet());

    assertEquals(new HashSet<>(asList("a", "b")), s1);
    assertEquals(new HashSet<>(asList("a", "b", "d")), s2);
    assertEquals(new HashSet<>(asList("b", "c", "d")), s3);
    assertEquals(new HashSet<>(), s4);
    assertEquals(new HashSet<>(singletonList("e")), s5);
  }

  static class TermHolder {

    List<Term> t;
    String s;

    public TermHolder(Term t, String s) {
      this.t = singletonList(t);
      this.s = s;
    }

    public TermHolder(String s, Term... t) {
      this.t = asList(t);
      this.s = s;
    }

    public Term getTerm() {
      return t.get(0);
    }

    public List<Term> getTerms() {
      return t;
    }

    public String getLabel() {
      return s;
    }
  }
}
