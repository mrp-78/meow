package com.social.meow.cache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CacheEvictRangeAspect {

    @Autowired
    private CacheManager cacheManager;

    private final ExpressionParser parser = new SpelExpressionParser();

    @AfterReturning("@annotation(cacheEvictRange)")
    public void evictRange(JoinPoint joinPoint, CacheEvictRange cacheEvictRange) {
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((org.aspectj.lang.reflect.CodeSignature) joinPoint.getSignature()).getParameterNames();
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        int start = parser.parseExpression(cacheEvictRange.startKey()).getValue(context, Integer.class);
        int pageSize = parser.parseExpression(cacheEvictRange.pageSize()).getValue(context, Integer.class);
        Cache cache = cacheManager.getCache(cacheEvictRange.cacheName());
        if (cache != null) {
            for (int k = start; k < start + pageSize; k++) {
                cache.evict(k);
            }
        }
    }
}
