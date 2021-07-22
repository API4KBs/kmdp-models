package org.omg.spec.api4kp._20200801.aspects;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.omg.spec.api4kp._20200801.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class ExceptionAdvisingInterceptor {

  @Around("@annotation(org.omg.spec.api4kp._20200801.aspects.Failsafe) && execution(* *(..))")
  public Object executeGracefully(ProceedingJoinPoint joinPoint) {
    try {
      return joinPoint.proceed();
    } catch (Throwable throwable) {
      logExceptions(joinPoint, throwable);
      return Answer.failed(throwable);
    }
  }

  private void logExceptions(ProceedingJoinPoint joinPoint, Throwable exception) {
    var fs = getFailsafe(joinPoint);
    LogLevel level = Arrays.stream(fs.traces())
        .filter(t -> t.throwable().isInstance(exception))
        .findFirst()
        .map(Track::value)
        .orElse(fs.value());
    log(level, exception, joinPoint);
  }

  private void log(LogLevel l, Throwable exception, ProceedingJoinPoint joinPoint) {
    var logger = getSpecificLogger(joinPoint);
    boolean enabled = l.logEnabledTest.apply(logger);
    if (enabled) {
      l.errorChannel.accept(logger, exception);
    }
  }

  private Logger getSpecificLogger(ProceedingJoinPoint joinPoint) {
    return LoggerFactory.getLogger(joinPoint.getTarget().getClass());
  }

  private Failsafe getFailsafe(ProceedingJoinPoint joinPoint) {
    MethodSignature ms = (MethodSignature) joinPoint.getSignature();
    // must be present
    return ms.getMethod().getAnnotation(Failsafe.class);
  }
}
