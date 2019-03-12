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
package org.omg.spec.api4kp._1_0.services.language.parser;

import edu.mayo.kmdp.terms.krformat._2018._08.KRFormat;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.services.ASTCarrier;
import org.omg.spec.api4kp._1_0.services.BinaryCarrier;
import org.omg.spec.api4kp._1_0.services.DocumentCarrier;
import org.omg.spec.api4kp._1_0.services.ExpressionCarrier;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

import java.util.Optional;
import java.util.Set;

public class NullParser implements Parser {

  @Override
  public Optional<KRFormat> getDefaultFormat() {
    return Optional.empty();
  }

  @Override
  public Optional<ConceptIdentifier> getDefaultCharset() {
    return Optional.empty();
  }

  @Override
  public KRLanguage getSupportedLanguage() {
    return null;
  }

  @Override
  public Set<SyntacticRepresentation> getSupportedRepresentations() {
    return null;
  }

  @Override
  public Optional<ASTCarrier> abstrakt(DocumentCarrier carrier) {
    return Optional.empty();
  }

  @Override
  public Optional<ExpressionCarrier> decode(BinaryCarrier carrier) {
    return Optional.empty();
  }

  @Override
  public Optional<DocumentCarrier> deserialize(ExpressionCarrier carrier) {
    return Optional.empty();
  }

  @Override
  public Optional<ASTCarrier> parse(ExpressionCarrier carrier) {
    return Optional.empty();
  }

  @Override
  public Optional<DocumentCarrier> concretize(ASTCarrier carrier) {
    return Optional.empty();
  }

  @Override
  public Optional<BinaryCarrier> encode(ExpressionCarrier carrier) {
    return Optional.empty();
  }

  @Override
  public Optional<ExpressionCarrier> externalize(ASTCarrier carrier) {
    return Optional.empty();
  }

  @Override
  public Optional<ExpressionCarrier> serialize(DocumentCarrier carrier) {
    return Optional.empty();
  }
}
