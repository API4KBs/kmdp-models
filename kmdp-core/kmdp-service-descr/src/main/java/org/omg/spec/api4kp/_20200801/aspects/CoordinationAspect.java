package org.omg.spec.api4kp._20200801.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("org.omg.spec.api4kp._20200801.aspects.Logging*+,"
    + "org.omg.spec.api4kp._20200801.aspects.Explanation*,"
    + "org.omg.spec.api4kp._20200801.aspects.Exception*"
)
public class CoordinationAspect {
  // empty
}
