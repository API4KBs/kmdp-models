package org.omg.spec.api4kp._20200801.aspects;

import edu.mayo.kmdp.util.Util;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public abstract class LoggingAdvisingInterceptor {

  @Pointcut("@annotation(org.omg.spec.api4kp._20200801.aspects.Loggable) && execution(* *(..))")
  public void getLog() {
    // pointcut
  }

  protected Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    var logInfo = getLoggable(joinPoint);
    var logger = getSpecificLogger(joinPoint);

    entryLog(logInfo, joinPoint, logger);

    Object result = joinPoint.proceed();

    exitLog(logInfo, joinPoint, logger);

    return result;

  }

  private void errorLog(
      Loggable info,
      String message,
      Logger logger) {
    doLog(info.level(), () -> message, logger);
  }

  private void exitLog(
      Loggable info,
      ProceedingJoinPoint joinPoint,
      Logger logger) {
    if (!Util.isEmpty(info.afterCode())) {
      doLog(info.level(),
          info.afterCode(),
          joinPoint,
          logger);
    }
  }

  private void entryLog(
      Loggable info,
      ProceedingJoinPoint joinPoint,
      Logger logger) {
    doLog(info.level(),
        info.beforeCode(),
        joinPoint,
        logger);
  }

  private void doLog(
      LogLevel level,
      String msgCode,
      ProceedingJoinPoint joinPoint,
      Logger logger) {
    doLog(level,
        () -> getMessage(
            msgCode,
            joinPoint.getArgs(),
            getDefaultMessage(joinPoint),
            Locale.getDefault()),
        logger);
  }

  protected abstract String getMessage(
      String msgCode, Object[] args, String defaultMessage, Locale aDefault);

  protected String codifyMessage(String msgCode, String message) {
    return String.format("[%s] %s", msgCode, message);
  }

  private String getDefaultMessage(ProceedingJoinPoint joinPoint) {
    return String.format(
        "Call %s / %s",
        joinPoint.getSignature().toShortString(),
        Arrays.toString(joinPoint.getArgs())
    );
  }

  private void doLog(
      LogLevel level,
      Supplier<String> msg,
      Logger logger) {
    boolean isLogEnabled = level.logEnabledTest.apply(logger);
    if (isLogEnabled) {
      level.logChannel.accept(logger, msg.get());
    }
  }

  private Logger getSpecificLogger(ProceedingJoinPoint joinPoint) {
    return LoggerFactory.getLogger(joinPoint.getTarget().getClass());
  }

  private Loggable getLoggable(ProceedingJoinPoint joinPoint) {
    MethodSignature ms = (MethodSignature) joinPoint.getSignature();
    // must be present
    return ms.getMethod().getAnnotation(Loggable.class);
  }

}
