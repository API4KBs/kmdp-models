/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.mayo.kmdp.util;

import static edu.mayo.kmdp.util.CatalogBasedURIResolver.catalogResolver;
import static edu.mayo.kmdp.util.URIUtil.parseQName;
import static edu.mayo.kmdp.util.Util.isEmpty;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.XMLConstants.XML_NS_URI;

import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.xslt.XSLTConfig;
import edu.mayo.kmdp.xslt.XSLTConfig.XSLTOptions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.catalog.CatalogResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.StandardErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class XMLUtil {

  private static final Logger logger = LoggerFactory.getLogger(XMLUtil.class);

  private XMLUtil() {
  }

  /**
   * Loads a Document from a URL, capturing exceptions
   *
   * @param source the URL from which the XML Document can be retrieved
   * @return a Document, if successful
   */
  public static Optional<Document> loadXMLDocument(URL source) {
    try {
      return loadXMLDocument(openXMLInputStream(source));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  /**
   * Opens a stream from a URL known to point to an XML document Ensures that http-based URLs are
   * resolved using the correct request headers.
   * <p>
   * The JDK default implementation may set the Accept header to text/html: if the server supports
   * content negotiation, it may provide an HTML stream derived from the XML instead of the XML data
   * itself
   *
   * @param source the URL pointing to an XML document
   * @return an Inpustream with the XML document content
   * @throws IOException error
   */
  private static InputStream openXMLInputStream(URL source) throws IOException {
    if (source.getProtocol().startsWith("http")) {
      var urlConnection = source.openConnection();
      HttpURLConnection httpConn = (HttpURLConnection) urlConnection;
      httpConn.setRequestProperty(
          "Accept",
          "application/xml, application/octet-stream;q=.9; */*;q=.2");
      try (var is = urlConnection.getInputStream();
          var baos = new ByteArrayOutputStream()) {
        Util.copyCompletely(is, baos);
        byte[] data = baos.toByteArray();
        return new ByteArrayInputStream(data);
      }
    } else {
      return source.openStream();
    }
  }

  /**
   * Loads a Document from a Byte Array, capturing exceptions
   *
   * @param source The serialized XML document
   * @return a Document, if successful
   */
  public static Optional<Document> loadXMLDocument(String source) {
    if (Util.isEmpty(source)) {
      return Optional.empty();
    }
    return loadXMLDocument(source.getBytes());
  }

  /**
   * Loads a Document from a Byte Array, capturing exceptions
   *
   * @param source The serialized XML document
   * @return a Document, if successful
   */
  public static Optional<Document> loadXMLDocument(byte[] source) {
    return loadXMLDocument(new ByteArrayInputStream(source));
  }

  /**
   * Loads a Document from a Stream, capturing exceptions
   *
   * @param source The stream carrying the XML Document
   * @return a Document, if successful
   */
  public static Optional<Document> loadXMLDocument(InputStream source) {
    try {
      DocumentBuilder builder = getSecureBuilder();
      Document dox = builder.parse(source);
      dox.normalizeDocument();
      return Optional.of(dox);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  /**
   * Streams a Document to an output stream
   *
   * @param dox          The Document to be serialized
   * @param outputStream the Stream into which to serialize the Document
   */
  public static void streamXMLDocument(Document dox, OutputStream outputStream) {
    try {
      removeEmptyNodes(dox.getDocumentElement());
      streamXMLNode(dox, outputStream);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  /**
   * Streams an XML Node to an output stream
   *
   * @param dox          The Node to be serialized
   * @param outputStream the Stream into which to serialize the Node
   */
  public static void streamXMLNode(Node dox, OutputStream outputStream) {
    try {
      var transformer = getSecureTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      var result = new StreamResult(new StringWriter());
      transformer.transform(new DOMSource(dox), result);
      outputStream.write(result.getWriter().toString().getBytes());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static byte[] toByteArray(Document dox) {
    if (dox == null) {
      return new byte[0];
    }
    var baos = new ByteArrayOutputStream();
    streamXMLDocument(dox, baos);
    return baos.toByteArray();
  }

  public static byte[] toByteArray(Node node) {
    if (node == null) {
      return new byte[0];
    }
    var baos = new ByteArrayOutputStream();
    streamXMLNode(node, baos);
    return baos.toByteArray();
  }

  public static String toString(Document dox) {
    return new String(toByteArray(dox));
  }

  public static String toString(Node node) {
    return new String(toByteArray(node));
  }

  /**
   * Cleanup : removes empty nodes from an XML document
   *
   * @param node The node to be pruned
   */
  public static void removeEmptyNodes(Node node) {
    if (node == null) {
      return;
    }
    var nodeList = node.getChildNodes();
    var i = 0;
    while (i < nodeList.getLength()) {
      var childNode = nodeList.item(i);
      if (childNode.getNodeType() == Node.TEXT_NODE && isEmpty(childNode.getNodeValue())) {
        childNode.getParentNode().removeChild(childNode);
      } else {
        i++;
      }
      removeEmptyNodes(childNode);
    }
  }

  /**
   * XSD Validation
   *
   * @param dox  The Document to validate
   * @param lang the URI of the schema / language / metamodel that provides an XSD schema to
   *             validate against (notice this is not the URI of the schema itself)
   * @return true if valid according to the schema, false otherwise
   */
  public static boolean validate(Document dox, URI lang) {
    return validate(new DOMSource(dox), lang);
  }

  public static boolean validate(Document dox, Schema schema) {
    return validate(new DOMSource(dox), schema);
  }

  /**
   * XSD Validation
   *
   * @param source A Source of the Document to validate
   * @param lang   the URI of the schema / language / metamodel that provides an XSD schema to
   *               validate against (notice this is not the URI of the schema itself)
   * @return true if valid according to the schema, false otherwise
   */
  public static boolean validate(Source source, URI lang) {
    return getSchemas(lang).map(schema -> {
          try {
            Optional<Validator> validator = getSecureValidator(schema);
            if (validator.isEmpty()) {
              return false;
            }
            validator.get().validate(source);
          } catch (SAXException | IOException e) {
            logger.error(e.getMessage(), e);
            return false;
          }
          return true;
        }
    ).orElse(false);
  }

  public static boolean validate(String source, Schema schema) {
    return validate(new StreamSource(new ByteArrayInputStream(source.getBytes())),
        schema);
  }

  public static boolean validate(Source source, Schema schema) {
    try {
      Optional<Validator> validator = getSecureValidator(schema);
      if (validator.isEmpty()) {
        return false;
      }
      validator.get().validate(source);
      return true;
    } catch (SAXException | IOException e) {
      logger.error(e.getMessage(), e);
      return false;
    }
  }

  /**
   * Loads known schemas
   *
   * @return A Schema for the language and its sublanguages
   */
  public static Optional<Schema> getSchemas(URI... langs) {
    var cat = catalogResolver(
        Arrays.stream(langs)
            .map(Registry::getCatalog)
            .flatMap(StreamUtil::trimStream)
            .map(XMLUtil.class::getResource)
            .map(URIUtil::asURI)
            .filter(Objects::nonNull)
            .toArray(URI[]::new));

    try {
      Optional<String> schemaBaseUrl = Registry.getValidationSchema(langs[0]);
      if (schemaBaseUrl.isEmpty()) {
        throw new IllegalStateException(
            "Defensive Programming: Unable to locate schema for language " + langs[0]);
      }
      String mainSchema = cat.resolve(schemaBaseUrl.get(), "").getSystemId();
      var url = new URL(mainSchema);
      return getSchemas(url, cat);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  public static Optional<Schema> getSchemas(
      final URL mainSchemaURL,
      final CatalogResolver catalogResolver) {
    return getSchemas(mainSchemaURL, new CatalogBasedURIResolver(catalogResolver));
  }

  public static Optional<Schema> getSchemas(
      final URL mainSchemaURL,
      final LSResourceResolver catalogResolver) {
    var sFactory = getSchemaFactory();
    sFactory.setResourceResolver(catalogResolver);

    return Optional.ofNullable(mainSchemaURL)
        .map(s -> {
          try {
            return sFactory.newSchema(s);
          } catch (SAXException e) {
            logger.warn(e.getMessage(), e);
            return null;
          }
        });
  }

  private static SchemaFactory getSchemaFactory() {
    return SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
  }


  /**
   * Utility: creates a Stream of Elements from a NodeList, to avoid the unnecessarily complex
   * iteration APIs
   * <p>
   * Assumes the NodeList is actually a list of XML Elements
   *
   * @param nodes the NodeList
   * @return a Stream of Element
   */
  public static Stream<Element> asElementStream(NodeList nodes) {
    return asElementStream(nodes, Element.class);
  }

  public static Stream<Attr> asAttributeStream(NodeList nodes) {
    return asElementStream(nodes, Attr.class);
  }

  private static <T> Stream<T> asElementStream(NodeList nodes, Class<T> type) {
    if (nodes == null) {
      return Stream.empty();
    }
    int numNodes = nodes.getLength();
    Collection<Node> nodeList = new ArrayList<>(numNodes);
    for (var j = 0; j < numNodes; j++) {
      nodeList.add(nodes.item(j));
    }
    return nodeList.stream()
        .filter(type::isInstance)
        .map(type::cast);
  }

  /**
   * Gets the prefix for a given namespace, as declared in the root element of an XML document
   *
   * @param dox       the Document
   * @param namespace the Namespace
   * @return the prefix for that namespace, as declared in the Document
   */
  public static String getPrefix(Document dox, String namespace) {
    NamedNodeMap map = dox.getDocumentElement().getAttributes();
    for (var j = 0; j < map.getLength(); j++) {
      var attr = (Attr) map.item(j);
      if (namespace.equals(attr.getValue())) {
        String xmlns = attr.getName();
        if ("xmlns".equals(xmlns)) {
          return "";
        }
        return xmlns.substring(xmlns.indexOf(':') + 1) + ":";
      }
    }
    return "";
  }

  public static Map<String, Document> applyXSLT(final InputStream source,
      final URL xslt,
      final XSLTConfig p) {
    return applyXSLT(source, null, xslt, p);
  }

  public static Map<String, Document> applyXSLT(final InputStream source,
      final String sourceSystemID,
      final URL xslt,
      final XSLTConfig p) {
    try {
      Source stylesheetSource = new StreamSource(xslt.openStream());
      Source inputSource = new StreamSource(source);
      inputSource.setSystemId(sourceSystemID);

      TransformerFactory factory = initFactory(xslt, p.getTyped(XSLTOptions.CATALOGS));

      var out = emptyDocument();
      var outputResult = new DOMResult(out);

      if (p.get(XSLTOptions.OUTPUT_RESOLVER).isPresent()) {
        var splitter = new XSLTSplitter(outputResult);
        factory.setAttribute(XSLTOptions.OUTPUT_RESOLVER.getName(), splitter);

        var transformer = factory.newTransformer(stylesheetSource);
        transformer.setErrorListener(new StandardErrorListener());
        applyProperties(transformer, p);

        transformer.transform(inputSource, outputResult);
        return splitter.getFragments();
      } else {
        var transformer = factory.newTransformer(stylesheetSource);
        applyProperties(transformer, p);

        transformer.transform(inputSource, outputResult);
        return Collections.singletonMap(sourceSystemID, (Document) outputResult.getNode());
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return Collections.emptyMap();
    }

  }

  private static void applyProperties(Transformer transformer, XSLTConfig p) {
    p.consume((k, v) -> transformer.setParameter(NameUtils.getTrailingPart(k), v));
  }

  public static String applyXSLTSimple(final URL source,
      final URL xslt,
      final XSLTConfig p) {
    try {
      return applyXSLTSimple(openXMLInputStream(source), xslt, source.toString(), p);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      return "";
    }
  }

  public static String applyXSLTSimple(final InputStream source,
      final URL xslt,
      final XSLTConfig p) {
    return applyXSLTSimple(source, xslt, null, p);
  }

  public static String applyXSLTSimple(final InputStream source,
      final URL xslt,
      String sourceSystemId,
      final XSLTConfig p) {
    try {
      Source stylesheetSource = new StreamSource(openXMLInputStream(xslt));
      Source inputSource = new StreamSource(source);
      if (sourceSystemId != null) {
        inputSource.setSystemId(sourceSystemId);
      }

      TransformerFactory factory = initFactory(null, p.getTyped(XSLTOptions.CATALOGS));
      var transformer = factory.newTransformer(stylesheetSource);

      p.get(XSLTOptions.CATALOGS).ifPresent(value ->
          transformer.setParameter(XSLTOptions.CATALOGS.name(), value));

      var baos = new ByteArrayOutputStream();
      var res = new StreamResult(baos);
      transformer.transform(inputSource, res);
      return baos.toString();

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return "";
    }

  }


  private static TransformerFactory initFactory(URL baseLocation, String catalogUrls)
      throws TransformerConfigurationException {
    var factory = getSecureTransformerFactory();

    factory.setURIResolver(new CatalogBasedURIResolver(baseLocation, catalogUrls));

    return factory;
  }

  public static URL asFileURL(String href) {
    try {
      var url = new URL(href);
      if (new File(url.toURI()).exists()) {
        return url;
      }
    } catch (Exception ignored) {
      // do nothing
    }
    return XMLUtil.class.getResource(href);
  }

  public static Optional<String> resolveXsiTypeClassName(final Element el) {
    var xsiType = el.getAttributeNodeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
        "type");
    var qName = parseQName(xsiType.getValue(), el);
    var pack = NameUtils.namespaceURIStringToPackage(qName.getNamespaceURI());
    String name = qName.getLocalPart();
    if (XML_NS_URI.equals(qName.getNamespaceURI())) {
      return Optional.empty();
    } else {
      return Optional.of(pack + "." + name);
    }
  }

  public static Document emptyDocument() {
    try {
      return getSecureBuilder().newDocument();
    } catch (ParserConfigurationException e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  public static DocumentBuilder getSecureBuilder() throws ParserConfigurationException {
    return getSecureDocumentBuilderFactory().newDocumentBuilder();
  }

  public static Transformer getSecureTransformer()
      throws TransformerConfigurationException {
    return getSecureTransformerFactory().newTransformer();
  }

  private static TransformerFactory getSecureTransformerFactory()
      throws TransformerConfigurationException {
    var factory = TransformerFactory.newInstance();
    factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
    factory.setFeature(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, true);
    return factory;
  }

  public static DocumentBuilderFactory getSecureDocumentBuilderFactory()
      throws ParserConfigurationException {
    var factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setNamespaceAware(true);
    return factory;
  }

  public static Optional<Validator> getSecureValidator(Schema schema) {
    try {
      var validator = schema.newValidator();
      validator.setProperty("http://javax.xml.XMLConstants/property/accessExternalSchema", "");
      validator.setProperty("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
      return Optional.of(validator);
    } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
      logger.error(e.getMessage(), e);
      return Optional.empty();
    }
  }


  /**
   * Utility that maps {@link Source} of various types to {@link InputSource}
   *
   * @param source the resource to get
   * From http://www.java2s.com/Tutorials/Java/XML/How_to_convert_Source_to_InputSource_using_Java.htm
   */
  public static InputSource sourceToInputSource(Source source) {
    if (source instanceof SAXSource) {
      return ((SAXSource) source).getInputSource();
    } else if (source instanceof DOMSource) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Node node = ((DOMSource) source).getNode();
      if (node instanceof Document) {
        node = ((Document) node).getDocumentElement();
      }
      Element domElement = (Element) node;
      elementToStream(domElement, baos);
      InputSource isource = new InputSource(source.getSystemId());
      isource.setByteStream(new ByteArrayInputStream(baos.toByteArray()));
      return isource;
    } else if (source instanceof StreamSource) {
      StreamSource ss = (StreamSource) source;
      InputSource isource = new InputSource(ss.getSystemId());
      isource.setByteStream(ss.getInputStream());
      isource.setCharacterStream(ss.getReader());
      isource.setPublicId(ss.getPublicId());
      return isource;
    } else {
      return getInputSourceFromURI(source.getSystemId());
    }
  }

  public static InputSource getInputSourceFromURI(String uri) {
    return new InputSource(uri);
  }

  public static void elementToStream(Element element, OutputStream out) {
    try {
      DOMSource source = new DOMSource(element);
      StreamResult result = new StreamResult(out);
      Transformer transformer = getSecureTransformer();
      transformer.transform(source, result);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }
}
