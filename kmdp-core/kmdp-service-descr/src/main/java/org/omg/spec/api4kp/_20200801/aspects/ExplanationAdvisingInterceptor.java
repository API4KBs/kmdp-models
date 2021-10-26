package org.omg.spec.api4kp._20200801.aspects;

import java.time.Instant;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.omg.spec.api4kp._20200801.Answer;

@Aspect
public class ExplanationAdvisingInterceptor {

  @Around("@annotation(org.omg.spec.api4kp._20200801.aspects.Explainable) && execution(* *(..))")
  public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    Object result = joinPoint.proceed();
    if (result instanceof Answer<?>) {
      ((Answer<?>) result).withAddedExplanationMessage(toTrace(joinPoint));
    }
    return result;
  }

  private String toTrace(ProceedingJoinPoint joinPoint) {
    return String.format(
        "%s / %s @%d",
        joinPoint.getSignature().getName(),
        Arrays.toString(joinPoint.getArgs()),
        Instant.now().getEpochSecond());
  }


}
