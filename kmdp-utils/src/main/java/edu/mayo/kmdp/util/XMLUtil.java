/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.util;

import static edu.mayo.kmdp.util.URIUtil.parseQName;
import static edu.mayo.kmdp.util.Util.isEmpty;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.XMLConstants.XML_NS_URI;

import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.schemas.CatalogResourceResolver;
import edu.mayo.kmdp.xslt.XSLTConfig;
import edu.mayo.kmdp.xslt.XSLTConfig.XSLTOptions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.xerces.util.XMLCatalogResolver;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLUtil {


  /**
   * Loads a Document from a URL, capturing exceptions
   * @param source
   * @return
   */
  public static Optional<Document> loadXMLDocument(URL source) {
    try {
      return loadXMLDocument(source.openStream());
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  /**
   * Loads a Document from a Byte Array, capturing exceptions
   * @param source
   * @return
   */
  public static Optional<Document> loadXMLDocument(byte[] source) {
    return loadXMLDocument(new ByteArrayInputStream(source));
  }

  /**
   * Loads a Document from a Stream, capturing exceptions
   * @param source
   * @return
   */
  public static Optional<Document> loadXMLDocument(InputStream source) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document dox = builder.parse(source);
      return Optional.of(dox);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  /**
   * Streams a Document to an output stream
   *
   * TODO FIXME: Should accept Properties
   * @param dox
   * @param outputStream
   */
  public static void streamXMLDocument(Document dox, OutputStream outputStream) {
    try {
      removeEmptyNodes(dox.getDocumentElement());
      streamXMLNode(dox, outputStream);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Streams a Document to an output stream
   *
   * TODO FIXME: Should accept Properties
   * @param dox
   * @param outputStream
   */
  public static void streamXMLNode(Node dox, OutputStream outputStream) {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      StreamResult result = new StreamResult(new StringWriter());
      transformer.transform(new DOMSource(dox), result);
      outputStream.write(result.getWriter().toString().getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public static byte[] toByteArray(Document dox) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    streamXMLDocument(dox, baos);
    return baos.toByteArray();
  }

  public static byte[] toByteArray(Node node) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    streamXMLNode(node, baos);
    return baos.toByteArray();
  }


  /**
   * Cleanup : removes empty nodes from an XML document
   * @param node
   */
  private static void removeEmptyNodes(Node node) {
    if (node == null) {
      return;
    }
    NodeList nodeList = node.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node childNode = nodeList.item(i);
      if (childNode.getNodeType() == Node.TEXT_NODE && isEmpty(childNode.getNodeValue())) {
        childNode.getParentNode().removeChild(childNode);
        i--;
      }
      removeEmptyNodes(childNode);
    }
  }

  /**
   * XSD Validation
   * @param dox
   * @return
   */
  public static boolean validate(Document dox, URI lang) {
    return validate(new DOMSource(dox), lang);
  }

  public static boolean validate(Document dox, Schema schema) {
    return validate(new DOMSource(dox), schema);
  }

  /**
   * XSD Validation
   * @param source
   * @return
   */
  public static boolean validate(Source source, URI lang) {
    return getSchemas(lang).map((schema) -> {
          Validator validator = schema.newValidator();
          try {
            validator.validate(source);
          } catch (SAXException | IOException e) {
            e.printStackTrace();
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
    Validator validator = schema.newValidator();
    try {
      validator.validate(source);
      return true;
    } catch (SAXException | IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Loads known schemas
   * @return
   */

  // Now let's do this in Java 8 using FlatMap List<String> flatMapList = playersInWorldCup2016.stream() .flatMap(pList -> pList.stream()) .collect(Collectors.toList());
  public static Optional<Schema> getSchemas(URI... langs) {
    XMLCatalogResolver cat = catalogResolver(
        Arrays.stream(langs)
            .map(Registry::getCatalog)
            .flatMap(Util::trimStream)
            .map(XMLUtil.class::getResource)
            .toArray(URL[]::new));

    try {
      Optional<String> schemaBaseUrl = Registry.getValidationSchema(langs[0]);
      if (!schemaBaseUrl.isPresent()) {
        throw new IllegalStateException(
            "Defensive Programming: Unable to locate schema for language " + langs[0]);
      }
      String mainSchema = cat.resolveURI(schemaBaseUrl.get());
      URL url = new URL(mainSchema);
      return getSchemas(url, cat);
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public static XMLCatalogResolver catalogResolver(URL... catalogs) {
    return new XMLCatalogResolver(Arrays.stream(catalogs)
        .map(URL::toString).toArray(String[]::new));
  }

  public static XMLCatalogResolver catalogResolver(String... catalogRelativePaths) {
    return new XMLCatalogResolver(Arrays.stream(catalogRelativePaths)
        .map(XMLUtil::asFileURL)
        .map(URL::toString)
        .toArray(String[]::new));
  }

  public static Optional<Schema> getSchemas(final URL mainSchemaURL,
      final XMLCatalogResolver catalogResolver) {
    SchemaFactory sFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
    sFactory.setResourceResolver(new CatalogResourceResolver(catalogResolver));

    try {
      return Optional.ofNullable(sFactory.newSchema(mainSchemaURL));
    } catch (SAXException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }


  private static Optional<Schema> getSchemasFromStreams(List<InputStream> schemas) {
    return getSchemasFromStreams(schemas, null);
  }

  private static Optional<Schema> getSchemasFromStreams(List<InputStream> schemas,
      final CatalogResourceResolver resolver) {

    SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);

    if (resolver != null) {
      schemaFactory.setResourceResolver(resolver);
    }

    List<Source> sources = schemas.stream()
        .map(StreamSource::new)
        .collect(Collectors.toList());

    try {
      return Optional.of(schemaFactory.newSchema(sources.toArray(new Source[sources.size()])));
    } catch (SAXException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }


  /**
   * Utility: creates a Stream of Elements from a NodeList,
   * to avoid the clunky iteration
   *
   * Assumes the NodeList is actually a list of XML Elements
   * @param nodes
   * @return
   */
  public static Stream<Element> asElementStream(NodeList nodes) {
    int N = nodes.getLength();
    Collection<Node> nodeList = new ArrayList<>(N);
    for (int j = 0; j < N; j++) {
      nodeList.add(nodes.item(j));
    }
    return nodeList.stream()
        .filter(Element.class::isInstance)
        .map(Element.class::cast);
  }

  public static Stream<Attr> asAttributeStream(NodeList nodes) {
    int N = nodes.getLength();
    Collection<Node> nodeList = new ArrayList<>(N);
    for (int j = 0; j < N; j++) {
      nodeList.add(nodes.item(j));
    }
    return nodeList.stream()
        .filter(Attr.class::isInstance)
        .map(Attr.class::cast);
  }


  /**
   * Gets the prefix for a given namespace, as declared in the root element of an XML document
   * @param dox
   * @param namespace
   * @return
   */
  public static String getPrefix(Document dox, String namespace) {
    NamedNodeMap map = dox.getDocumentElement().getAttributes();
    for (int j = 0; j < map.getLength(); j++) {
      Attr attr = (Attr) map.item(j);
      if (namespace.equals(attr.getValue())) {
        String xmlns = attr.getName();
        if ("xmlns".equals(xmlns)) {
          return "";
        }
        return xmlns.substring(xmlns.indexOf(":") + 1) + ":";
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

      TransformerFactory factory = initFactory(xslt.toString(), p.getTyped(XSLTOptions.CATALOGS));

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      Document out = dbf.newDocumentBuilder()
          .newDocument();
      DOMResult outputResult = new DOMResult(out);

      if (p.get(XSLTOptions.OUTPUT_RESOLVER).isPresent()) {
        XSLTSplitter splitter = new XSLTSplitter(outputResult);
        factory.setAttribute(XSLTOptions.OUTPUT_RESOLVER.getName(), splitter);

        Transformer transformer = factory.newTransformer(stylesheetSource);
        applyProperties(transformer, p);

        transformer.transform(inputSource, outputResult);
        return splitter.getFragments();
      } else {
        Transformer transformer = factory.newTransformer(stylesheetSource);
        applyProperties(transformer, p);

        transformer.transform(inputSource, outputResult);
        return Collections.singletonMap(sourceSystemID, (Document) outputResult.getNode());
      }

    } catch (Exception e) {
      e.printStackTrace();
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
      return applyXSLTSimple(source.openStream(), xslt, source.toString(), p);
    } catch (IOException e) {
      e.printStackTrace();
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
      Source stylesheetSource = new StreamSource(xslt.openStream());
      Source inputSource = new StreamSource(source);
      if (sourceSystemId != null) {
        inputSource.setSystemId(sourceSystemId);
      }

      TransformerFactory factory = initFactory(null, p.getTyped(XSLTOptions.CATALOGS));
      Transformer transformer = factory.newTransformer(stylesheetSource);

      p.get(XSLTOptions.CATALOGS).ifPresent((value) ->
          transformer.setParameter(XSLTOptions.CATALOGS.name(), value));

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      StreamResult res = new StreamResult(baos);
      transformer.transform(inputSource, res);
      return new String(baos.toByteArray());

    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }

  }


  private static TransformerFactory initFactory(String loc, String catalog) {
    TransformerFactory factory = TransformerFactory.newInstance();

    if (catalog != null) {
      factory.setURIResolver(new CatalogBasedURIResolver(catalog.split(",")).withLoc(loc));
    } else {
      factory.setURIResolver(new RelativeFileURIResolver().withLoc(loc));
    }

    return factory;
  }

  public static URL asFileURL(String href) {
    try {
      URL url = new URL(href);
      if (new File(url.toURI()).exists()) {
        return url;
      }
    } catch (Exception ignored) {
    }
    return XMLUtil.class.getResource(href);
  }

  public static Optional<String> resolveXsiTypeClassName(final Element el) {
    Attr xsiType = el.getAttributeNodeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
        "type");
    QName qName = parseQName(xsiType.getValue(), el);
    String pack = NameUtils.namespaceURIToPackage(qName.getNamespaceURI());
    String name = qName.getLocalPart();
    if (XML_NS_URI.equals(qName.getNamespaceURI())) {
      return Optional.empty();
    } else {
      return Optional.of(pack + "." + name);
    }
  }

  public static Document emptyDocument() {
    try {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      return null;
    }
  }
}
