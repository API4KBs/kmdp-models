package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.id.SemanticIdentifier;
import org.omg.spec.api4kp._20200801.id.Term;
import org.omg.spec.api4kp._20200801.terms.ConceptTerm;
import org.omg.spec.api4kp._20200801.terms.model.ConceptDescriptor;

class ConceptDescriptorTest {

  @Test
  void testIdentifierDescriptorRoundtrip() {
    Term t = Term.mock("foo", "bar");
    ConceptIdentifier cid = t.asConceptIdentifier();

    assertNotNull(cid.getReferentId());
    assertNotNull(cid.getConceptId());
    assertNotNull(cid.getVersionId());
    assertNotNull(cid.getEstablishedOn());
    assertNotNull(cid.getLabel());
    assertNotNull(cid.getNamespaceUri());
    assertNotNull(cid.getResourceId());
    assertNotNull(cid.getUuid());
    assertNotNull(cid.getTag());
    assertNotNull(cid.getVersionTag());

    ConceptDescriptor cd = ConceptDescriptor.toConceptDescriptor(cid);
    ConceptIdentifier cid2 = cd.toConceptIdentifier();
    assertEquals(cid, cid2);
  }

  @Test
  void testNS() {
    ResourceIdentifier id = SemanticIdentifier.newNamespaceId(URI.create("https://foo.org/child/124"));
    id.getResourceId();
  }

  @Test
  void testNamespaceGeneration() {
    Term t = Term.mock("foo", "bar");
    ConceptIdentifier cid = t.asConceptIdentifier();

    String ns = cid.getNamespaceUri().toString();
    assertEquals(
        t.getResourceId().toString(),
        ns + t.getTag()
        );
  }

}
