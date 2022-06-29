package edu.mayo.kmdp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.mayo.kmdp.util.XMLUtilTest.Customer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.Optional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

class JaxBUtilTest {

  public static final String SRC_TEST_RESOURCES_XXE_JAXB = "src/test/resources/xxe-customer.xml";
  public static final String XML_1 =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
          + "<customer>\n"
          + "    <age>5</age>\n"
          + "    <name>\n"
          + "</name>\n"
          + "</customer>\n";

  public static final String XML_2 = "<?xml version=\"1.0\"?>\n"
      + "<!DOCTYPE customer\n"
      + "  [\n"
      + "    <!ENTITY name SYSTEM \"/Users/m212350/dev/kmdp-impl/kmdp-utils/pom.xml\">\n"
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

    XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StreamSource(SRC_TEST_RESOURCES_XXE_JAXB));

    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    Customer customer = (Customer) unmarshaller.unmarshal(xmlStreamReader);

    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    marshaller.marshal(customer, byteArrayOutputStream);

    String xmlOutput = new String(byteArrayOutputStream.toByteArray());

    assertEquals(xmlOutput, XML_1);

  }

  @Test
  // Demonstrate that using JaxbUtil.getXXESafeXMLInputFactory() prevents XXE because it
  // throws an UnmarshalException.
  // Based on https://stackoverflow.com/questions/12977299/prevent-xxe-attack-with-jaxb
  void testGetXXESafeXMLInputFactorySafeFromXXE() throws XMLStreamException, JAXBException {

    Exception exception = assertThrows(UnmarshalException.class, () -> {

      JAXBContext jaxbContext = JAXBContext.newInstance(Customer.class);

      // NOTE: This is the key difference, using the safely configured JaxbUtil.getXXESafeXMLInputFactory() from kmdp-util
      XMLInputFactory xmlInputFactory = JaxbUtil.getXXESafeXMLInputFactory();

      InputStream targetStream = new ByteArrayInputStream(XML_2.getBytes());

      XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StreamSource(SRC_TEST_RESOURCES_XXE_JAXB));

      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      Customer customer = (Customer) unmarshaller.unmarshal(xmlStreamReader);

      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(customer, System.out);

    });

    String messageActual = exception.toString();
    String messageFragmentExpected = "Undeclared general entity \"name\"";

    assertTrue(messageActual.contains(messageFragmentExpected));

  }

  @Test
  void testExploration() throws IOException, TransformerException {

//      String xml = Files.readString(Paths.get(SRC_TEST_RESOURCES_XXE_JAXB));
//      Document document = loadXMLFrom(xml);

      Document document = loadXMLFrom(XML_2);

      // TODO: This test should *FAIL* because it uses XML that contains an XXE attack (through the use of &name;)
      // Why this test succeeds and does not fail is under investigation

      Optional<Customer> customerOptional = JaxbUtil.unmarshall(Collections.singleton(Customer.class), Customer.class, document);
      Customer customer = customerOptional.get();

  }

  protected static Document loadXMLFrom(String xml) throws TransformerException {
    Source source = new StreamSource(new StringReader(xml));
    DOMResult result = new DOMResult();
    TransformerFactory.newInstance().newTransformer().transform(source , result);
    return (Document) result.getNode();
  }

  private static Document convertStringToXMLDocument(String xmlString)
  {
    //Parser that produces DOM object trees from XML content
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    //API to obtain DOM Document instance
    DocumentBuilder builder = null;
    try
    {
      //Create DocumentBuilder with default configuration
      builder = factory.newDocumentBuilder();

      //Parse the content to Document object
      Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
      return doc;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

}
