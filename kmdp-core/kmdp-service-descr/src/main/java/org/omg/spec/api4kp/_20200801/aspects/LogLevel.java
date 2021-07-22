package org.omg.spec.api4kp._20200801.aspects;

import java.util.function.BiConsumer;
import java.util.function.Function;
import org.slf4j.Logger;

/**
 * https://softwareengineering.stackexchange.com/questions/279690/why-does-the-trace-level-exist-and-when-should-i-use-it-rather-than-debug/
 * <ul>
 * <li> error   you MUST        do something
 * <li> warn    you MAY HAVE    to do something
 * <li> info    you SHOULD      always log this in production
 * <li> debug   you SHOULD      log this on demand, not in production
 * <li> trace   you SHOULD NOT  log this anywhere (maybe in DEV)
 * </ul>
 */
public enum LogLevel {
  TRACE(Logger::isTraceEnabled, Logger::trace, (l, t) -> l.trace(t.getMessage(), t)),
  DEBUG(Logger::isDebugEnabled, Logger::debug, (l, t) -> l.debug(t.getMessage(), t)),
  INFO(Logger::isInfoEnabled, Logger::info, (l, t) -> l.info(t.getMessage(), t)),
  WARN(Logger::isWarnEnabled, Logger::warn, (l, t) -> l.warn(t.getMessage(), t)),
  ERROR(Logger::isErrorEnabled, Logger::error, (l, t) -> l.error(t.getMessage(), t));

  final BiConsumer<Logger, String> logChannel;
  final Function<Logger, Boolean> logEnabledTest;
  final BiConsumer<Logger, Throwable> errorChannel;

  LogLevel(Function<Logger, Boolean> tester,
      BiConsumer<Logger, String> logger,
      BiConsumer<Logger, Throwable> errorHandler) {
    this.logChannel = logger;
    this.logEnabledTest = tester;
    this.errorChannel = errorHandler;
  }
}
