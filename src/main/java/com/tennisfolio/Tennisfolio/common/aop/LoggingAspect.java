package com.tennisfolio.Tennisfolio.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("execution(* com.tennisfolio..*Service.*(..))")
    public void serviceMethods(){}

    @Pointcut("@annotation(com.tennisfolio.Tennisfolio.common.aop.SkipLog) || within(com.tennisfolio.Tennisfolio.common.aop.SkipLog)")
    void skipLog(){}

    @Around("serviceMethods() && !skipLog()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable{
        long start = System.currentTimeMillis();
        String cls = joinPoint.getSignature().getDeclaringTypeName();
        String mtd = joinPoint.getSignature().getName();

        String argJson = safeJson(joinPoint.getArgs());
        try{
            Object ret = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            String retJson = safeJson(ret);
            log.info("[HTTP] {}.{} args={} -> {} ({}ms)", cls, mtd, argJson, retJson, elapsed);
            return ret;
        }catch(Throwable e){
            long elapsed = System.currentTimeMillis() - start;
            log.error("[HTTP-ERR] {}.{} args={} took {}ms msg={}",
                    cls, mtd, argJson, elapsed, e);
            throw e;
        }
    }

    private String safeJson(Object o){
        try {
            String s = objectMapper.writeValueAsString(o);
            return s.length() > 2000 ? s.substring(0,2000) + "...(truncated)" : s;
        } catch (Exception ignore) { return String.valueOf(o); }
    }
}
