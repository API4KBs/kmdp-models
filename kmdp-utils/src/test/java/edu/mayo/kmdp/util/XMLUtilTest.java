package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class XMLUtilTest {

  public static final String SRC_TEST_RESOURCES_XXE_DISALLOW_DOCTYPE_DECL_XML = "src/test/resources/xxe-disallow-doctype-decl.xml";

  /**
   * Demonstrate that an out-of-the-box instance of the DocumentBuilderFactory is vulnerable to an
   * XXE attack (ie: the parser will fully process the malicious XML). This is the case that the
   * XMLUtil.getSecureDocumentBuilderFactory() configures the parser to protect against.
   */
  @Test
  void testGetSecureDocumentBuilderFactoryUnsafeOutOfTheBox()
      throws ParserConfigurationException {

    // Use an out-of-the-box instance of the DocumentBuilderFactory
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

    File file = new File(SRC_TEST_RESOURCES_XXE_DISALLOW_DOCTYPE_DECL_XML);

    Assertions.assertDoesNotThrow(() -> documentBuilder.parse(file));

  }

  /**
   * Demonstrate that a DocumentBuilderFactory configured from the XMLUtil.getSecureDocumentBuilderFactory()
   * is *not* vulnerable to an XXE attack (ie: the parser will reject processing the malicious
   * XML).
   */
  @Test
  void testGetSecureDocumentBuilderFactory()
      throws ParserConfigurationException, IOException, SAXException {

    // Use an instance of the DocumentBuilderFactory as configured by our Utils
    DocumentBuilderFactory documentBuilderFactory = XMLUtil.getSecureDocumentBuilderFactory();

    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

    File file = new File(SRC_TEST_RESOURCES_XXE_DISALLOW_DOCTYPE_DECL_XML);

    Exception exception = assertThrows(SAXParseException.class, () -> {
      Document document = documentBuilder.parse(file);
    });

    String messageActual = exception.getMessage();
    String messageExpected = "DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.";

    assertTrue(messageExpected.equals(messageActual));

  }

  @XmlRootElement
  public static class Customer {

    private String name;
    private int age;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

  }

}

