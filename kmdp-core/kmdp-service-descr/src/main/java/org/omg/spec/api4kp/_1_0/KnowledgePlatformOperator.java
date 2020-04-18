package org.omg.spec.api4kp._1_0;

import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp._1_0.id.ResourceIdentifier;
import org.omg.spec.api4kp._1_0.services.KnowledgeProcessingOperator;

public interface KnowledgePlatformOperator<T extends KnowledgeProcessingOperator> {

  ResourceIdentifier getOperatorId();

  T getDescriptor();

  KnowledgeRepresentationLanguage getSupportedLanguage();

}
