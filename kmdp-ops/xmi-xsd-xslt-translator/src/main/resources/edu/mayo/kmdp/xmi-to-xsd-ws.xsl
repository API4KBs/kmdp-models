<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:annox="http://annox.dev.java.net"
  xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
  xmlns:map="http://www.w3.org/2005/xpath-functions/map"
  xmlns:res="http://meta.kmdp.mayo.edu/Resource"
  xmlns:uml="http://www.eclipse.org/uml2/5.0.0/UML" xmlns:xmi2xsd="http://www.mayo.edu/kmdp/xmi2xsd"
  xsi:schemaLocation="http://www.w3.org/1999/XSL/Transform
                                    https://www.w3.org/TR/xslt-30/schema-for-xslt30.xsd"
  xmi:version="2.5" version="3.0">

  <xsl:import href="hrefResolver.xsl"/>

  <xsl:param name="targetFolder"/>
  <xsl:param name="includedPackages" as="xs:string?"/>
  <xsl:param name="targetNamespace" as="xs:string?" required="true"/>

  <xsl:variable name="pns" as="xs:string" select="'pns'"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="text() | @*"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Create schema element -->
  <xsl:template match="//uml:Model">
    <xsl:for-each
      select="
        //packagedElement[@xmi:type = 'uml:Package'
        and xmi2xsd:shouldIncludePackage(., $includedPackages)]">

      <xsl:if
        test="exists(root()//res:Resource) and
          ./packagedElement[(@xmi:type = 'uml:Class' or @xmi:type = 'uml:DataType')]">

        <xsl:variable name="path" select="xmi2xsd:packageToSchemaLocation(., (), '.openapi.xsd')"/>
        <xsl:result-document href="{concat(xmi2xsd:toFolderURI($targetFolder),$path)}" method="xml">
          <xsl:element name="xs:schema">
            <xsl:attribute name="elementFormDefault">qualified</xsl:attribute>
            <xsl:attribute name="attributeFormDefault">qualified</xsl:attribute>

            <!-- Declare Namespaces -->
            <xsl:namespace name="" select="concat(xmi2xsd:namespaceURI(.),'/resources')"/>
            <xsl:namespace name="annox">http://annox.dev.java.net</xsl:namespace>
            <xsl:namespace name="jaxb">http://java.sun.com/xml/ns/jaxb</xsl:namespace>
            <xsl:namespace name="xmi">http://www.omg.org/spec/XMI/20131001</xsl:namespace>
            <xsl:namespace name="{$pns}" select="xmi2xsd:namespaceURI(.)"/>

            <xsl:attribute name="targetNamespace" select="concat(xmi2xsd:namespaceURI(.),'/resources')"/>
            <xsl:attribute name="jaxb:extensionBindingPrefixes">annox</xsl:attribute>
            <xsl:attribute name="jaxb:version">2.1</xsl:attribute>

            <xsl:element name="xs:import">
              <xsl:attribute name="namespace">
                <xsl:value-of select="xmi2xsd:namespaceURI(.)"/>
              </xsl:attribute>
              <xsl:attribute name="schemaLocation">
                <xsl:value-of
                  select="xmi2xsd:makeRelative(xmi2xsd:packageToSchemaLocation(.,()),$path)"/>
              </xsl:attribute>
            </xsl:element>


            <xsl:call-template name="proxyTypes">
              <xsl:with-param name="root" select="."/>
            </xsl:call-template>

          </xsl:element>
        </xsl:result-document>
      </xsl:if>
    </xsl:for-each>

  </xsl:template>


  <!-- for each Class,
    create a named typed element, and a complex type -->
  <xsl:template name="proxyTypes">
    <xsl:param name="root" as="node()"/>

    <xsl:for-each
      select="
        $root/packagedElement[@xmi:type = 'uml:Class'
        and xmi2xsd:isRestResource(.)]">

      <xsl:element name="xs:element">

        <xsl:variable name="name"
          select="concat(lower-case(substring(@name, 1, 1)), substring(@name, 2))"/>
        <xsl:attribute name="name">
          <xsl:value-of select="$name"/>
        </xsl:attribute>

        <xsl:element name="xs:complexType">
          <xsl:element name="xs:annotation">
            <xsl:element name="xs:appinfo">
              <xsl:element name="annox:annotateClass">
                <xsl:value-of
                  select="concat('@javax.xml.bind.annotation.XmlRootElement(name=&quot;',$name,'&quot;, namespace=&quot;',xmi2xsd:namespaceURI(..),'&quot;)')"/>
              </xsl:element>
              <!--<xsl:element name="annox:annotate">
                <xsl:value-of select="concat('@javax.xml.bind.annotation.XmlType(name=&quot;',$name,'&quot;, namespace=&quot;',xmi2xsd:namespaceURI(..),'&quot;)')"/>
              </xsl:element>-->
            </xsl:element>
          </xsl:element>

          <xsl:element name="xs:complexContent">
            <xsl:element name="xs:extension">
              <xsl:attribute name="base" select="xmi2xsd:qualifiedName($pns,.,'')"/>
            </xsl:element>
            <!--<xsl:element name="xs:restriction">
              <xsl:attribute name="base" select="xmi2xsd:qualifiedName($pns,.,'')"/>
              <xsl:element name="xs:group">
                <xsl:attribute name="ref" select="xmi2xsd:contentGroupName(.,$pns)" />
              </xsl:element>
              <xsl:element name="xs:attributeGroup">
                <xsl:attribute name="ref" select="xmi2xsd:attrGroupName(.,$pns)" />
              </xsl:element>
            </xsl:element>-->
          </xsl:element>
        </xsl:element>
      </xsl:element>

    </xsl:for-each>
  </xsl:template>


</xsl:stylesheet>
