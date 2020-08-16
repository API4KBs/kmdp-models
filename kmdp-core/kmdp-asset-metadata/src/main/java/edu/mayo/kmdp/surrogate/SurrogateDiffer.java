package edu.mayo.kmdp.surrogate;

import edu.mayo.kmdp.comparator.AbstractDiffer;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeArtifact;
import edu.mayo.kmdp.metadata.surrogate.KnowledgeAsset;
import edu.mayo.kmdp.terms.ControlledTerm;
import edu.mayo.kmdp.util.URIUtil;
import java.util.Arrays;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.MappingStyle;
import org.javers.core.diff.custom.CustomValueComparator;
import org.javers.core.metamodel.clazz.EntityDefinition;
import org.omg.spec.api4kp._20200801.identifiers.NamespaceIdentifier;

public class SurrogateDiffer extends AbstractDiffer<KnowledgeAsset> {

  public SurrogateDiffer() { }

  public SurrogateDiffer(Mode mode) {
    super(mode);
  }

  @Override
  protected Javers init() {
    return JaversBuilder.javers()
        .withMappingStyle(MappingStyle.BEAN)

        .registerEntity(
            new EntityDefinition(KnowledgeAsset.class, "assetId",
                Arrays.asList("policies", "lifecycle")))
        .registerEntity(
            new EntityDefinition(KnowledgeArtifact.class, "artifactId")
        )

        .registerValue(ControlledTerm.class)

        .registerValue(NamespaceIdentifier.class, new LegacyNamespaceComparator())
        
        .build();
  }


  /*
    This class exists for backwards compatibility.
    In versions (,4.0.0], some NamespaceIdentifiers included a unique fragment
    that denoted a concept scheme associated to the namespace.
    As this fragment was removed in later versions, the custom comparator
    tentatively checks both alternatives, with and without the fragment
   */
  private class LegacyNamespaceComparator
      implements CustomValueComparator<NamespaceIdentifier> {

    @Override
    public boolean equals(NamespaceIdentifier a, NamespaceIdentifier b) {
      boolean eq = a != null && a.equals(b);
      if (!eq) {
        NamespaceIdentifier n1 = normalize(a);
        NamespaceIdentifier n2 = normalize(b);
        return n1 != null && n1.equals(n2);
      }
      return true;
    }

    private NamespaceIdentifier normalize(NamespaceIdentifier nid) {
      if (nid == null) {
        return null;
      }
      NamespaceIdentifier nnid = (NamespaceIdentifier) nid.clone();
      nnid.setId(URIUtil.normalizeURI(nid.getId()));
      return nnid;
    }

    @Override
    public String toString(NamespaceIdentifier value) {
      return value != null ? value.toString() : null;
    }
  }
}
