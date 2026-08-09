package com.notifyhub.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    public RateLimitInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean rateLimited = handlerMethod.hasMethodAnnotation(RateLimited.class)
            || handlerMethod.getBeanType().isAnnotationPresent(RateLimited.class);

        if (!rateLimited) {
            return true;
        }

        CurrentUser currentUser = CurrentUserContext.get();
        if (currentUser != null) {
            rateLimiterService.checkAndRecord(currentUser);
        }

        return true;
    }
}
