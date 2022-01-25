package org.omg.spec.api4kp._20200801;


import edu.mayo.kmdp.util.StreamUtil;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.StatusType;

/**
 * Implementation of {@link Problem} that supports composition, by means of nested Problems
 * <p>
 * Aggregates the information in the sub-problems to determine some of the characteristics of the
 * complex
 */
@SuppressWarnings("java:S110")
public class ComplexProblem extends AbstractThrowableProblem {

  private static final String COMPONENTS_KEY = "components";

  /**
   * Explicit Constructor
   *
   * @param instance The URI of the entity that this problem refers to
   * @param status   The response code of the operation explained by this
   * @param severity The severity of the (constraints violated by) the outcome of the operation
   *                 explained by this
   * @param problems The component problems
   */
  @SuppressWarnings("unchecked")
  public ComplexProblem(
      URI instance, StatusType status, Severity severity,
      Collection<? extends Problem> problems) {
    super(
        combineType(problems).orElseGet(() -> mapSeverityToGenericType(severity)),
        null,
        status,
        null,
        instance,
        null,
        Map.of(
            COMPONENTS_KEY, new ArrayList<>(),
            Severity.KEY, severity));
    this.setStackTrace(Explainer.EMPTY_STACK_TRACE);
    List<Problem> parts = (List<Problem>) this.getParameters().get(COMPONENTS_KEY);
    problems.forEach(p -> add(p, parts, this.getType(), this.getInstance()));
  }

  /**
   * Aggregating Constructor
   * <p>
   * Builds a {@link ComplexProblem} by aggregation of the type, instance, status and severity of
   * the components
   *
   * @param problems The component problems
   */
  public ComplexProblem(Collection<? extends Problem> problems) {
    this(combineInstance(problems), combineStatus(problems), combineSeverity(problems), problems);
  }

  /**
   * If p is a ComplexProblem, returns its components, otherwise returns p itself.
   *
   * @param p the problem
   * @return the problem's components
   */
  public static List<Problem> componentsOf(Problem p) {
    if (p instanceof ComplexProblem) {
      return ((ComplexProblem) p).getComponentProblems();
    } else {
      return Collections.singletonList(p);
    }
  }

  /**
   * Accessor
   * <p>
   * Returns the components of this ComplexProblem, wrapped in an unmodifiable List.
   *
   * @return the components of this ComplexProblem
   */
  public List<Problem> getComponentProblems() {
    if (getParameters() == null) {
      return Collections.emptyList();
    }
    Object comps = this.getParameters().get(COMPONENTS_KEY);
    if (comps instanceof List) {
      List<?> l = (List<?>) comps;
      return l.stream()
          .filter(Problem.class::isInstance)
          .map(Problem.class::cast)
          .collect(Collectors.toUnmodifiableList());
    } else {
      return Collections.emptyList();
    }
  }


  /**
   * Clones a component problem and adds it to the complex as a part. Cloning preserves the original
   * problem (e.g. to support idempotence), while allowing to rewrite some of the elements, to
   * reduce rendundancy.
   * <p>
   * Specifically, when all sub-Problems reference the same type and/or instance, that information
   * is assumed inherited and stripped from the sub-problems
   *
   * @param part           the sub-problem to be added to this complex problem
   * @param parts          the list of sub-problems to add to
   * @param sharedType     the type shared by all sub-problems, if any
   * @param sharedInstance the instance shared by all sub-problems if any
   */
  private void add(Problem part, List<Problem> parts,
      URI sharedType, URI sharedInstance) {
    URI clonedType = part.getType() != null && !part.getType().equals(sharedType)
        ? part.getType() : null;
    URI clonedInstance = part.getInstance() != null && !part.getInstance().equals(sharedInstance)
        ? part.getInstance() : null;
    Problem clone = Explainer.newOutcomeProblem(
            clonedType, (Severity) part.getParameters().get(Severity.KEY))
        .withStatus(part.getStatus())
        .withDetail(part.getDetail())
        .withTitle(part.getTitle())
        .withInstance(clonedInstance)
        .build();
    parts.add(clone);
  }

  /**
   * Combines the severity of all sub-problems, returning the most severe one
   *
   * @param problems the components
   * @return the combined severity
   */
  private static Severity combineSeverity(Collection<? extends Problem> problems) {
    return problems.stream()
        .map(p -> p.getParameters().get(Severity.KEY))
        .flatMap(StreamUtil.filterAs(Severity.class))
        .reduce((s1, s2) -> s1.ordinal() > s2.ordinal() ? s1 : s2)
        .orElse(Severity.INF);
  }

  /**
   * Combines the response code of all sub-problems, returning the most severe one
   *
   * @param problems the components
   * @return the combined response code
   */
  private static StatusType combineStatus(Collection<? extends Problem> problems) {
    return problems.stream()
        .map(Problem::getStatus)
        .filter(Objects::nonNull)
        .reduce((s1, s2) -> s1.getStatusCode() > s2.getStatusCode() ? s1 : s2)
        .orElse(Status.OK);
  }

  /**
   * Tries to combine the type/code of all sub-problems into a single type. Returns a type if all
   * sub-problems share the same type, null otherwise
   *
   * @param problems the components
   * @return the combined type
   */
  private static Optional<URI> combineType(Collection<? extends Problem> problems) {
    if (problems.isEmpty()) {
      return Optional.empty();
    }
    URI type = null;
    for (Problem problem : problems) {
      URI next = problem.getType();
      if (next != null && type != null && !type.equals(next)) {
        return Optional.empty();
      } else {
        type = next;
      }
    }
    return Optional.ofNullable(type);
  }

  /**
   * Tries to combine the instance of all sub-problems into a single type. Returns an instance if
   * all sub-problems share the same, null otherwise
   *
   * @param problems the components
   * @return the combined instance
   */
  private static URI combineInstance(Collection<? extends Problem> problems) {
    if (problems.isEmpty()) {
      return null;
    }
    URI instance = null;
    for (Problem problem : problems) {
      URI next = problem.getInstance();
      if (next != null && instance != null && !instance.equals(next)) {
        return null;
      } else {
        instance = next;
      }
    }
    return instance;
  }

  /**
   * Uses the (combined) severity as a surrogate to determine a generic type for this Complex
   * problem, in case a more informative type is neither asserted nor inferred
   *
   * @param severity a Severity
   * @return {@link Explainer#GENERIC_INFO_TYPE} or {@link Explainer#GENERIC_ERROR_TYPE}, based on
   * the input Severity
   */
  private static URI mapSeverityToGenericType(Severity severity) {
    switch (severity) {
      case OK:
      case INF:
      case WRN:
      case UNK:
        return Explainer.GENERIC_INFO_TYPE;
      case ERR:
      case FATAL:
      default:
        return Explainer.GENERIC_ERROR_TYPE;
    }
  }

}