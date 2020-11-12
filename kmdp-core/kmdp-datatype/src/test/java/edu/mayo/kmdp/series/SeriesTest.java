package edu.mayo.kmdp.series;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.zafarkhaja.semver.Version;
import edu.mayo.kmdp.util.DateTimeUtil;
import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.Identifiable;
import org.omg.spec.api4kp._20200801.id.Identifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.VersionIdentifier;
import org.omg.spec.api4kp._20200801.series.SemVerSeries;
import org.omg.spec.api4kp._20200801.series.SemVersionable;

class SeriesTest {

  Person p;

  @BeforeEach
  void reinit() {
    p = new Person("John", 42);
    assertNotNull(p.evolve(43, p.getLatestVersion().incrementMajorVersion()));
    assertNotNull(p.evolve(45, p.getLatestVersion().incrementMajorVersion()));
  }

  @Test
  void testSnapshotCreation() {
    assertFalse(p.isEmpty());
    assertEquals(3, p.getVersions().size());
  }

  @Test
  void testVersionsIndex() {
    assertEquals(45, p.getLatest().getAge());
    assertEquals(45, p.getVersion(0).orElse(p.getLatest()).getAge());
    assertEquals(43, p.getVersion(1).orElse(p.getLatest()).getAge());
    assertEquals(42, p.getVersion(2).orElse(p.getLatest()).getAge());

    assertFalse(p.getVersion(-1).isPresent());
    assertFalse(p.getVersion(3).isPresent());
  }

  @Test
  void testVersionsIdentifiers() {
    assertTrue(p.getVersions().stream()
        .map(p -> p.getVersionIdentifier().getTag())
        .allMatch("John"::equals));

    assertEquals(Version.valueOf("2.0.0"), p.getLatestVersion());
    assertEquals("1.0.0",
        p.getVersion(1).orElse(p.getLatest()).getVersionIdentifier().getVersionTag());

    assertEquals(43,
        p.getVersion("1.0.0").orElse(p.getLatest()).getAge());

    assertEquals(42,
        p.getVersion("0.0.0").orElse(p.getLatest()).getAge());
  }


  @Test
  void testAddNewVersion() {
    PersonSnapshot ps = p.evolve(18,
        Version.valueOf("0.0.0-SNAPSHOT"),
        toDate("2001-01-01"));

    assertNotNull(ps);
    assertNotSame(ps, p.getLatest());
    assertSame(ps, p.getVersion(3).orElse(null));
  }

  @Test
  void testHistoryRollback() {
    p.evolve(18,
        Version.valueOf("0.0.0-a1"),
        toDate("2001-01-01"));

    p.evolve(22,
        Version.valueOf("0.0.0-a2"),
        toDate("2005-01-01"));

    assertFalse(p.asOf(toDate("1999-01-01")).isPresent());

    Optional<PersonSnapshot> p1 = p.asOf(toDate("2001-03-01"));
    assertTrue(p1.isPresent());
    assertEquals(18, p1.get().getAge());

    p1 = p.asOf(toDate("2007-03-01"));
    assertEquals(22, p1.map(PersonSnapshot::getAge).orElse(0));

    p1 = p.asOf(new Date());
    assertEquals(45, p1.map(PersonSnapshot::getAge).orElse(0));
  }

  @Test
  void testSameNess() {
    assertTrue(p.getLatest().ofSameAs(p.getVersion(2).orElse(null)));
    assertTrue(p.getLatest().isDifferentVersion(p.getVersion(2).orElse(null)));
  }

  @Test
  void testPresence() {
    assertFalse(p.isEmpty());
    assertTrue(p.hasVersion("0.0.0"));
    assertFalse(p.hasVersion("50.0.0"));
  }

  @Test
  void testVersionOrder() {
    PersonSnapshot px = p.evolve(25,
        Version.valueOf("1.0.4"),
        toDate("2005-01-01"));
    assertSame(px,p.getVersion(3).orElse(null));

    List<PersonSnapshot> sorted = p.sortedByVersion();
    assertNotNull(sorted);
    assertSame(px,sorted.get(1));
  }


  public static class Person implements SemVerSeries<PersonSnapshot, Person>, Identifiable {

    ResourceIdentifier seriesId;

    private final List<PersonSnapshot> versions = new LinkedList<>();

    Person(String name, int age) {
      seriesId = SemanticIdentifier.newId(name);
      addVersion(
          new PersonSnapshot(name, age),
          newIdentifier(name));
    }

    PersonSnapshot evolve(int newAge, Version newVersionTag) {
      return this.evolve(newAge, newVersionTag, new Date());
    }

    PersonSnapshot evolve(int newAge, Version newVersionTag, Date date) {
      return evolve(p -> p.withAge(newAge), newVersionTag.toString(), date);
    }

    public List<PersonSnapshot> getVersions() {
      return versions;
    }

    @Override
    public Identifier getIdentifier() {
      return seriesId;
    }

    @Override
    public URI getResourceId() {
      return getIdentifier().getResourceId();
    }
  }


  public static class PersonSnapshot implements SemVersionable<PersonSnapshot, Person> {

    private ResourceIdentifier id;
    private int age;
    private final String name;

    private PersonSnapshot(String name, int age) {
      this.name = name;
      this.age = age;
    }

    private PersonSnapshot(PersonSnapshot other) {
      this.name = other.name;
      this.age = other.age;
    }

    @Override
    public void dub(VersionIdentifier identifier) {
      SemVersionable.super.dub(identifier);
      this.id = (ResourceIdentifier) identifier;
    }

    @Override
    public ResourceIdentifier getVersionIdentifier() {
      return id;
    }

    public int getAge() {
      return age;
    }

    public PersonSnapshot withAge(int age) {
      this.age = age;
      return this;
    }

    public String getName() {
      return name;
    }

    @Override
    public PersonSnapshot snapshot() {
      return new PersonSnapshot(this);
    }

    @Override
    public Date getVersionEstablishedOn() {
      return getVersionIdentifier().getEstablishedOn();
    }

    @Override
    public String toString() {
      return name + "@" + age + " :: " + getVersionIdentifier().getVersionTag() + " - "
          + getVersionIdentifier().getEstablishedOn();
    }

    @Override
    public Identifier getIdentifier() {
      return getVersionIdentifier();
    }
  }


  private Date toDate(String s) {
    return DateTimeUtil.parseDate(s);
  }

}
