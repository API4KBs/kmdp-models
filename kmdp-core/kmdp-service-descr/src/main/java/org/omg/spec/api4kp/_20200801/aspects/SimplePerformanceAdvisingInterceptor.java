package org.omg.spec.api4kp._20200801.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Source https://www.baeldung.com/spring-aop-annotation
 */
@Aspect
public class SimplePerformanceAdvisingInterceptor {

  @Around("@annotation(org.omg.spec.api4kp._20200801.aspects.LogExecutionTime) && execution(* *(..))")
  public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();

    Object proceed = joinPoint.proceed();

    long executionTime = System.currentTimeMillis() - start;

    getLogger(joinPoint).debug("{} executed in ms {}", joinPoint.getSignature(), executionTime);

    return proceed;
  }

  private Logger getLogger(ProceedingJoinPoint joinPoint) {
    return LoggerFactory.getLogger(joinPoint.getTarget().getClass());
  }

}
