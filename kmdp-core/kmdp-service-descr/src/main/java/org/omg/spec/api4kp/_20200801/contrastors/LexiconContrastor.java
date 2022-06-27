package org.omg.spec.api4kp._20200801.contrastors;

import edu.mayo.kmdp.comparator.Contrastor;
import java.util.Collection;
import java.util.Collections;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp._20200801.taxonomy.lexicon.Lexicon;

/**
 * {@link Contrastor} that contrasts the {@link Lexicon} component of a
 * {@link SyntacticRepresentation} - to be used as part or in conjunction with the a
 * {@link SyntacticRepresentationContrastor}
 */
public class LexiconContrastor extends Contrastor<Collection<Lexicon>> {

  public static final LexiconContrastor theLexiconContrastor = new LexiconContrastor();

  protected LexiconContrastor() {
  }

  @Override
  public boolean comparable(Collection<Lexicon> l1, Collection<Lexicon> l2) {
    Collection<Lexicon> lex1 = l1 != null ? l1 : Collections.emptyList();
    Collection<Lexicon> lex2 = l2 != null ? l2 : Collections.emptyList();
    if (lex1.isEmpty() || lex2.isEmpty()) {
      return true;
    }

    boolean sup1 = lex1.stream().anyMatch(x -> x.isNoneOf(lex2));
    boolean sup2 = lex2.stream().anyMatch(x -> x.isNoneOf(lex1));
    return !(sup1 && sup2);
  }

  @Override
  public int compare(Collection<Lexicon> lex1, Collection<Lexicon> lex2) {
    boolean sup1 = lex1.stream().allMatch(x -> x.isAnyOf(lex2));
    boolean sup2 = lex2.stream().allMatch(x -> x.isAnyOf(lex1));
    if (sup1 && sup2) {
      return 0;
    } else if (sup1) {
      return 1;
    } else if (sup2) {
      return -1;
    } else {
      return 0;
    }
  }

  public boolean isEqual(SyntacticRepresentation r1, SyntacticRepresentation r2) {
    return Contrastor.isEqual(theLexiconContrastor.contrast(r1.getLexicon(), r2.getLexicon()));
  }
}
