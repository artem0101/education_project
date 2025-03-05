package org.example.starter.aspect;

import java.util.Map;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.starter.config.LoggingProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class LoggingAspect {

    private final LoggingProperties loggingProperties;

    private static final Map<String, Consumer<String>> logLevels = Map.of(
            "DEBUG", log::debug,
            "ERROR", log::error,
            "INFO", log::info,
            "WARN", log::warn
    );

    public LoggingAspect(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @Before("@annotation(org.example.starter.aspect.annotation.BeforeLogging)")
    public void logBefore(JoinPoint point) {
        logging("Before execution method: " + point.getSignature());
    }

    @Around("@annotation(org.example.starter.aspect.annotation.AroundLogging)")
    public Object trackingAdvice(ProceedingJoinPoint point) {
        logging("Tracking method start: " + point.getSignature());

        var start = System.currentTimeMillis();
        long end;
        Object result;

        try {
            result = point.proceed();
        } catch (Throwable throwable) {
            throw new RuntimeException("It's a failure.", throwable);
        } finally {
            end = System.currentTimeMillis();
        }

        logging("Tracking method finish. Time execution method: " + point.getSignature().getName() + ". Time execution: " + (end - start) + " ms.");

        return result;
    }

    @AfterReturning(pointcut ="@annotation(org.example.starter.aspect.annotation.AfterReturningLogging)", returning = "result")
    public void logAfterReturning(JoinPoint point, Object result) {
        logging("Calling method: " + point.getSignature());
        logging("Find task: " + result.toString());
    }

    @AfterThrowing(pointcut = "@annotation(org.example.starter.aspect.annotation.AfterReturningLogging)", throwing = "exception")
    public void logAfterThrowing(JoinPoint point, Throwable exception) {
        logging("Exception catch in: " + point.getSignature().getName());
        logging("Exception type is: " + exception.getClass().getName());
    }

    private void logging(String message) {
        logLevels.getOrDefault(loggingProperties.getLevel().toUpperCase(), log::info).accept(message);
    }

}
