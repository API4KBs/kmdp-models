package org.omg.spec.api4kp._1_0.id;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.JSonUtil;
import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.XMLUtil;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.Test;

public class SerializationTest {

  @Test
  void testIdentifiersJson() {
    ResourceIdentifier rid = SemanticIdentifier.newId(UUID.randomUUID(),"1");
    Optional<String> jsonStr = JSonUtil.writeJsonAsString(rid);
    assertTrue(jsonStr.isPresent());
  }

  @Test
  void testIdentifiersXml() {
    Pointer ptr = SemanticIdentifier.newIdAsPointer(UUID.randomUUID(),"1");
    assertFalse(Util.isEmpty(JaxbUtil.marshallToString(ptr)));
  }

  @Test
  void testFieldIdentifiersXml() {
    assertTrue(JaxbUtil.marshallToString(new Foo()).contains(Util.uuid("aaa").toString()));
  }

  @XmlRootElement
  public static class Foo {
    Pointer ptr = SemanticIdentifier.newId(Util.uuid("aaa"),"0").toInnerPointer();

    public Pointer getPtr() {
      return ptr;
    }

    public void setPtr(Pointer p) {
      this.ptr = p;
    }
  }
}
