package org.example.starter.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.starter.config.LoggingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class.getName());
    private final LoggingProperties loggingProperties;

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
        var level = loggingProperties.getLevel().toUpperCase();
        switch (level) {
            case "DEBUG":
                logger.debug(message);
                break;
            case "ERROR":
                logger.error(message);
                break;
            case "WARN":
                logger.warn(message);
                break;
            default:
                logger.info(message);
        }
    }

}
