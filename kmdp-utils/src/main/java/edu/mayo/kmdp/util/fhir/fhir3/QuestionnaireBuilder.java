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
package edu.mayo.kmdp.util.fhir.fhir3;

import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.NameUtils;
import java.net.URI;
import java.util.Calendar;
import java.util.Collections;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactDetail;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Questionnaire;

public class QuestionnaireBuilder {

  private Questionnaire q;

  public static QuestionnaireBuilder newQ(URI questionnaireId, String version, String qName,
      String qDescr, String code) {
    return new QuestionnaireBuilder().newQuestionnaire(questionnaireId, version, qName, qDescr, code);
  }


  protected QuestionnaireBuilder newQuestionnaire(URI qId, String version, String qName,
      String qDescr, String qCode) {
    q = new Questionnaire();

    q.setId("q" + qId);
    q.setUrl(qId.toString());
    q.setIdentifier(Collections.singletonList(new Identifier()
        .setValue(NameUtils.getTrailingPart(qId.toString()))
        .setUse(Identifier.IdentifierUse.OFFICIAL)
        .setSystem(Registry.MAYO_ASSETS_BASE_URI)));
    q.setVersion(version);

    q.setName(qName);
    q.setTitle(qName);
    q.setDescription(qDescr);
    q.setContact(Collections.singletonList(new ContactDetail().setTelecom(
        Collections.singletonList(new ContactPoint().setValue("RSTKNOWLEDGEMGMT@mayo.edu")))));
    q.setCopyright(Calendar.getInstance().get(Calendar.YEAR) + " Mayo Clinic");

    q.setStatus(Enumerations.PublicationStatus.DRAFT);
    q.setExperimental(true);

    q.setCode(Collections.singletonList(new Coding().setCode(qCode)));

    return this;
  }

  public RootGroupBuilder body() {
    return new RootGroupBuilder();
  }


  public abstract class GroupBuilder {

    Questionnaire.QuestionnaireItemComponent item = new Questionnaire.QuestionnaireItemComponent();

    public GroupBuilder addSimpleGroup(String name) {
      new NestedGroupBuilder(this).withGroup(name);
      return this;
    }

    protected Questionnaire.QuestionnaireItemComponent newQuestion(String name,
        Questionnaire.QuestionnaireItemType qType, String code) {
      Questionnaire.QuestionnaireItemComponent question = new Questionnaire.QuestionnaireItemComponent();
      question.setLinkId(item.getLinkId() + "/" + name)
          .setType(qType)
          .setCode(Collections.singletonList(new Coding().setCode(code)));
      item.addItem(question);
      return question;
    }

    public GroupBuilder addHiddenQuestion(String name, Questionnaire.QuestionnaireItemType qType,
        String questionCode) {
      newQuestion(name, qType, questionCode)
          .setText("")
          .setRequired(false)
          .addExtension(
              new Extension().setUrl("http://hl7.org/fhir/StructureDefinition/questionnaire-hidden")
                  .setValue(new BooleanType(true)));

      return this;
    }

    public GroupBuilder addQuestion(String name, Questionnaire.QuestionnaireItemType qType,
        String questionCode, String text) {
      newQuestion(name, qType, questionCode)
          .setText(text)
          .setRequired(true);
      return this;
    }

    public GroupBuilder nestSimpleGroup(String name) {
      return new NestedGroupBuilder(this).withGroup(name);
    }

    public GroupBuilder weaveConceptDefinitions() {
      return this;
    }

    public Questionnaire get() {
      return q;
    }

    protected GroupBuilder withGroup(String name) {
      item.setLinkId(getParentLink() + "/" + name)
          .setType(Questionnaire.QuestionnaireItemType.GROUP)
          .setRequired(true)
          .setRepeats(false);
      return this;
    }

    protected abstract String getParentLink();

    public abstract GroupBuilder pop();

  }

  public class RootGroupBuilder extends GroupBuilder {

    public RootGroupBuilder() {
      q.addItem(this.item);
      this.item.setLinkId("#groups");
      this.item.setType(Questionnaire.QuestionnaireItemType.GROUP);
    }

    @Override
    protected String getParentLink() {
      return item.getLinkId();
    }

    @Override
    public GroupBuilder pop() {
      return this;
    }
  }

  public class NestedGroupBuilder extends GroupBuilder {

    GroupBuilder parent;

    public NestedGroupBuilder(GroupBuilder parent) {
      this.parent = parent;
      this.parent.item.addItem(this.item);
    }

    @Override
    protected String getParentLink() {
      return parent.item.getLinkId();
    }

    @Override
    public GroupBuilder pop() {
      return parent;
    }
  }

}
