package ru.cvhub.authservice.util.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Marker;
import org.springframework.stereotype.Component;

import static ru.cvhub.authservice.util.logging.LogUtil.marker;
import static ru.cvhub.authservice.util.logging.LogUtil.warn;

@Aspect
@Component
public class ErrorLoggableMethodCallsAspect {
    private static final Marker LOG_MARKER = marker("SERVICE");

    @Around("@within(ru.cvhub.authservice.util.logging.ErrorLoggableMethodCalls) && execution(public * *(..))")
    public Object logExceptions(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Throwable ex) {
            warn(
                    LOG_MARKER,
                    "Exception in gRPC service method",
                    "error", ex
            );
            throw ex;
        }
    }

}
