package org.omg.spec.api4kp._20200801;

import org.zalando.problem.Problem;

/**
 * Severity is used to assess whether, and to what degree, the State of an Entity violates one or
 * more Constraints.
 * <p>
 * Constraints can be "hard" or "soft": the former are either satisfied or violated, while the
 * latter can be violated to some (partial) degree.
 * <p>
 * The primary application is assessing the impact of an operation failure, where the state and
 * constraints are, respectively, the outcome (actual results) vs the goals (desired results). Note:
 * Logging Levels (e.g. Splunk) are a narrower implementation of the same principle
 * <p>
 * More generally, Severity can be used for quality assessment / validation purposes, where State is
 * determined by the entity being assessed, and the Constraints are the quality/validation criteria.
 * Most Knowledge resources are candidate for this approach.
 *
 * @see <a href="http://www.hl7.org/fhir/valueset-issue-severity.html">FHIR implementation of the
 * same concept</a>
 */
public enum Severity {

  /**
   * Used when constraints are applicable, and satisfied
   */
  OK,
  /**
   * Used when constraints are not applicable, or no constraints
   */
  INF,
  /**
   * Used when unable to determine whether a constraint is satisfied or violated
   */
  UNK,
  /**
   * Used when a soft constraint is violated, but no mitigation is needed
   */
  WRN,
  /**
   * Used when either a hard constraint is violated, or a soft constraint is violated that needs
   * mitigation, and mitigation is possible
   */
  ERR,
  /**
   * Used when a constraint is violated, some mitigation is needed, but no mitigation is possible
   */
  FATAL;

  /**
   * Key for use in key/value pairs, where the value is the Severity level of a specific context
   */
  public static final String KEY = "api4kp:" + Severity.class.getSimpleName();

  /**
   * Determines the {@link Severity} of a {@link Problem}
   *
   * @param p the Problem
   * @return the Severity of the Problem, or UNK if unable to determine
   */
  public static Severity severityOf(Problem p) {
    if (p == null) {
      return UNK;
    }
    Object val = p.getParameters().get(KEY);
    return val instanceof Severity
        ? (Severity) val
        : UNK;
  }
}
