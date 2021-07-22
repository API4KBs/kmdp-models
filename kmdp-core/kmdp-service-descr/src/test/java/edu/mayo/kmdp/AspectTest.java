package edu.mayo.kmdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.Locale;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.omg.spec.api4kp._20200801.aspects.Failsafe;
import org.omg.spec.api4kp._20200801.aspects.Explainable;
import org.omg.spec.api4kp._20200801.aspects.LogLevel;
import org.omg.spec.api4kp._20200801.aspects.Loggable;
import edu.mayo.ontology.taxonomies.ws.responsecodes.ResponseCodeSeries;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._20200801.Answer;
import org.omg.spec.api4kp._20200801.ServerSideException;
import org.omg.spec.api4kp._20200801.aspects.LoggingAdvisingInterceptor;
import org.slf4j.LoggerFactory;

class AspectTest {

  final Logger logger = (ch.qos.logback.classic.Logger)
      LoggerFactory.getLogger(Weavable.class);

  @Test
  void testLoggingAspect() {
    TestAppender<ILoggingEvent> appender = append(logger);
    Answer<Double> ans = new Weavable().doCircleArea(1.0);
    assertTrue(ans.isSuccess());

    assertEquals(1, appender.getLog().size());
    detach(logger, appender);
  }

  @Test
  void testGracefulExceptionHandlerAspectOnSuccess() {
    Answer<Double> ans1 = new Weavable().doSquareRoot(4.0);
    assertTrue(ans1.isSuccess());
    assertEquals(2.0, ans1.get());
  }

  @Test
  void testGracefulExceptionHandlerAspectOnException2() {
    Answer<Double> ans2 = new Weavable().doSquareRoot(-1.0);
    assertTrue(ans2.isFailure());
    String exp = ans2.printExplanation();
    assertTrue(exp.contains("not acceptable"));
  }

  @Test
  void testGracefulExceptionHandlerAspect() {
    Answer<Double> ans3 = new Weavable().doSquareRoot(null);
    assertTrue(ans3.isFailure());
    assertTrue(ans3.isNotFound());
  }

  @Test
  void testExplanation() {
    Weavable w = new Weavable();

    Answer<Double> ans = Answer.of(3.0)
        .flatMap(w::doMultBy2)
        .flatMap(w::doSquareRoot);

    assertTrue(ans.isSuccess());
    assertEquals(Math.sqrt(6.0), ans.get());

    System.out.println(ans.printExplanation());
    String[] explLines = ans.printExplanation().split("\n");
    assertEquals(2, explLines.length);
  }

  @Test
  void testExplanationWithError() {
    Weavable w = new Weavable();

    TestAppender<ILoggingEvent> appender = append(logger);

    Answer<Double> ans = Answer.of(-1.0)
        .flatMap(w::doMultBy2)
        .flatMap(w::doSquareRoot)
        .flatMap(w::doMultBy2);

    System.out.println(ans.printExplanation());
    String[] explLines = ans.printExplanation().split("\n");
    assertEquals(3, explLines.length);

    assertEquals(2, appender.getLog().size());
    detach(logger, appender);
  }

  public static class Weavable {

    @Loggable(level = LogLevel.ERROR)
    @Explainable
    @Failsafe(value = LogLevel.TRACE)
    public Answer<Double> doSquareRoot(Double v) {
      if (v == null) {
        throw new ServerSideException(ResponseCodeSeries.NotFound);
      }
      Double d = Math.sqrt(v);
      if (d.isNaN()) {
        throw new IllegalArgumentException("Negative numbers not acceptable " + v);
      }
      return Answer.of(d);
    }

    @Loggable(level = LogLevel.ERROR)
    public Answer<Double> doCircleArea(double r) {
      return Answer.of(Math.PI * r * r);
    }

    @Explainable
    @Loggable(level = LogLevel.WARN)
    public Answer<Double> doMultBy2(double x) {
      return Answer.of(2.0 * x);
    }
  }

  static class TestAppender<E> extends AppenderBase<E> {

    private final List<E> log = new ArrayList<>();

    public List<E> getLog() {
      return log;
    }

    @Override
    protected void append(E event) {
      log.add(event);
    }
  }

  static TestAppender<ILoggingEvent> append(Logger logger) {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    TestAppender<ILoggingEvent> appender = new TestAppender<>();
    appender.setContext(lc);
    appender.start();
    logger.addAppender(appender);
    logger.setAdditive(true);
    return appender;
  }

  static void detach(Logger logger, TestAppender<ILoggingEvent> appender) {
    appender.stop();
    logger.detachAppender(appender);
  }

  @Aspect
  public static class TestLoggingAdvisingInterceptor extends LoggingAdvisingInterceptor {

    @Override
    @Around("getLog()")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
      return super.logExecution(joinPoint);
    }


    @Override
    protected String getMessage(String msgCode, Object[] args, String defaultMessage,
        Locale aDefault) {
      return defaultMessage;
    }
  }
}
