package org.omg.spec.api4kp._20200801;

import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.services.KnowledgeProcessingOperator;

public interface KnowledgePlatformOperator<T extends KnowledgeProcessingOperator> {

  ResourceIdentifier getOperatorId();

  T getDescriptor();

  KnowledgeRepresentationLanguage getSupportedLanguage();

}
