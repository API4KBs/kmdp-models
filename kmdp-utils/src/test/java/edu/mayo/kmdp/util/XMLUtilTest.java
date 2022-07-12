package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class XMLUtilTest {

  public static final String SRC_TEST_RESOURCES_XXE_DISALLOW_DOCTYPE_DECL_XML =
      "/xxe-disallow-doctype-decl.xml";

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
    documentBuilder.setEntityResolver(new CatalogBasedURIResolver());

    try (InputStream is = XMLUtilTest.class.getResourceAsStream(
        SRC_TEST_RESOURCES_XXE_DISALLOW_DOCTYPE_DECL_XML)) {

      Assertions.assertDoesNotThrow(() -> {
        var dox = documentBuilder.parse(is);
        var str = XMLUtil.toString(dox);
        assertTrue(str.contains("file that does contain a XXE attack"));
      });
    } catch (IOException e) {
      fail(e);
    }

  }

  /**
   * Demonstrate that a DocumentBuilderFactory configured from the
   * XMLUtil.getSecureDocumentBuilderFactory() is *not* vulnerable to an XXE attack (ie: the parser
   * will reject processing the malicious XML).
   */
  @Test
  void testGetSecureDocumentBuilderFactory()
      throws ParserConfigurationException, IOException, SAXException {

    // Use an instance of the DocumentBuilderFactory as configured by our Utils
    DocumentBuilderFactory documentBuilderFactory = XMLUtil.getSecureDocumentBuilderFactory();

    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

    try (InputStream is = XMLUtilTest.class.getResourceAsStream(
        SRC_TEST_RESOURCES_XXE_DISALLOW_DOCTYPE_DECL_XML)) {

      Exception exception = assertThrows(SAXParseException.class,
          () -> documentBuilder.parse(is));

      String messageActual = exception.getMessage();
      String messageExpected = "DOCTYPE is disallowed when the feature "
          + "\"http://apache.org/xml/features/disallow-doctype-decl\" set to true.";

      assertEquals(messageExpected, messageActual);
    } catch (IOException e) {
      fail(e);
    }

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

