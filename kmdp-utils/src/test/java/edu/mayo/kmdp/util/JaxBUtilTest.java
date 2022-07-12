package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.mayo.kmdp.util.XMLUtilTest.Customer;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JaxBUtilTest {

  public static final String SRC_TEST_RESOURCES_XXE_JAXB = "/xxe-customer.xml";
  public static final String XML_1 =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
          + "<customer>\n"
          + "    <age>5</age>\n"
          + "    <name>%s</name>\n"
          + "</customer>\n";

  public static final String XML_2 = "<?xml version=\"1.0\"?>\n"
      + "<!DOCTYPE customer\n"
      + "  [\n"
      + "    <!ENTITY name SYSTEM \"file:/foo.txt\">\n"
      + "    ]\n"
      + "  >\n"
      + "<customer>\n"
      + "  <age>5</age>\n"
      + "  <name>&name;</name>\n"
      + "</customer>";

  @Test
    // This test demonstrates that an out of the box
    // XMLInputFactory parses and does not reject the malicious XXE XML and parses it
  void testJAXBUnsafeOutOfTheBox() throws XMLStreamException, JAXBException {

    JAXBContext jaxbContext = JAXBContext.newInstance(Customer.class);

    // Note: using an out-of-the-box XMLInputFactory rather than kmdp-impl's securely configured JaxbUtil.getXXESafeXMLInputFactory()
    XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
    xmlInputFactory.setXMLResolver(new CatalogBasedURIResolver());

    XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(
        new StreamSource(getTestInputStream()));

    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    Customer customer = (Customer) unmarshaller.unmarshal(xmlStreamReader);

    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    marshaller.marshal(customer, byteArrayOutputStream);

    String xmlOutput = byteArrayOutputStream.toString();

    String xxInput = FileUtil.read(JaxBUtilTest.class.getResourceAsStream("/foo.txt"))
        .map(x -> String.format(XML_1, x))
        .orElseGet(Assertions::fail);

    assertEquals(xxInput, xmlOutput);

  }

  @Test
    // Demonstrate that using JaxbUtil.getXXESafeXMLInputFactory() prevents XXE because it
    // throws an UnmarshalException.
    // Based on https://stackoverflow.com/questions/12977299/prevent-xxe-attack-with-jaxb
  void testGetXXESafeXMLInputFactorySafeFromXXE() {

    Exception exception = assertThrows(UnmarshalException.class, () -> {

      JAXBContext jaxbContext = JAXBContext.newInstance(Customer.class);

      // NOTE: This is the key difference, using the safely configured JaxbUtil.getXXESafeXMLInputFactory() from kmdp-util
      XMLInputFactory xmlInputFactory = JaxbUtil.getXXESafeXMLInputFactory();

      XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(
          new StreamSource(getTestInputStream()));

      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      unmarshaller.unmarshal(xmlStreamReader);

      fail("Will throw before this point");

    });

    String messageActual = exception.toString();
    String messageFragmentExpected = "Undeclared general entity \"name\"";

    assertTrue(messageActual.contains(messageFragmentExpected));

  }

  @Test
  void testUseJaxbUtil() {
    String xmlData = FileUtil.read(getTestInputStream())
        .orElseGet(Assertions::fail);

    var tryParsed = JaxbUtil.unmarshall(Customer.class, Customer.class, xmlData);
    assertTrue(tryParsed.isEmpty());
  }

  private InputStream getTestInputStream() {
    return JaxBUtilTest.class.getResourceAsStream(SRC_TEST_RESOURCES_XXE_JAXB);
  }

}
