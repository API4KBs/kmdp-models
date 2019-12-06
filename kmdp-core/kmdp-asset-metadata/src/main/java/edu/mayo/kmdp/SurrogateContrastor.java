package edu.mayo.kmdp;

import edu.mayo.kmdp.comparator.Contrastor;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;

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
