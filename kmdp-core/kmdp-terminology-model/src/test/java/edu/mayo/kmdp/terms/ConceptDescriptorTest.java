package edu.mayo.kmdp.terms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.id.ConceptIdentifier;
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

}
