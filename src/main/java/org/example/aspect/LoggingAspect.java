package org.example.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class.getName());

    @Before("@annotation(org.example.aspect.annotation.BeforeLogging)")
    public void logBefore(JoinPoint point) {
        logger.info("Before execution method: {}", point.getSignature());
    }

    @Around("@annotation(org.example.aspect.annotation.AroundLogging)")
    public Object trackingAdvice(ProceedingJoinPoint point) {
        logger.info("Tracking method start: {}", point.getSignature());

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

        logger.info("Tracking method finish. Time execution method: {}. Time execution: {} ms.", point.getSignature().getName(), (end - start));

        return result;
    }

    @AfterReturning(pointcut = "execution(public * org.example.service.TaskService.findTask(..))", returning = "result")
    public void logAfterReturning(JoinPoint point, Object result) {
        logger.info("Calling method: {}", point.getSignature());
        logger.info("Find task: {}", result.toString());
    }

    @AfterThrowing(pointcut = "execution(public * org.example.service.TaskService..*(..))", throwing = "exception")
    public void logAfterThrowing(JoinPoint point, Throwable exception) {
        logger.error("Exception catch in: {}", point.getSignature().getName());
        logger.error("Exception type is: {}", exception.getClass().getName());
    }

}
