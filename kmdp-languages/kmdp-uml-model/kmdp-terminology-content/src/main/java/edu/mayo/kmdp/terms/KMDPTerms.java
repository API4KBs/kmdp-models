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
package edu.mayo.kmdp.terms;

import edu.mayo.kmdp.id.Term;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

/**
 * This class is a placeholder for a terminology as a Service instance
 */
public class KMDPTerms extends ConceptSchemeDirectory {

  public static final KMDPTerms directory = new KMDPTerms();

  protected KMDPTerms() {
    new Reflections(new ConfigurationBuilder()
        .forPackages("edu.mayo.kmdp.terms", "edu.mayo.terms")
        .filterInputsBy(this::filter))
        .getSubTypesOf(Term.class)
        .forEach(this::register);
  }

  private boolean filter(String sourceName) {
    return sourceName.endsWith(".class");
  }

}
