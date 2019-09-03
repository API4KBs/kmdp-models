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
  xmlns:map="http://www.w3.org/2005/xpath-functions/map"
  xmlns:uml="http://www.eclipse.org/uml2/5.0.0/UML" xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
  xmlns:annox="http://annox.dev.java.net" xmlns:res="http://meta.kmdp.mayo.edu/Resource"
  xmlns:mvf="uri:urn:iso:11179-3" xmlns:xmi2xsd="http://www.mayo.edu/kmdp/xmi2xsd"
  xmlns:xmi2xjld="http://www.mayo.edu/kmdp/xmi2jxb-jsonld"
  xsi:schemaLocation="http://www.w3.org/1999/XSL/Transform
                                    https://www.w3.org/TR/xslt-30/schema-for-xslt30.xsd"
  xmi:version="2.5" version="3.0">

  <xsl:import href="hrefResolver.xsl"/>

  <xsl:param name="targetFolder"/>
  <xsl:param name="includedPackages" as="xs:string?"/>

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
      <xsl:if test="./packagedElement[@xmi:type = 'uml:Class' or @xmi:type = 'uml:DataType']">

        <xsl:variable name="path" select="xmi2xsd:packageToSchemaLocation(., (), '.xjb')"/>
        <xsl:result-document href="{concat(xmi2xsd:toFolderURI($targetFolder),$path)}" method="xml">

          <xsl:element name="jaxb:bindings">
            <xsl:namespace name="xjc" select="'http://java.sun.com/xml/ns/jaxb/xjc'"/>
            <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/>
            <xsl:namespace name="annox" select="'http://annox.dev.java.net'"/>
            <xsl:namespace name="xs" select="'http://www.w3.org/2001/XMLSchema'"/>

            <xsl:attribute name="xsi:schemaLocation"
              select="'http://java.sun.com/xml/ns/jaxb http://java.sun.com/xml/ns/jaxb/bindingschema_2_0.xsd'"/>
            <xsl:attribute name="version">2.1</xsl:attribute>
            <xsl:attribute name="jaxb:extensionBindingPrefixes">xjc annox</xsl:attribute>

            <xsl:element name="jaxb:bindings">
              <xsl:attribute name="schemaLocation" select="concat(@name, '.xsd')"/>
              <xsl:attribute name="node" select="'/xs:schema'"/>

              <xsl:call-template name="resourceTypes">
                <xsl:with-param name="pack" select="."/>
              </xsl:call-template>

            </xsl:element>
          </xsl:element>

        </xsl:result-document>
      </xsl:if>
    </xsl:for-each>

  </xsl:template>


  <xsl:template name="resourceTypes">
    <xsl:param name="pack"/>

    <xsl:for-each select="$pack/packagedElement[@xmi:type = 'uml:Class']">
      <xsl:variable name="klass" select="."/>
      <xsl:variable name="root" select="root($pack)"/>

      <xsl:variable name="info" select="xmi2xsd:getInternalTypeInfo($klass/@xmi:id, $klass)"/>

      <xsl:element name="jaxb:bindings">
        <xsl:attribute name="node"
          select="concat('xs:complexType[@name=''', map:get($info, 'name'), ''']')"/>

        <xsl:call-template name="identityInfo">
          <xsl:with-param name="pack" select="$pack"/>
          <xsl:with-param name="klass" select="$klass"/>
        </xsl:call-template>

        <xsl:call-template name="typeInfo">
          <!--<xsl:with-param name="pack" select="$pack"/-->
          <xsl:with-param name="klassName" select="$klass/@name"/>
        </xsl:call-template>

        <xsl:call-template name="subTypeInfo">
          <xsl:with-param name="childrenIds" select="map:get($info, 'childrenIds')"/>
          <xsl:with-param name="root" select="$root"/>
          <!--xsl:with-param name="klass" select="$klass"/>-->
        </xsl:call-template>

        <xsl:call-template name="semanticInfo">
          <xsl:with-param name="conceptUri" select="map:get($info, 'conceptUri')"/>
        </xsl:call-template>

        <xsl:call-template name="attrInfo">
          <!--<xsl:with-param name="pack" select="$pack"/>-->
          <xsl:with-param name="klass" select="$klass"/>
        </xsl:call-template>

        <xsl:call-template name="assocInfo">
          <!--<xsl:with-param name="pack" select="$pack"/>-->
          <xsl:with-param name="klass" select="$klass"/>
        </xsl:call-template>

      </xsl:element>

    </xsl:for-each>

  </xsl:template>


  <xsl:template name="identityInfo">
    <xsl:param name="pack" as="node()"/>
    <xsl:param name="klass" as="node()"/>

    <xsl:variable name="isResource" select="xmi2xjld:isRestResource($klass, $pack)"
      as="xs:boolean"/>
    <xsl:variable name="isConcept" select="exists(xmi2xsd:getConcept($klass))" as="xs:boolean"/>
    <xsl:variable name="entityIDAttribute" select="xmi2xsd:getResourceEntityIDAttribute($klass)"
      as="xs:string?"/>

    <xsl:choose>
      <xsl:when test="exists($entityIDAttribute)">
        <xsl:element name="annox:annotate">
          <xsl:attribute name="target">class</xsl:attribute>
          <xsl:value-of
            select="'@com.fasterxml.jackson.annotation.JsonIdentityInfo( generator = com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator.class )'"
          />
        </xsl:element>
        <xsl:element name="jaxb:bindings">
          <xsl:attribute name="node"
            select="concat('//xs:attributeGroup[@name=''', $klass/@name, '.attrs'']/xs:attribute[@name=''',$entityIDAttribute,''']')"/>
          <xsl:element name="annox:annotate">
            <xsl:attribute name="target">getter</xsl:attribute>
            <xsl:value-of select="'@com.fasterxml.jackson.annotation.JsonProperty(&quot;@id&quot;)'"
            />
          </xsl:element>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <!--<xsl:element name="annox:annotate">-->
        <!--<xsl:attribute name="target">class</xsl:attribute>-->
        <!--<xsl:value-of-->
        <!--select="'@com.fasterxml.jackson.annotation.JsonIdentityInfo( generator = com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator.class )'"-->
        <!--/>-->
        <!--</xsl:element>-->
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template name="typeInfo">
    <xsl:param name="klassName" as="xs:string"/>
    <xsl:element name="annox:annotate">
      <xsl:variable name="idType" select="'NAME'"/>
      <xsl:variable name="as" select="'PROPERTY'"/>

      <xsl:attribute name="target">class</xsl:attribute>
      <xsl:value-of
        select="
          concat('@com.fasterxml.jackson.annotation.JsonTypeInfo(',
          ' use = Id.', $idType, ', ',
          ' include = As.', $as, ', ',
          ' defaultImpl = ', $klassName, '.class, ',
          ' property = &quot;_class&quot; )')"
      />
    </xsl:element>
  </xsl:template>

  <xsl:template name="subTypeInfo">
    <xsl:param name="childrenIds" as="xs:string*"/>
    <xsl:param name="root" as="node()"/>
    <xsl:if test="exists($childrenIds)">

      <xsl:element name="annox:annotate">
        <xsl:attribute name="target">class</xsl:attribute>
        <xsl:value-of
          select="concat('@com.fasterxml.jackson.annotation.JsonSubTypes( {', xmi2xjld:subTypeInfos($childrenIds, $root), '})')"
        />
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template name="semanticInfo">
    <xsl:param name="conceptUri" as="xs:anyURI?"/>
    <xsl:if test="exists($conceptUri)">
      <xsl:element name="annox:annotate">
        <xsl:attribute name="target">class</xsl:attribute>
        <xsl:value-of
          select="concat('@de.escalon.hypermedia.hydra.mapping.Expose( &quot;', $conceptUri, '&quot; )')"
        />
      </xsl:element>
      <xsl:element name="annox:annotate">
        <xsl:attribute name="target">class</xsl:attribute>
        <xsl:value-of
          select="concat('@de.escalon.hypermedia.hydra.mapping.Vocab( &quot;', substring-before(xs:string($conceptUri), '#'), '#&quot; )')"
        />
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template name="attrInfo">
    <xsl:param name="klass" as="node()"/>
    <xsl:for-each select="ownedAttribute">
      <xsl:variable name="conceptUri" select="xmi2xsd:getConcept(.)"/>
      <xsl:if test="exists($conceptUri)">
        <xsl:element name="jaxb:bindings">
          <xsl:attribute name="node"
            select="concat('//xs:attributeGroup[@name=''', $klass/@name, '.attrs'']/xs:attribute[@name=''', @name, ''']')"/>
          <xsl:element name="annox:annotate">
            <xsl:attribute name="target">getter</xsl:attribute>
            <xsl:value-of
              select="concat('@de.escalon.hypermedia.hydra.mapping.Expose( &quot;', $conceptUri, '&quot; )')"
            />
          </xsl:element>
        </xsl:element>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="assocInfo">
    <xsl:param name="klass" as="node()"/>
    <xsl:variable name="this" select="$klass/@xmi:id"/>
    <xsl:for-each
      select="
        //packagedElement[@xmi:type = 'uml:Association' and @navigableOwnedEnd]">
      <xsl:variable name="navigs" select="tokenize(@navigableOwnedEnd, ' ')"/>

      <xsl:for-each
        select="
          ./ownedEnd[
          (preceding-sibling::ownedEnd/@type = $this
          or following-sibling::ownedEnd/@type = $this)
          and exists(index-of(@xmi:id, $navigs))]">

        <!-- Need '..' to go back from ownedEnd to the parent Association element -->
        <xsl:variable name="conceptUri" select="xmi2xsd:getConcept(..)"/>
        <xsl:if test="exists($conceptUri)">
          <xsl:element name="jaxb:bindings">
            <xsl:attribute name="node"
              select="concat('//xs:group[@name=''', $klass/@name, '.content'']//xs:element[@name=''', @name, ''']')"/>
            <xsl:element name="annox:annotate">
              <xsl:attribute name="target">getter</xsl:attribute>
              <xsl:value-of
                select="concat('@de.escalon.hypermedia.hydra.mapping.Expose( &quot;', $conceptUri, '&quot; )')"
              />
            </xsl:element>
          </xsl:element>
        </xsl:if>

        <!--<xsl:message select="concat('Checking association annos ', xmi2xsd:resolveType(.,@type))"/>-->
        <xsl:if test="exists(@type) and local-name-from-QName(xmi2xsd:resolveType(.,@type))='Map'">
          <xsl:element name="jaxb:bindings">
            <xsl:attribute name="node"
              select="concat('//xs:group[@name=''', $klass/@name, '.content'']//xs:element[@name=''', @name, ''']')"/>
            <xsl:element name="annox:annotate">
              <xsl:attribute name="target">field</xsl:attribute>
              <xsl:value-of
                select="'@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter( edu.mayo.kmdp.id.adapter.MapAdapter.class )'"
              />
            </xsl:element>
          </xsl:element>
        </xsl:if>

      </xsl:for-each>

    </xsl:for-each>

  </xsl:template>


  <xsl:function name="xmi2xjld:subTypeInfos" as="xs:string">
    <xsl:param name="childrenIds" as="xs:string*"/>
    <xsl:param name="root" as="node()"/>
    <xsl:variable name="children" as="xs:string*">
      <xsl:for-each select="$childrenIds">
        <xsl:variable name="childId" select="."/>
        <xsl:sequence select="xmi2xjld:subTypeInfo($childId, $root)"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:value-of select="string-join($children, ',')"/>
  </xsl:function>

  <xsl:function name="xmi2xjld:subTypeInfo" as="xs:string">
    <xsl:param name="childId" as="xs:string*"/>
    <xsl:param name="root" as="node()"/>
    <xsl:variable name="subKlassInfo"
      select="xmi2xsd:getInternalTypeInfo($childId, $root//node()[@xmi:id = $childId])"/>

    <xsl:value-of
      select="
        concat('@com.fasterxml.jackson.annotation.JsonSubTypes.Type(',
        ' name = &quot;', map:get($subKlassInfo, 'semName'), '&quot;, ',
        ' value = ', map:get($subKlassInfo, 'fqName'), '.class )')"
    />
  </xsl:function>


  <xsl:function name="xmi2xjld:isRestResource" as="xs:boolean">
    <xsl:param name="klass" as="node()"/>
    <xsl:param name="pack" as="node()"/>
    <xsl:variable name="doc" select="root($pack)"/>
    <xsl:variable name="id" select="$klass/@xmi:id"/>
    <xsl:variable name="rest" select="xmi2xsd:isResourcePackage($pack)" as="xs:boolean"/>

    <xsl:variable name="marker" select="exists($doc//res:Resource[@base_Class = $id])"/>

    <xsl:sequence select="$rest and $marker"/>
  </xsl:function>


</xsl:stylesheet>
