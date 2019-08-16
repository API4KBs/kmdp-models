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

import edu.mayo.kmdp.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class XPathUtil {

  private static Logger logger = LogManager.getLogger(XPathUtil.class);

  private static NamespaceContext ctx = new NamespaceContext() {
    @Override
    public String getNamespaceURI(String prefix) {
      return Registry.getNamespaceURIForPrefix(prefix).orElse("");
    }

    @Override
    public String getPrefix(String namespaceURI) {
      return Registry.getPrefixforNamespace(namespaceURI).orElse("");
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
      return Registry.getPrefixforNamespace(namespaceURI)
          .map(Collections::singleton)
          .orElse(Collections.emptySet()).iterator();
    }
  };

  private XPath defaultXPath;

  public XPathUtil() {
    XPathFactory factory = XPathFactory.newInstance();
    defaultXPath = factory.newXPath();
    defaultXPath.setNamespaceContext(ctx);
  }

  private static Object evaluateXPath(XPath xpath, Document dox, String xpathExpression,
      QName type) {
    try {
      Object result = xpath.evaluate(xpathExpression, dox.getDocumentElement(), type);
      xpath.reset();

      return result;
    } catch (Exception e) {
      logger.error(e.getMessage(),e);
    }
    return null;
  }

  public Node xNode(XPath xpath, Document dox, String xpathExpression) {
    return (Node) evaluateXPath(xpath, dox, xpathExpression, XPathConstants.NODE);
  }

  public Node xNode(Document dox, String xpathExpression) {
    return (Node) evaluateXPath(defaultXPath, dox, xpathExpression, XPathConstants.NODE);
  }

  public NodeList xList(XPath xpath, Document dox, String xpathExpression) {
    return (NodeList) evaluateXPath(xpath, dox, xpathExpression, XPathConstants.NODESET);
  }

  public NodeList xList(Document dox, String xpathExpression) {
    return (NodeList) evaluateXPath(defaultXPath, dox, xpathExpression, XPathConstants.NODESET);
  }

  public String xString(XPath xpath, Document dox, String xpathExpression) {
    return (String) evaluateXPath(xpath, dox, xpathExpression, XPathConstants.STRING);
  }

  public String xString(Document dox, String xpathExpression) {
    return (String) evaluateXPath(defaultXPath, dox, xpathExpression, XPathConstants.STRING);
  }

  public Double xNumber(XPath xpath, Document dox, String xpathExpression) {
    return (Double) evaluateXPath(xpath, dox, xpathExpression, XPathConstants.NUMBER);
  }

  public Double xNumber(Document dox, String xpathExpression) {
    return (Double) evaluateXPath(defaultXPath, dox, xpathExpression, XPathConstants.NUMBER);
  }

  public Boolean xBool(XPath xpath, Document dox, String xpathExpression) {
    return (Boolean) evaluateXPath(xpath, dox, xpathExpression, XPathConstants.BOOLEAN);
  }

  public Boolean xBool(Document dox, String xpathExpression) {
    return (Boolean) evaluateXPath(defaultXPath, dox, xpathExpression, XPathConstants.BOOLEAN);
  }

  public Object attr(Node n, String attribName) {
    Node att = n.getAttributes().getNamedItem(attribName);
    return att != null ? att.getNodeValue() : null;
  }

  public List<Node> children(Node node, String elName) {
    return XMLUtil.asElementStream(node.getChildNodes())
        .filter(el -> elName.equals(el.getNodeName()))
        .collect(Collectors.toList());
  }
}
