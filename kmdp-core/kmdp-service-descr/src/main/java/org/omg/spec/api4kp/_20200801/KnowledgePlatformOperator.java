package org.omg.spec.api4kp._20200801;

import org.omg.spec.api4kp._20200801.id.ResourceIdentifier;
import org.omg.spec.api4kp._20200801.services.KnowledgeProcessingOperator;
import org.omg.spec.api4kp._20200801.taxonomy.krlanguage.KnowledgeRepresentationLanguage;

public interface KnowledgePlatformOperator<T extends KnowledgeProcessingOperator> {

  ResourceIdentifier getOperatorId();

  T getDescriptor();

  KnowledgeRepresentationLanguage getSupportedLanguage();

}
