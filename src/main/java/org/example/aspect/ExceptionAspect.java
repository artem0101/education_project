package org.example.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ExceptionAspect {

    private final Logger logger = LoggerFactory.getLogger(ExceptionAspect.class.getName());

    @AfterThrowing(pointcut = "execution(public * org.example.service.TaskService..*(..))", throwing = "exception")
    public void logAfterThrowing(JoinPoint point, Throwable exception) {
        logger.error("Exception catch in: {}", point.getSignature().getName());
        logger.error("Exception type is: {}", exception.getClass().getName());
    }

}
