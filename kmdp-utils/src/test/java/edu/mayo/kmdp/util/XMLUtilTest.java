package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class XMLUtilTest {

  @Test
  void testXMLMigration() {
    var dox = XMLUtil.emptyDocument();
    assertNotNull(dox);

    var NS1 = "http://foo.bar";
    var NS2 = "http://foo.baz";
    var NS3 = "http://another.ns";

    var z = dox.createElementNS(NS1, "nodeZero");
    var a = dox.createElementNS(NS1, "nodeA");
    var b = dox.createElementNS(NS1, "nodeB");
    var c = dox.createElementNS(NS2, "nodeC");
    c.setAttributeNS(NS1, "attr", "attrVal");

    dox.appendChild(z);
    z.appendChild(a);
    a.appendChild(b);
    a.appendChild(c);

    XMLUtil.migrateNamespace(dox, a, NS1, NS3);

    assertEquals(NS1, z.getNamespaceURI());
    assertEquals(NS3, a.getNamespaceURI());
    assertEquals(NS3, b.getNamespaceURI());
    assertEquals(NS2, c.getNamespaceURI());
    assertEquals(NS3, c.getAttributeNode("attr").getNamespaceURI());
  }

}
