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
package edu.mayo.kmdp.terms.example;


import de.escalon.hypermedia.hydra.mapping.Expose;
import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.Taxonomic;
import edu.mayo.kmdp.terms.TermsXMLAdapter;
import edu.mayo.kmdp.terms.impl.model.DefaultConceptScheme;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

import java.net.URI;

/*
	Example of generated 'terminology' class
*
* */
@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(SCH1.Adapter.class)
public enum SCH1 implements Term, Taxonomic<SCH1> {

  @Expose("http://test/generator#specific_concept")
  Specific_Concept("6789", "specific_concept", "http://test/generator#specific_concept",
      new SCH1[0],
      new SCH1[0]),
  @Expose("http://test/generator#nested_specific_concept")
  Nested_Specific_Concept("12345", "nested_specific_concept",
      "http://test/generator#nested_specific_concept",
      new SCH1[]{Specific_Concept},
      new SCH1[]{Specific_Concept}),
  @Expose("http://test/generator#sub_sub_concept")
  Sub_Sub_Concept("sub", "sub_sub_concept", "http://test/generator#sub_sub_concept",
      new SCH1[]{Nested_Specific_Concept},
      new SCH1[]{Nested_Specific_Concept, Specific_Concept});


  public static final String schemeName = "SCH1";
  public static final String schemeID = "0.0.0.0";
  public static final URIIdentifier schemeURI = new URIIdentifier()
      .withUri(URI.create("http://test/generator#concept_scheme1"))
      .withVersionId(URI.create("http://test/generator/v01#concept_scheme1"));

  public static final ConceptScheme<SCH1> __SELF = new DefaultConceptScheme<>(schemeID,
      schemeName,
      schemeURI.getUri(),
      schemeURI.getVersionId(),
      SCH1.class);

  private URI ref;
  private String displayName;
  private String code;
  private URI conceptId;

  private SCH1[] ancestors;
  private SCH1[] ancestorsClosure;

  SCH1(String code, String displayName, String ref, SCH1[] ancestors, SCH1[] closure) {
    this.ref = URI.create(ref);
    this.code = code;
    this.displayName = displayName;
    this.ancestors = ancestors == null ? new SCH1[0] : ancestors;
    this.ancestorsClosure = closure;
    this.conceptId = URI.create("http://test/generator#" + code);
  }


  @Override
  public String getLabel() {
    return displayName;
  }

  @Override
  public String getTag() {
    return code;
  }

  @Override
  public URI getConceptId() {
    return conceptId;
  }

  @Override
  public URI getRef() {
    return ref;
  }

  @Override
  public NamespaceIdentifier getNamespace() {
    return __SELF.asNamespace();
  }

  @Override
  public Term[] getClosure() {
    return ancestorsClosure;
  }

  public Term[] getAncestors() {
    return ancestors;
  }

  public static class Adapter extends TermsXMLAdapter {

    @Override
    protected Term[] getValues() {
      return SCH1.values();
    }
  }
}


