package org.omg.spec.api4kp._20200801.surrogate;

import edu.mayo.kmdp.comparator.Contrastor;

public class SurrogateContrastor extends Contrastor<KnowledgeAsset> {

  @Override
  public boolean comparable(KnowledgeAsset first, KnowledgeAsset second) {
    return false;
  }

  @Override
  public int compare(KnowledgeAsset o1, KnowledgeAsset o2) {
    return 0;
  }
}
