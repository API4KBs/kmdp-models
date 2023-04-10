package org.omg.spec.api4kp._20200801.services.repository.asset;

import static java.util.stream.Collectors.toList;

import edu.mayo.kmdp.Option;
import java.util.Arrays;
import javax.xml.namespace.QName;
import org.omg.spec.api4kp._20200801.services.ParameterDefinition;
import org.omg.spec.api4kp._20200801.services.ParameterDefinitions;
import org.omg.spec.api4kp._20200801.services.repository.KnowledgeAssetCatalog;

public class ConfigurableKnowledgeAssetCatalog extends KnowledgeAssetCatalog {
  private ParameterDefinitions parameters;

  public ConfigurableKnowledgeAssetCatalog(Option<?>[] values) {
    var defs = Arrays.stream(values)
        .map(p -> new ParameterDefinition()
            .withType(new QName(p.getType().getPackageName(), p.getType().getSimpleName()))
            .withName(p.getName())
            .withDefinition(p.getDefinition())
            .withDefaultValue(p.getDefaultValue())
            .withRequired(p.isRequired()))
        .collect(toList());
    this.parameters = new ParameterDefinitions().withParameterDefinition(defs);
  }

  public ParameterDefinitions getParameters() {
    return parameters;
  }

  public void setParameters(ParameterDefinitions parameters) {
    this.parameters = parameters;
  }

  @Override
  public boolean equals(Object other) {
    return super.equals(other);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}