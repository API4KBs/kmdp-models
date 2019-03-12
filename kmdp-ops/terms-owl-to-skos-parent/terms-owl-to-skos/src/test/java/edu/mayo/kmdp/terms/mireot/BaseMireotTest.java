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
package edu.mayo.kmdp.terms.mireot;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public abstract class BaseMireotTest {

  protected String baseUri;

  MireotExtractor newExtractor(String sourcePath, String baseURI) {
    MireotExtractor extractor = new MireotExtractor(
        SelectResourcesTest.class.getResourceAsStream(sourcePath), baseURI);
    baseUri = baseURI;
    return extractor;
  }

  MireotExtractor newExtractor(String sourcePath) {
    MireotExtractor extractor = new MireotExtractor(
        SelectResourcesTest.class.getResourceAsStream(sourcePath));
    baseUri = extractor.getBaseURI();
    return extractor;
  }

  Resource r(String localName) {
    return ResourceFactory.createResource(fix(baseUri, "/") + localName);
  }

  Resource h(String localName) {
    return ResourceFactory.createResource(fix(baseUri, "#") + localName);
  }

  String fix(String baseUri, String delim) {
    return baseUri.endsWith(delim) ? baseUri : baseUri + delim;
  }

}
